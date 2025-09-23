package org.sterl.ai.desk.pdf;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class PdfDocumentTest {

    @Test
    void testReadText() throws IOException {
        var pdfFile = new ClassPathResource("/out.PDF").getFile();
        try (var pdf = new PdfDocument(pdfFile)) {
            
            for (int i = 0; i < pdf.getNumberOfPages(); i++) {
                System.err.println("---Seite " + i + "---");
                System.err.println(pdf.readText(i));
            }
            
        }
    }
}
