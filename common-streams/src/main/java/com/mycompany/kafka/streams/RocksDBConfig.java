package com.mycompany.kafka.streams;

import org.apache.kafka.streams.state.RocksDBConfigSetter;
import org.rocksdb.*;

import java.util.Map;

public class RocksDBConfig implements RocksDBConfigSetter {

    private static String BLOCK_SIZE_KB = "rocksdb.block.size.kb";
    private static String INDEX_FILTER_BLOCK_RATIO = "rocksdb.index.filter.block.ratio";
    private static String OPTIMIZE_FILTERS_FOR_HITS = "rocksdb.optimize.filters.for.hits";
    private static String OPTIMIZE_FILTERS_FOR_MEMORY = "rocksdb.optimize.filters.for.memory";
    private static String MAX_OPEN_FILES = "rocksdb.max.open.files";
    private static String MEMTABLE_INSTANCES = "rocksdb.memtable.instances";
    private static String MEMTABLE_SIZE_MB = "rocksdb.memtable.size.mb";
    private static String BLOCKCACHE_MEMORY_MB = "rocksdb.blockcache.memory.mb";

    // Block cache is being shared across all threads in this streams instance. So total memory per thread will be equal
    // to TOTAL_OFF_HEAP_MEMORY/T where T is the number of threads.
    private static Cache cache;
    private static final Object lock = new Object();

    @Override
    public void setConfig(String storeName, Options options, Map<String, Object> configs) {

        // Indexes and filter blocks can be big memory users and by default they don't count in memory you allocate
        // for block cache. If cache_index_and_filter_blocks set to "true", the index and filter blocks will not grow
        // unbounded. If you set cache_index_and_filter_blocks to true, index and filter blocks will be stored in block
        // cache, together with all other data blocks. This also means they can be paged out. If your access pattern is
        // very local (i.e. you have some very cold key ranges), this setting might make sense. However, in most cases
        // it will hurt your performance, since you need to have index and filter to access a certain file. An exception
        // to cache_index_and_filter_blocks=true is for L0 when setting pin_l0_filter_and_index_blocks_in_cache=true,
        // which can be a good compromise setting.

        // Block cache is where RocksDB caches uncompressed data blocks. If the data block is not found in block cache,
        // RocksDB reads it from file using buffered IO. That means it also uses the OS's page cache for raw file blocks,
        // usually containing compressed data. In a way, RocksDB's cache is two-tiered: block cache and page cache.
        // Counter-intuitively, decreasing block cache size will not increase IO. The memory saved will likely be used
        // for page cache, so even more data will be cached. However, CPU usage might grow because RocksDB needs to
        // decompress pages it reads from page cache.
        synchronized (lock) {
            if (cache == null) {

                // memtable is used for writes
                int memTableInstances = 3;
                if (configs.containsKey(MEMTABLE_INSTANCES)) {
                    // The maximum number of write buffers that are built up in memory. The default and the minimum number is 2,
                    // so that when 1 write buffer is being flushed to storage, new writes can continue to the other write
                    // buffer.If max_write_buffer_number > 3, writing will be slowed down to options.delayed_write_rate if we
                    // are writing to the last write buffer allowed.
                    memTableInstances = Integer.parseInt((String) configs.get(MEMTABLE_INSTANCES));
                    options.setMaxWriteBufferNumber(memTableInstances);
                }

                int totalMemTableMemoryMb = 48;
                if (configs.containsKey(MEMTABLE_SIZE_MB)) {
                    int memTableSizeMb = Integer.parseInt((String) configs.get(MEMTABLE_SIZE_MB));
                    options.setWriteBufferSize(memTableSizeMb * 1024L * 1024L);
                    totalMemTableMemoryMb = memTableInstances * memTableSizeMb;
                }

                // default block cache size is 50MB plus total memtable memory if specified
                int totalOffHeapMemoryMb = 50 + totalMemTableMemoryMb;
                if (configs.containsKey(BLOCKCACHE_MEMORY_MB)) {
                    totalOffHeapMemoryMb = Integer.parseInt((String) configs.get(BLOCKCACHE_MEMORY_MB)) + totalMemTableMemoryMb;
                }

                // INDEX_FILTER_BLOCK_RATIO can be used to set a fraction of the block cache to set aside for “high priority”
                // (aka index and filter) blocks, preventing them from being evicted by data blocks.
                double indexFilterBlockRatio = 0.0;
                if (configs.containsKey(INDEX_FILTER_BLOCK_RATIO)) {
                    indexFilterBlockRatio = Double.parseDouble((String) configs.get(INDEX_FILTER_BLOCK_RATIO));
                }

                // configure the size of the block cache (off-heap) used by rocksdb (might be used for index and filters
                // as well, depending on whether cache_index_and_filter_blocks=true).
                cache = new LRUCache(totalOffHeapMemoryMb * 1024L * 1024L, -1, false,
                        indexFilterBlockRatio);

                // configure the size of the memtable (off-heap) used by rocksdb
                if (configs.containsKey(MEMTABLE_INSTANCES) && configs.containsKey(MEMTABLE_SIZE_MB)) {
                    WriteBufferManager writeBufferManager = new WriteBufferManager(totalMemTableMemoryMb * 1024L * 1024L, cache);
                    options.setWriteBufferManager(writeBufferManager);
                }
            }
        }

        BlockBasedTableConfig tableConfig = (BlockBasedTableConfig) options.tableFormatConfig();
        // These options in combination will limit the memory used by RocksDB to the size passed to the block cache
        // (BLOCK_CACHE_CAPACITY_MB). To truly bound the memory, MEMTABLE_SIZE_BYTES must be set as well.
        tableConfig.setBlockCache(cache);
        tableConfig.setCacheIndexAndFilterBlocks(true);
        // This must be set to "true" in order for INDEX_FILTER_BLOCK_RATIO to be used.
        tableConfig.setCacheIndexAndFilterBlocksWithHighPriority(true);
        tableConfig.setPinTopLevelIndexAndFilter(true);

        // use LZ4 for compression to match compression used by stream serdes
        options.setCompressionType(CompressionType.LZ4_COMPRESSION);
        options.setBlobCompressionType(CompressionType.LZ4_COMPRESSION);

        // see https://github.com/facebook/rocksdb/wiki/Memory-usage-in-RocksDB#indexes-and-filter-blocks
        if (configs.containsKey(BLOCK_SIZE_KB)) {
            // For each data block, three pieces of data are stored in the index: key, offset and size. A larger block
            // size means index blocks will be smaller, but the cached data blocks may contain more cold data that would
            // otherwise be evicted. There are two ways you can reduce the size of the index. If you increase block size,
            // the number of blocks will decrease, so the index size will also reduce linearly. By default, the block
            // size is 4KB, although it often runs with 16-32KB in production. The second way to reduce the index size
            // is the reduce key size, although that might not be an option for some use-cases.
            tableConfig.setBlockSize(Integer.parseInt((String) configs.get(BLOCK_SIZE_KB)) * 1024L);
        }

        if (configs.containsKey(OPTIMIZE_FILTERS_FOR_MEMORY)) {
            // Set BlockBasedTableOptions.optimize_filters_for_memory for more jemalloc friendly bloom filter sizing.
            // Default is false.
            tableConfig.setOptimizeFiltersForMemory(Boolean.parseBoolean((String) configs.get(OPTIMIZE_FILTERS_FOR_MEMORY)));
        }
        options.setTableFormatConfig(tableConfig);

        // If cache_index_and_filter_blocks is false (which is default), the number of index/filter blocks is controlled
        // by option max_open_files. If you are certain that your ulimit will always be bigger than number of files in
        // the database, we recommend setting max_open_files to -1, which means infinity. This option will preload all
        // filter and index blocks and will not need to maintain LRU of files. Setting max_open_files to -1 will get you
        // the best possible performance.
        if (configs.containsKey(MAX_OPEN_FILES)) {
            options.setMaxOpenFiles(Integer.parseInt((String) configs.get(MAX_OPEN_FILES)));
        }

        // Calculating the size of filter blocks is easy. If you configure bloom filters with 10 bits per key (default,
        // which gives 1% of false positives), the bloom filter size is number_of_keys * 10 bits. There's one trick you
        // can play here, though. If you're certain that Get() will mostly find a key you're looking for, you can set
        // options.optimize_filters_for_hits = true. With this option turned on, we will not build bloom filters on the
        // last level, which contains 90% of the database. Thus, the memory usage for bloom filters will be 10X less.
        // You will pay one IO for each Get() that doesn't find data in the database, though.
        if (configs.containsKey(OPTIMIZE_FILTERS_FOR_HITS)) {
            options.setOptimizeFiltersForHits(Boolean.parseBoolean((String) configs.get(OPTIMIZE_FILTERS_FOR_HITS)));
        }
    }

    @Override
    public void close(String storeName, Options options) {
        // don't close cache because its being shared across instances
    }
}
