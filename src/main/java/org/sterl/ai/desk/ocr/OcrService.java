package org.sterl.ai.desk.ocr;

import java.io.File;
import java.io.IOException;

import javax.management.RuntimeErrorException;

import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.sterl.ai.desk.pdf.PdfDocument;
import org.sterl.ai.desk.pdf.PdfUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OcrService {
    
    private final OllamaChatModel ollamaChat;

    public String readPdfAi(PdfDocument pdf) {
        var pdfAsMedia = new Media(MimeTypeUtils.IMAGE_PNG, 
                PdfUtil.image2Resource(pdf.getPageAsImage(0)));
                
        var message = UserMessage.builder()
                .text("""
              Extract all text from the pdf and format it accodingly. 
              Try to fix the missing letters through the context.
              """)
                .media(pdfAsMedia)
                .build();
        var prompt = new Prompt(message,
                OllamaOptions.builder()
                .model("granite3.2-vision")
                .build());
        
        System.err.println("Reading PDF AI");
        // TODO error handling
        return ollamaChat.call(prompt).getResult().toString();
    }
    
    public File ocrPdf(File file) {

        var fileName = file.getName();
        var directory = file.getAbsolutePath().replace(fileName, "");
        System.err.println(directory);
        System.err.println(fileName);
        // Docker command
        ProcessBuilder pb = new ProcessBuilder(
                "docker", "run", "-i", "--rm",
                "jbarlow83/ocrmypdf",
                "--skip-text", "-l", "deu", "-", "-"
        );
        
        log.debug("Input={} exists={} size={}kb",
                file.getAbsolutePath(), file.exists(), file.length() / 1024);
        if (!file.isFile()) throw new IllegalArgumentException(file.getAbsolutePath() + " is not a file!");
        
        var output = new File(directory + fileName + ".ocr");
        try {
            output.createNewFile();
            System.err.println(output.getAbsolutePath());
            pb.redirectInput(file);
            pb.redirectOutput(output);
            pb.redirectError(ProcessBuilder.Redirect.INHERIT);

            Process process = pb.start();
            int exitCode = process.waitFor();
            System.out.println("Finished with exit code " + exitCode);
        } catch (Exception e) {
            throw new RuntimeException("Failed to OCR + " + file.getAbsolutePath(), e);
        }
        return output;
    }
}
