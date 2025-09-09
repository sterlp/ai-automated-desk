package org.sterl.ai.desk.file_name;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.sterl.ai.desk.ocr.OcrService;
import org.sterl.ai.desk.pdf.PdfDocument;
import org.sterl.ai.desk.summarise.SummariseService;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

//@Service
@RequiredArgsConstructor
@Slf4j
public class FileNameService {

    private final OcrService ocrService;
    private final SummariseService summariseService;
    
    @Value("${file.source:./}")
    private String sourceDir;
    @Value("${file.source:./}")
    private String destinationDir;
    
    private File s;
    private File d;
    @PostConstruct
    void start() {
        s = new File(sourceDir);
        d = new File(destinationDir);
        if (!d.exists()) d.mkdirs();
    }
    
    
    @Scheduled(fixedDelay = 10, timeUnit = TimeUnit.SECONDS)
    public void run() {
        if (!s.exists()) {
            log.warn("Folder {} does not exists", sourceDir);
            return;
        }
        
        var files = s.listFiles();
        for (File file : files) {
            if (file.getName().endsWith(".pdf")) {
                handlePdf(file);
            }
        }
    }
    
    void handlePdf(File inPdf) {
        try (var pdf = new PdfDocument(new FileInputStream(inPdf))) {
            if 
        } catch (Exception e) {
            
        }
    }
}
