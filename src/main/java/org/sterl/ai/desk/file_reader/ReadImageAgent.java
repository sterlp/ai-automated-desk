package org.sterl.ai.desk.file_reader;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;

import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.sterl.ai.desk.pdf.PdfUtil;
import org.sterl.ai.desk.shared.AIHelper;
import org.sterl.ai.desk.summarise.mode.AiResult;

import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Service
@RequiredArgsConstructor
public class ReadImageAgent {

    @Setter
    private String llmModel = "ministral-3:14b-instruct-2512-q8_0";
    private final OllamaChatModel ollamaChat;
    
    public AiResult<String> read(List<BufferedImage> images) {
        var media = images.stream()
                .map(i -> new Media(MimeTypeUtils.IMAGE_PNG, PdfUtil.image2Resource(i)))
                .toList();

        var system = SystemMessage.builder().text("""
                You goal is to read documents preceise as possible provided by the user to you.
                Dont add any informations which are not part of the given information.
                Don't leave anything out, read the whole document, from start to end.
                Use only informations given by the user, don't add any opinion, explanation or anything else.
                Return the whole read document - everything you can read.
                Use a well understanable structure - which represents the document as close as possible.
                Use markdown to represent the document structure e.g. tables headlines etc.
                Use mermaid to represent any e.g. graph or sequence diagrams.
                Always use the language of the document provided to you by the user in your response.
                Correct spelling errors if you find any but don't change the stucture or the informations, if you are unsure don't change anything.
                If the document is blank and you can read nothing, return an empty string / nothing.
                If you cant find any text or the picture is not a document return an empty string / nothing.
                """
                ).build();

        var message = UserMessage.builder().text("").media(media).build();
        var prompt = new Prompt(Arrays.asList(system, message),
                OllamaChatOptions.builder()
                    .model(llmModel)
                    .temperature(0.5)
                    .build());
        
        var time = System.currentTimeMillis();
        var result = ollamaChat.call(prompt);
        time = System.currentTimeMillis() - time;
        time = AIHelper.modelTime(result, time);

        return new AiResult<>(time, llmModel, result.getResult().getOutput().getText());
    }
}
