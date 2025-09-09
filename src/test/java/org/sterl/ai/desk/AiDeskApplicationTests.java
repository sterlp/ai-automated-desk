package org.sterl.ai.desk;

import java.io.IOException;

import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.sterl.ai.desk.pdf.PdfDocument;
import org.sterl.ai.desk.summarise.SummariseService;

@SpringBootTest
class AiDeskApplicationTests {

    @Autowired
    OllamaChatModel ollamaChat;
    @Autowired
    SummariseService summariseService;
    
    //@Test
    void testTessReadPdf() throws IOException {
        var pdfResource = new ClassPathResource("/out.PDF");
        // var pdfMedia = new Media(new MimeType("application", "pdf"), pdf);
        try (var pdf = new PdfDocument(pdfResource.getInputStream())) {
            var text = pdf.readText();
            var summerize = summariseService.summarise(text);
            System.err.println("----");
            System.err.println(summerize);
        }
    }
}
