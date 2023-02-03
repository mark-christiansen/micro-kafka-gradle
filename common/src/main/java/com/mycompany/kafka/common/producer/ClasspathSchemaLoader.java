package com.mycompany.kafka.common.producer;

import org.apache.avro.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

import static java.lang.String.format;

public class ClasspathSchemaLoader {

    private static final Logger log = LoggerFactory.getLogger(ClasspathSchemaLoader.class);

    public Map<String, Schema> load(String schemasPath) throws IOException {

        Map<String, Schema> schemas = new HashMap<>();
        ClassLoader loader = getClass().getClassLoader();

        // Micronaut doesn't have a classpath loader that supports wildcard patterns. The creators believe it is a bad
        // practice that lads to long startup times for applications. In this case I wanted to dynamically load any
        // schemas I placed in the schemas resource directory, so I implemented this behavior somewhat manually.
        final List<String> paths = new ArrayList<>();

        URI schemasURI;
        try {
            schemasURI = Objects.requireNonNull(loader.getResource(schemasPath)).toURI();
        } catch (Exception e) {
            throw new IOException(format("Error finding schemas path \"%s\" in classpath", schemasPath), e);
        }

        if ("jar".equals(schemasURI.getScheme())) {
            log.debug("Found JAR schemas path: {}", schemasURI);
            try (FileSystem fs = getFileSystem(schemasURI)) {
                try(Stream<Path> walk = Files.walk(fs.getPath(schemasPath))) {
                    for (Iterator<Path> iter = walk.iterator(); iter.hasNext(); ) {
                        Path path = iter.next();
                        if (!path.toString().endsWith(schemasPath)) {
                            paths.add(schemasPath + "/" + path.getFileName());
                            log.debug("Adding path {}", schemasPath + "/" + path.getFileName());
                        }
                    }
                }
            }
        } else {
            log.debug("Found non-JAR schemas path: {}", schemasURI);
            try(Stream<Path> walk = Files.walk(Paths.get(schemasURI))) {
                for (Iterator<Path> iter = walk.iterator(); iter.hasNext(); ) {
                    Path path = iter.next();
                    if (!path.toString().endsWith(schemasPath)) {
                        paths.add(schemasPath + "/" + path.getFileName());
                        log.debug("Adding path {}", schemasPath + "/" + path.getFileName());
                    }
                }
            }
        }

        // sort paths by filename so that dependencies can be enforced by the
        // order of the schema files alphabetically
        paths.sort(Comparator.comparing(Objects::requireNonNull));
        for (String path : paths) {

            URL schemaUrl = loader.getResource(path);
            assert schemaUrl != null;
            try (InputStream in = schemaUrl.openStream();
                 BufferedReader br = new BufferedReader(new InputStreamReader(in))) {

                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line.trim());
                }

                log.info("Loading schema from file {}", schemaUrl.getFile());
                Schema schema = new Schema.Parser().parse(sb.toString());
                log.info("Schema {} loaded", schema.getName().toLowerCase());
                schemas.put(schema.getName().toLowerCase(), schema);
            }
        }
        return schemas;
    }

    private FileSystem getFileSystem(URI uri) throws IOException {
        try {
            return FileSystems.getFileSystem(uri);
        } catch (FileSystemNotFoundException e) {
            return FileSystems.newFileSystem(uri, Collections.<String, String>emptyMap());
        }
    }
}
