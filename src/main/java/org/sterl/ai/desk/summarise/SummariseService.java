package org.sterl.ai.desk.summarise;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.sterl.ai.desk.pdf.PdfUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SummariseService {

    private final OllamaChatModel ollamaChat;
    private final DocumentConverter documentConverter;
    
    public DocumentInfo summarise(String text) {
        var system = SystemMessage.builder().text("""
                You are an AI specialized in document information extraction. 
                Your task is to carefully analyze text documents (such as letters, invoices, reminders, delivery notes, insurance statements, settlements, etc.) and extract the key elements. 
                Use the language of the given text by the user for the result.
                """ + documentConverter.getFormat()
                ).build();
        var message = UserMessage.builder().text(text).build();
        var prompt = new Prompt(Arrays.asList(system, message),
                OllamaOptions.builder().format("json")
                    //.model("gpt-oss:20b") // gemma3:4b granite3-dense:8b - gpt-oss:20b
                    .build());
        
        // System.err.println("Summerize Text:\n" + text);
            

        var resutText = ollamaChat.call(prompt)
                .getResult()
                .getOutput()
                .getText();
        
        return documentConverter.convert(resutText);
    }
    
    /**
     * Currently very bad results
     */
    public DocumentInfo summarise(List<BufferedImage> images) {
        var media = new ArrayList<Media>();
        for (var i : images) {
            media.add(new Media(MimeTypeUtils.IMAGE_PNG, PdfUtil.image2Resource(i)));
        }

        var message = UserMessage.builder()
                .text("""
              You are an AI specialized in document information extraction. 
              Your task is to carefully analyze documents (such as letters, invoices, reminders, delivery notes, insurance statements, settlements, etc.) and extract the key elements. 
              Use the language of the text. Try to find for for each field the correct information. Verify your result before returning it.
              """ + documentConverter.getFormat())
                .media(media)
                .build();
        var prompt = new Prompt(message,
                OllamaOptions.builder()
                    .format("json")
                    .model("granite3.2-vision")
                    .build());
        
        System.err.println("Reading PDF AI");
        var resutText = ollamaChat.call(prompt)
                .getResult()
                .getOutput()
                .getText();

        return documentConverter.convert(resutText);
    }
}
