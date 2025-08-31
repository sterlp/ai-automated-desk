package org.sterl.ai.desk.summarise;

import java.util.Arrays;

import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SummariseService {

    private final OllamaChatModel ollamaChat;
    
    public String summarise(String text) {
        var message = UserMessage.builder().text(text).build();
        var system = SystemMessage.builder().text("""
                You are an AI specialized in document information extraction. Your task is to carefully analyze text documents (such as letters, invoices, reminders, delivery notes, insurance statements, settlements, etc.) and extract the key elements. 
                Always return results in a structured JSON RFC 8259 format as one JSON Object.
                Use the language of the text.
                Specifically, identify and extract the following fields if present:
                - from: The sender’s or issuing company/organization name.
                - to:   The receiver’s company/organization name.
                - date: The date of the letter or document (use ISO format: YYYY-MM-DD if possible).
                - document_type: The type of document (e.g., Rechnung, Mahnung, Lieferschein, Abrechnung, Versicherungsrechnung, etc.).
                - document_number: Any invoice number, reference number, policy number, or other identifier.
                - other_relevant_info: Any additional key information that may be useful (e.g., customer number, contract number, claim number).
                - reason: For what reason this document was sent, e.g. Vertrag, Kreditvertrag, Kaufvertrag, Rechnung
                - abstract: one short sentence, which summerizes the document in the most exact way.
                If a field is not available in the text, return it with the value null. 
                """).build();
        var prompt = new Prompt(Arrays.asList(system, message),
                OllamaOptions.builder()
                    .model("granite3-dense:8b") // granite3-dense:granite3-dense:8b - gpt-oss:20b
                    .build());
        
        System.err.println("Summerize Text");
        // TODO error handling
        return ollamaChat.call(prompt).getResult().toString();
    }
}
