package org.sterl.ai.desk.file_name;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.sterl.ai.desk.ocr.OcrService;
import org.sterl.ai.desk.pdf.PdfDocument;
import org.sterl.ai.desk.summarise.SummariseService;

import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileNameService {

    private final OcrService ocrService;
    private final SummariseService summariseService;
    
    @Value("${file.source:./}")
    @Setter(AccessLevel.PACKAGE)
    private String sourceDir;
    @Value("${file.destination:./}")
    @Setter(AccessLevel.PACKAGE)
    private String destinationDir;
    
    private File s;
    private File d;

    @PostConstruct
    void start() {
        s = new File(sourceDir);
        d = new File(destinationDir);
        if (!d.exists()) d.mkdirs();
        log.info("Source dir:      " + s.getAbsolutePath());
        log.info("Destination dir: " + d.getAbsolutePath());
    }
    
    
    @Scheduled(fixedDelay = 10, timeUnit = TimeUnit.SECONDS)
    public void run() {
        if (!s.exists()) {
            log.warn("Source folder {} does not exists - doing nothing.", sourceDir);
            return;
        }

        var files = FileUtils.listFiles(s, new String[] {".pdf", ".PDF"}, true);
        for (File file : files) {
            handlePdf(file);
        }
    }
    
    void handlePdf(File inPdf) {
        log.info("Processing {}", inPdf.getAbsolutePath().replace(sourceDir, ""));

        var ocrPdf = ocrService.ocrPdfIfNeeded(inPdf);
        try (var pdf = new PdfDocument(ocrPdf.out())) {
            var fileMetaData = summariseService.summarise(pdf.readText());
            pdf.set(fileMetaData);

            var targetDir = toDestinationDir(inPdf, s, d);
            var resultFile = new File(targetDir + fileMetaData.toFileName() + ".pdf");
            if (resultFile.getName().length() < 4) {
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
