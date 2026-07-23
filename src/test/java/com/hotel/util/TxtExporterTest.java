package com.hotel.util;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TxtExporterTest {

    @Test
    void writesARealOpenableTabSeparatedFileWithHeaderAndCorrectColumns() throws IOException {
        TxtExporter exporter = new TxtExporter();
        Path file = Path.of("exports", "test_" + System.nanoTime() + ".txt");

        exporter.export(file, List.of("Timestamp", "Actor", "Action"), List.of(
                List.of("2026-08-01 10:00", "admin", "LOGIN_SUCCESS")));

        assertTrue(Files.exists(file));
        List<String> lines = Files.readAllLines(file);
        assertEquals(2, lines.size());
        assertEquals("Timestamp\tActor\tAction", lines.get(0));
        assertEquals("2026-08-01 10:00\tadmin\tLOGIN_SUCCESS", lines.get(1));

        Files.deleteIfExists(file);
    }
}
