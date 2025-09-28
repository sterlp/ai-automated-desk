package org.sterl.ai.desk.ollama;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.ClassPathResource;
import org.sterl.ai.desk.pdf.PdfDocument;
import org.sterl.ai.desk.summarise.DocumentConverter;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
### granite3.3:8b
- Runtime: 217s
- LLM Score: 8/23 = 34%
#### File name
2013-12-28_HotelGasthofStern_Invoicenr207581.pdf
#### Title
Rechnung Nr. 207581
#### Creator
Hotel-Gasthof Stern, Postfach 20 23, 86310 Pfaffenhausen
#### Subject
This is a invoice from Hotel-Gasthof Stern dated 28.12.2013 for various supplies, with a total amount of €701.68.
 */
class RestApiTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    void test() throws Exception {
        var rest = new RestTemplateBuilder()
                .rootUri("http://localhost:11434/api")
                .build();
        
        var pdfFile = new ClassPathResource("/Musterrechnung_ocr.pdf").getFile();
        
        var request = new OllamaRequest("granite3.3:8b");
        request.system("""
            You are an AI specialized in document information extraction. 
            Your task is to analyze the provided text document (e.g., letters, invoices, reminders, delivery notes, insurance statements, settlements) and identify its key elements. 
            Review your extracted elements and correct them if necessary before generating the final result.
            Try to find for for each field the correct information. 
            Verify your result before returning it.
            """
            + "Use the language of the text for the result. If you are unsure about the language use German."
            + new DocumentConverter(mapper).getFormat());
        
        try(var pdf = new PdfDocument(pdfFile)) {
            request.user(pdf.readText());
        }

        var r = rest.postForEntity("/chat", request, String.class);
        
        System.err.println(r.getBody());
    }
    
    @Data
    @RequiredArgsConstructor
    static class OllamaRequest {
        private final String model;
        private List<Message> messages = new ArrayList<>();
        private boolean stream = false;
        // json
        private String format = "json";
        
        public OllamaRequest addMessage(Message message) {
            this.messages.add(message);
            return this;
        }
        
        public OllamaRequest user(String message) {
            this.messages.add(Message.user(message));
            return this;
        }
        public OllamaRequest system(String message) {
            this.messages.add(Message.system(message));
            return this;
        }
    }
    // images, tool_calls
    // Role: system, user, assistant, or tool
    // {"role": "control", "content": "thinking"},
    record Message(String role, String content) {
        public final static Message GRANITE_THINK = new Message("control", "content");
        
        public static Message user(String value) {
            return new Message("user", value);
        }
        public static Message system(String value) {
            return new Message("system", value);
        }
    }

}
