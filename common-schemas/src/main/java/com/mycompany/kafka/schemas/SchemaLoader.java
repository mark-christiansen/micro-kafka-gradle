package com.mycompany.kafka.schemas;

import org.apache.avro.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

public class SchemaLoader {

    private static final Logger log = LoggerFactory.getLogger(SchemaLoader.class);

    private final Map<String, Schema> schemas = new HashMap<>();
    private String schemasPath;

    public SchemaLoader(String schemasPath) throws IOException {
        this.schemasPath = schemasPath;
        extractSchemasFromJar();
        loadSchemas();
    }

    public Schema getSchema(String name) {
        return schemas.get(name);
    }

    private void extractSchemasFromJar() throws IOException {

        final File jarFile = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
        if (jarFile.isFile()) {

            String schemaFolder = schemasPath.substring(schemasPath.lastIndexOf("/") + 1);
            String tmpDir = System.getProperty("java.io.tmpdir");

            final JarFile jar = new JarFile(jarFile);
            final Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {

                JarEntry jarEntry = entries.nextElement();
                final String name = jarEntry.getName();
                if (name.startsWith(schemaFolder)) {

                    java.io.File f = new java.io.File(tmpDir + java.io.File.separator + name);
                    if (jarEntry.isDirectory()) {
                        f.mkdir();
                        continue;
                    }

                    java.io.InputStream in = jar.getInputStream(jarEntry);
                    java.io.FileOutputStream out = new java.io.FileOutputStream(f);
                    while (in.available() > 0) {
                        out.write(in.read());
                    }
                    out.close();
                    in.close();
                }
            }
            jar.close();

            schemasPath = tmpDir + schemaFolder;
        }
    }

    private void loadSchemas() throws IOException {

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
                 BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8.name()))) {

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