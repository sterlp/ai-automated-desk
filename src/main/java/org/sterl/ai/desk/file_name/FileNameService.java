package org.sterl.ai.desk.file_name;

import java.io.File;

import org.springframework.stereotype.Service;
import org.sterl.ai.desk.ocr.OcrService;
import org.sterl.ai.desk.shared.FileHelper;
import org.sterl.ai.desk.summarise.SummariseService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileNameService {

    private final OcrService ocrService;
    private final SummariseService summariseService;
    
    public File handlePdf(File inPdf, File sourceDir, File targetDir) {
        log.info("Processing {}", inPdf.getAbsolutePath()
                .replace(sourceDir.getAbsolutePath(), ""));

        try {
            var ocrPdf = ocrService.ocrPdfIfNeeded(inPdf);
            var destinationDir = new File(FileHelper.toDestinationDir(inPdf, sourceDir, targetDir));
            if (!destinationDir.exists()) destinationDir.mkdirs();
            var completedPdf = summariseService.summariseAndNamePdf(ocrPdf.out(), destinationDir);
            inPdf.delete();
            return completedPdf.result();
        } catch (Exception e) {
            log.error("Failed to process {}", inPdf.getAbsolutePath(), e);
            return null;
        }
    }
}
