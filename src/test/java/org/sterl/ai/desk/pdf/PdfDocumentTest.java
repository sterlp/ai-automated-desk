package org.sterl.ai.desk.pdf;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.Iterator;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.sterl.ai.desk.pdf.PdfDocument;

class PdfDocumentTest {

    @Test
    void testReadText() throws IOException {
        var pdfResource = new ClassPathResource("/out.PDF");
        try (var pdf = new PdfDocument(pdfResource.getInputStream())) {
            
            for (int i = 0; i < pdf.getNumberOfPages(); i++) {
                System.err.println("---Seite " + i + "---");
                System.err.println(pdf.readText(i));
            }
            
        }
    }
}
