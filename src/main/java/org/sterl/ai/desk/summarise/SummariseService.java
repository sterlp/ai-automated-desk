package org.sterl.ai.desk.summarise;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.sterl.ai.desk.metric.MetricService;
import org.sterl.ai.desk.pdf.PdfDocument;
import org.sterl.ai.desk.pdf.PdfUtil;
import org.sterl.ai.desk.summarise.mode.AiResult;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SummariseService {

    @Setter
    private String llmModel = null;
    private final String language = "german";
    // make sure only to look at the first page
    private final int maxTextLength = 4 * 900;
    private final MetricService metricService;
    private final OllamaChatModel ollamaChat;
    private final DocumentConverter documentConverter;
    
    public AiResult<File> summariseAndNamePdf(File inPdfFile, File outDir) throws IOException {
        var timer = metricService.timer("summarisePdf", getClass());
        
        AiResult<DocumentInfo> aiResult = null;
        try (var inPdf = new PdfDocument(inPdfFile)) {
            aiResult = summarise(inPdf.readText());
            timer.stop("Summary " + inPdfFile.getName());
            
            if (aiResult.result().hasValidFileName()) {
                var pdfOutFile = new File(outDir.getAbsolutePath()
                        + File.separatorChar
                        + aiResult.result().buildFileName() 
                        + ".pdf");
                inPdf.set(aiResult.result());
                inPdf.save(pdfOutFile);
                
                log.info("Finished {} and saved as {}", inPdfFile.getName(), pdfOutFile.getAbsolutePath());
                return new AiResult<>(aiResult.timeInMs(), pdfOutFile);
            } else {
                throw new RuntimeException("Failed to generate name for "
                    + inPdfFile.getName() + " - result was "
                    + (aiResult == null ? "" : aiResult.result())
                );
            }
        }
    }
    
    public AiResult<DocumentInfo> summarise(String text) {
        var system = systemMessage();
        
        // shorten text to the given token count
        if (text.length() > maxTextLength) text = text.substring(0, maxTextLength);
        

        // shorten text to the given token count
        if (text.length() > maxTextLength) {
            text = text.substring(0, maxTextLength);
        }

        var message = UserMessage.builder().text(text).build();
        var prompt = new Prompt(Arrays.asList(system, message),
                OllamaOptions.builder()
                    .format("json")
                    .model(llmModel)
                    .build());
        
        var time = System.currentTimeMillis();
        var result = ollamaChat.call(prompt);
        time = System.currentTimeMillis() - time;

        time = modelTime(result, time);

        return new AiResult<>(time, documentConverter.convert(result.getResult().getOutput().getText()));
    }

    public SystemMessage systemMessage() {
        var system = SystemMessage.builder().text("""
                You are an AI specialized in document information extraction. 
                Your task is to analyze the provided text document (e.g., letters, invoices, reminders, delivery notes, insurance statements, settlements) and identify its key elements. 
                Review your extracted elements and correct them if necessary before generating the final result.
                The provided user text may contain spelling errors. 
                It may also contain errors through an OCR software like missing letters or blanks. You should correct it.
                Don't invent content, use only the informations provided by the user. You can summerize it, if so ensure correctness with the origional text.
                """
                + "Use the language of the text for the result. If you are unsure about the language use " + language
                + documentConverter.getFormat()
                ).build();
        return system;
    }

    public long modelTime(ChatResponse result, long defaultMs) {
        var totalTime = result.getMetadata().get("total-duration");
        var loadTime = result.getMetadata().get("load-duration");
        if (totalTime instanceof Duration t && loadTime instanceof Duration l) {
            return t.minus(l).toMillis();
        }
        return defaultMs;
    }
    
    /**
     * Currently very bad results
     */
    public AiResult<String> summarise(List<BufferedImage> images, String text) {
        var media = new ArrayList<Media>();
        for (var i : images) {
            media.add(new Media(MimeTypeUtils.IMAGE_PNG, PdfUtil.image2Resource(i)));
        }
        
        var system = systemMessage();

        var message = UserMessage.builder().text(text).media(media).build();
        var prompt = new Prompt(Arrays.asList(system, message),
                OllamaOptions.builder()
                    .format("json")
                    .model(llmModel)
                    .build());
        
        System.err.println("Reading PDF AI... ");
        var resutText = ollamaChat.call(prompt)
                .getResult()
                .getOutput()
                .getText();

        var time = System.currentTimeMillis();
        var result = ollamaChat.call(prompt);
        time = System.currentTimeMillis() - time;
        System.err.println("... done in " + time + "ms");
        time = modelTime(result, time);

        return new AiResult<>(time, result.getResult().getOutput().getText());

    }
}
