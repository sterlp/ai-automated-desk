package org.sterl.ai.desk.ollama;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.sterl.ai.desk.pdf.PdfDocument;
import org.sterl.ai.desk.summarise.DocumentConverter;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import reactor.netty.http.client.HttpClient;

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
    private DocumentConverter converter = new DocumentConverter(mapper);

    @Test
    void test() throws Exception {
        var rest = new RestTemplateBuilder()
                .rootUri("http://localhost:1234/v1")
                .connectTimeout(Duration.ofSeconds(2))
                .readTimeout(Duration.ofSeconds(20))
                .build();
        
        var restClient = WebClient.builder()
                .baseUrl("http://localhost:1234/v1")
                .clientConnector(new ReactorClientHttpConnector())
                .build();
        
        var pdfFile = new ClassPathResource("/Musterrechnung_ocr.pdf").getFile();
        
        var request = new AiRequest(null);
        request.setFormat(converter.getFormat());
        request.system("""
            You are an AI specialized in document information extraction. 
            Your task is to analyze the provided text document (e.g., letters, invoices, reminders, delivery notes, insurance statements, settlements) and identify its key elements. 
            Review your extracted elements and correct them if necessary before generating the final result.
            Try to find for for each field the correct information. 
            Verify your result before returning it.
            Use the language of the text for the result. If you are unsure about the language use German.
            """);
        
        try(var pdf = new PdfDocument(pdfFile)) {
            request.user(pdf.readText());
        }

        /*
        var r = rest.postForEntity("/chat/completions", request, String.class);
        System.err.println(r.getBody());
         */
        restClient.post()
            .uri("/chat/completions")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .retrieve()
            .bodyToFlux(String.class)
            .doOnNext(s -> System.err.println(s))
            .blockLast();
    }
    
    @Data
    @RequiredArgsConstructor
    static class AiRequest {
        private final String model;
        private List<Message> messages = new ArrayList<>();
        private boolean stream = true;
        // e.g. json or json schema
        private String format = null;
        
        public AiRequest addMessage(Message message) {
            this.messages.add(message);
            return this;
        }
        
        public AiRequest user(String message) {
            this.messages.add(Message.user(message));
            return this;
        }
        public AiRequest system(String message) {
            this.messages.add(Message.system(message));
            return this;
        }
    }
    // images, tool_calls
    // Role: system, user, assistant, or tool
    // {"role": "control", "content": "thinking"},
    record Message(String role, String content) {
        public enum RoleStrings {
            system,
            user,
            assistant,
            control,
            content,
            thinking
        }
        
        public Message(RoleStrings role, String message) {
            this(role.toString(), message);
        }
        
        public static Message user(String value) {
            return new Message(RoleStrings.user, value);
        }
        public static Message system(String value) {
            return new Message(RoleStrings.system, value);
        }
        public static Message assistant(String value) {
            return new Message(RoleStrings.assistant, value);
        }
    }

}
