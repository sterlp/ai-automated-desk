package org.sterl.ai.desk;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.sterl.ai.desk.config.AiDeskConfig;
import org.sterl.ai.desk.file_name.FileNameService;
import org.sterl.ai.desk.image.ImageService;
import org.sterl.ai.desk.pdf.PdfDocument;
import org.sterl.ai.desk.shared.FileHelper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Profile("!junit")
@Component
@RequiredArgsConstructor
@Slf4j
public class DirTimer {

    private final FileNameService fileNameService;
    private final ImageService imageService;
    private final AiDeskConfig config;
    
    @Scheduled(fixedDelay = 10, timeUnit = TimeUnit.SECONDS)
    public void run() throws IOException {
        if (!config.hasPdfNameFiles()) {
            log.info("Source folder {} does not exists - doing nothing.", config.getSource());
            return;
        }

        var time = System.currentTimeMillis();
        var count = Files.walk(config.getSource())
            .filter(Files::isRegularFile)
            .filter(s -> ".pdf".equalsIgnoreCase(FileHelper.getExtension(s)))
            .map(s -> {
                var fileType = FileHelper.getExtension(s);
                var result = 0;
                if (".pdf".equalsIgnoreCase(fileType)) {
                    var f = fileNameService.handlePdf(s.toFile(), config.getSource().toFile(), config.getDestination().toFile());
                    if ( f != null) result = 1;
                } else if (supportedImageFiles.contains(fileType)) {
                    result = handleImage(s.toFile());
                }
                return result;
            })
            .count();
        
        log.info("handled {} files in {}ms", count, System.currentTimeMillis() - time);
    }
    
    private final Set<String> supportedImageFiles = Set.of(
            "jpg", "jpeg", "tif", "tiff", "png", "gif", "bmp", "png"
    );
    
    public int handleImage(File file) {
        try (var pdf = new PdfDocument(new File(file.getAbsoluteFile() + ".pdf"), new PDDocument())) {
            imageService.addToPdf(pdf, file);
            pdf.save();
            file.delete();
            log.info("Converted {} to {}", file.getName(), pdf.getFile().getName());
            return 1;
        } catch (Exception e) {
            log.error("Failed to convert {} to a PDF. {}", file.getName(), e.getMessage(), e);
            return 0;
        }
    }
}
