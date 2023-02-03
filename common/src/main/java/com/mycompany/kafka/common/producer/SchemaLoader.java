package com.mycompany.kafka.common.producer;

import org.apache.avro.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class SchemaLoader {

    private static final Logger log = LoggerFactory.getLogger(SchemaLoader.class);

    private final Map<String, Schema> schemas = new HashMap<>();

    public SchemaLoader(String schemasPath) throws IOException {
        loadSchemas(schemasPath);
    }

    public Schema getSchema(String name) {
        return schemas.get(name);
    }

    private void loadSchemas(String schemasPath) throws IOException {

        List<String> paths;
        try (Stream<Path> stream = Files.list(Paths.get(schemasPath))) {
            paths = new ArrayList<>(stream
                    .filter(file -> !Files.isDirectory(file))
                    .map((path) -> schemasPath + "/" + path.getFileName()).toList());

        }

        // sort paths by filename so that dependencies can be enforced by the
        // order of the schema files alphabetically
        paths.sort(Comparator.comparing(Objects::requireNonNull));

        for (String path : paths) {

            log.info("Loading path {}", path);
            File file = new File(path);
            try (InputStream in = new FileInputStream(file);
                 BufferedReader br = new BufferedReader(new InputStreamReader(in))) {

                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line.trim());
                }

                log.info("Loading schema from file {}", file.getName());
                Schema schema = new Schema.Parser().parse(sb.toString());
                log.info("Schema {} loaded", schema.getName().toLowerCase());
                schemas.put(schema.getName().toLowerCase(), schema);
            }
        }
    }
}