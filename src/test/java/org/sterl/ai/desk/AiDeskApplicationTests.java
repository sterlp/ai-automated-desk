package org.sterl.ai.desk;

import java.io.IOException;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.MimeTypeUtils;
import org.sterl.ai.desk.pdf.PdfDocument;
import org.sterl.ai.desk.pdf.PdfUtil;
import org.sterl.ai.desk.summarise.SummariseService;

@SpringBootTest
class AiDeskApplicationTests {

    @Autowired
    OllamaChatModel ollamaChat;
    @Autowired
    SummariseService summariseService;
    
    @Test
    void testTessReadPdf() throws IOException {
        var pdfResource = new ClassPathResource("/out.PDF");
        // var pdfMedia = new Media(new MimeType("application", "pdf"), pdf);
        try (var pdf = new PdfDocument(pdfResource.getInputStream())) {
            var text = pdf.readText();
            var summerize = summerize(text);
            System.err.println("----");
            System.err.println(summerize);
        }
    }

    @Test
    void testAiReadPdf() throws IOException {
        var pdfResource = new ClassPathResource("/in.PDF");
        // var pdfMedia = new Media(new MimeType("application", "pdf"), pdf);
        try (var pdf = new PdfDocument(pdfResource.getInputStream())) {
            
            var pdfString = readPdfAi(pdf);
            System.err.println("---AI---");
            System.err.println(pdfString);
            var summerize = summerize(pdfString);
            System.err.println("----");
            System.err.println(summerize);
        }
    }
}
