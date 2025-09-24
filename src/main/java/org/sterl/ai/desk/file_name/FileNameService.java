package org.sterl.ai.desk.file_name;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.sterl.ai.desk.config.AiDeskConfig;
import org.sterl.ai.desk.ocr.OcrService;
import org.sterl.ai.desk.pdf.PdfDocument;
import org.sterl.ai.desk.summarise.DocumentInfo;
import org.sterl.ai.desk.summarise.SummariseService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Profile("!junit")
@Service
@RequiredArgsConstructor
@Slf4j
public class FileNameService {

    private final OcrService ocrService;
    private final SummariseService summariseService;
    
    private final AiDeskConfig aiDeskConfig;
    
    
    @Scheduled(fixedDelay = 10, timeUnit = TimeUnit.SECONDS)
    public void run() {
        if (!aiDeskConfig.hasSourceDir()) {
            log.warn("Source folder {} does not exists - doing nothing.", aiDeskConfig.getDestinationDir());
            return;
        }

        var files = FileUtils.listFiles(aiDeskConfig.getSourceDir(), new String[] {".pdf", ".PDF"}, true);
        for (File file : files) {
            handlePdf(file, aiDeskConfig.getSourceDir(), aiDeskConfig.getDestinationDir());
        }
    }
    
    void handlePdf(File inPdf, File sourceDir, File destinationDir) {
        log.info("Processing {}", inPdf.getAbsolutePath()
                .replace(sourceDir.getAbsolutePath(), ""));

        var ocrPdf = ocrService.ocrPdfIfNeeded(inPdf);
        try (var pdf = new PdfDocument(ocrPdf.out())) {
            var fileMetaData = summariseService.summarise(pdf.readText());
            pdf.set(fileMetaData);

            var targetDir = toDestinationDir(inPdf, sourceDir, destinationDir);
            var resultFile = new File(targetDir 
                    + DocumentInfo.cleanFileName(fileMetaData.getFileName()) 
                    + ".pdf");

            if (fileMetaData.getFileName().length() < 4) {
                log.warn("Failed to generate name for {} - result was {}", inPdf.getName(), fileMetaData);
            } else {
                FileUtils.createParentDirectories(resultFile);
                FileUtils.moveFile(ocrPdf.out(), resultFile);
            }
            log.info("Finished {} and moved to {}", inPdf.getName(), resultFile.getAbsolutePath());
        } catch (Exception e) {
            log.error("Failed to process {}", inPdf.getAbsolutePath(), e);
        }
    }

    /**
     * Returns the destination directory including the separator char 
     */
    static String toDestinationDir(File file, File sourceDir, File destinationDir) {
        var s = FilenameUtils.getFullPath(file.getAbsolutePath());
        var result = s.replace(sourceDir.getAbsolutePath(), destinationDir.getAbsolutePath());
        
        if (result.charAt(result.length() - 1) == File.separatorChar) return result;
        return result + File.separatorChar;
    }
}
