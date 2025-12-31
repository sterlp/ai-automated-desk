package org.sterl.ai.desk.file_name;

import java.io.File;
import java.nio.file.Path;
import java.util.stream.Stream;

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
            var attributes = FileHelper.readFileAttributes(inPdf.toPath());

            var ocrPdf = ocrService.ocrPdfIfNeeded(inPdf);
            if (!ocrPdf.out().getAbsolutePath().equals(inPdf.getAbsolutePath())) {
                FileHelper.setAttributes(attributes, ocrPdf.out().toPath());
                inPdf.delete();
            }

            var destinationDir = new File(FileHelper.toDestinationDir(inPdf, sourceDir, targetDir));
            if (!destinationDir.exists()) destinationDir.mkdirs();
            var completedPdf = summariseService.summariseAndNamePdf(ocrPdf.out(), destinationDir);
            FileHelper.setAttributes(attributes, completedPdf.result().toPath());

            ocrPdf.out().delete();
            
            return completedPdf.result();
        } catch (Exception e) {
            log.error("Failed to process {}", inPdf.getAbsolutePath(), e);
            return null;
        }
    }
}
