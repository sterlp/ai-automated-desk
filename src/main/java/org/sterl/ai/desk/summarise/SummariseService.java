package org.sterl.ai.desk.summarise;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
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
import org.sterl.ai.desk.metric.MetricService;
import org.sterl.ai.desk.pdf.PdfDocument;
import org.sterl.ai.desk.pdf.PdfUtil;

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
    
    public File summariseAndNamePdf(File inPdfFile, File outDir) throws IOException {
        var timer = metricService.timer("summarisePdf", getClass());
        
        DocumentInfo fileMetaData;
        try (var inPdf = new PdfDocument(inPdfFile)) {
            fileMetaData = summarise(inPdf.readText());
            timer.stop("Summary " + inPdfFile.getName());
            
            if (fileMetaData.hasValidFileName()) {
                var pdfOutFile = new File(outDir.getAbsolutePath()
                        + File.separatorChar
                        + fileMetaData.buildFileName() 
                        + ".pdf");
                inPdf.set(fileMetaData);
                inPdf.save(pdfOutFile);
                
                log.info("Finished {} and saved as {}", inPdfFile.getName(), pdfOutFile.getAbsolutePath());
                return pdfOutFile;
            } else {
                throw new RuntimeException("Failed to generate name for "
                        + inPdfFile.getName() + " - result was "
                        + fileMetaData);
            }
        }
    }
    
    public DocumentInfo summarise(String text) {
        var system = SystemMessage.builder().text("""
                /set think
                You are an AI specialized in document information extraction. 
                Your task is to analyze the provided text document (e.g., letters, invoices, reminders, delivery notes, insurance statements, settlements) and identify its key elements. 
                Review your extracted elements and correct them if necessary before generating the final result.
                Try to find for for each field the correct information. 
                Verify your result before returning it.
                """
                + "Use the language of the text for the result. If you are unsure about the language use " + language
                + documentConverter.getFormat()
                ).build();
        
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
                    .model(llmModel) // gpt-oss:20b, gemma3:4b granite3-dense:8b - gpt-oss:20b
                    .build());
        
        // System.err.println("Summerize Text:\n" + text);

        var resut = ollamaChat.call(prompt).getResult();

        // System.err.println(resut);
        return documentConverter.convert(resut.getOutput().getText());
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
