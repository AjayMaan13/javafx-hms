package com.hotel.util;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PdfExporterTest {

    @Test
    void writesARealOpenablePdfFile() throws IOException {
        PdfExporter exporter = new PdfExporter();
        Path file = Path.of("exports", "test_" + System.nanoTime() + ".pdf");

        exporter.export(file, "Revenue Report", List.of("Date", "Total"), List.of(
                List.of("2026-08-01", "100.00")));

        assertTrue(Files.exists(file));
        assertTrue(Files.size(file) > 0);

        // A real PDF file starts with the %PDF- magic bytes.
        byte[] header = Files.newInputStream(file).readNBytes(5);
        assertTrue(new String(header).equals("%PDF-"));

        Files.deleteIfExists(file);
    }
}
