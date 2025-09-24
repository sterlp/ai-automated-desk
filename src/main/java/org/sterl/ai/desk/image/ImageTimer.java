package org.sterl.ai.desk.image;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.sterl.ai.desk.config.AiDeskConfig;
import org.sterl.ai.desk.pdf.PdfDocument;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Profile("!junit")
@Component
@RequiredArgsConstructor
@Slf4j
public class ImageTimer {

    private final ImageService imageService;
    private final AiDeskConfig config;
    
    private final String[] supportedFiles = new String[] {
            "jpg", "jpeg", "tif", "tiff", "png", "gif", "bmp", "png"
    };
    
    
    @Scheduled(fixedDelay = 10, timeUnit = TimeUnit.SECONDS)
    public void run() {
        if (!config.hasSourceDir()) {
            log.warn("Source folder {} does not exists - doing nothing.", config.getDestinationDir());
            return;
        }

        var files = FileUtils.listFiles(config.getSourceDir(), 
                supportedFiles, true);
        for (File file : files) {
            try (var pdf = new PdfDocument(new File(file.getAbsoluteFile() + ".pdf"), new PDDocument())) {
                imageService.addToPdf(pdf, file);
                pdf.save();
                file.delete();
                log.info("Converted {} to {}", file.getName(), pdf.getFile().getName());
            } catch (Exception e) {
                log.error("Failed to convert {} to a PDF. {}", file.getName(), e.getMessage(), e);
            }
        }
    }
}
