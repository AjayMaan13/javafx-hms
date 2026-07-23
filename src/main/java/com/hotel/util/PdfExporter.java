package com.hotel.util;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class PdfExporter {

    public void export(Path file, String title, List<String> headers, List<List<String>> rows) throws IOException {
        Files.createDirectories(file.getParent());

        Document document = new Document();
        // document.close() must run before the try-with-resources auto-closes `out` —
        // otherwise the writer tries to flush its trailer into an already-closed stream.
        // So document.close() is called explicitly inside the try, not in a finally here.
        try (FileOutputStream out = new FileOutputStream(file.toFile())) {
            PdfWriter.getInstance(document, out);
            document.open();

            document.add(new Paragraph(title, new Font(Font.HELVETICA, 16, Font.BOLD)));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(headers.size());
            table.setWidthPercentage(100);
            Font headerFont = new Font(Font.HELVETICA, 11, Font.BOLD);
            for (String header : headers) {
                table.addCell(new PdfPCell(new Paragraph(header, headerFont)));
            }
            for (List<String> row : rows) {
                for (String value : row) {
                    table.addCell(value == null ? "" : value);
                }
            }
            document.add(table);
            document.close();
        } catch (DocumentException e) {
            throw new IOException("Failed to generate PDF", e);
        }
    }
}
