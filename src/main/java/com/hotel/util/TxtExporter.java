package com.hotel.util;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class TxtExporter {

    public void export(Path file, List<String> headers, List<List<String>> rows) throws IOException {
        Files.createDirectories(file.getParent());
        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            writer.write(String.join("\t", headers));
            writer.write("\n");
            for (List<String> row : rows) {
                writer.write(String.join("\t", row));
                writer.write("\n");
            }
        }
    }
}
