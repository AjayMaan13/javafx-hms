package com.hotel.util;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvExporterTest {

    @Test
    void writesARealOpenableFileWithHeaderAndCorrectColumns() throws IOException {
        CsvExporter exporter = new CsvExporter();
        Path file = Path.of("exports", "test_" + System.nanoTime() + ".csv");

        exporter.export(file, List.of("Date", "Total"), List.of(
                List.of("2026-08-01", "100.00"),
                List.of("2026-08-02", "200.00")));

        assertTrue(Files.exists(file));
        List<String> lines = Files.readAllLines(file);
        assertEquals(3, lines.size()); // header + 2 rows
        assertEquals("Date,Total", lines.get(0));
        assertEquals("2026-08-01,100.00", lines.get(1));

        Files.deleteIfExists(file);
    }

    @Test
    void escapesValuesContainingCommasAndQuotes() throws IOException {
        CsvExporter exporter = new CsvExporter();
        Path file = Path.of("exports", "test_" + System.nanoTime() + ".csv");

        exporter.export(file, List.of("Comment"), List.of(
                List.of("Great, would stay again \"totally\"")));

        String content = Files.readString(file);
        assertTrue(content.contains("\"Great, would stay again \"\"totally\"\"\""));

        Files.deleteIfExists(file);
    }
}
