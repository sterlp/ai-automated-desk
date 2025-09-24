package org.sterl.ai.desk.file_name;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.sterl.ai.desk.ocr.OcrService;
import org.sterl.ai.desk.pdf.PdfDocument;
import org.sterl.ai.desk.summarise.DocumentInfo;
import org.sterl.ai.desk.summarise.SummariseService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileNameService {

    private final OcrService ocrService;
    private final SummariseService summariseService;
    
    public File handlePdf(File inPdf, File sourceDir, File destinationDir) {
        log.info("Processing {}", inPdf.getAbsolutePath()
                .replace(sourceDir.getAbsolutePath(), ""));

        var ocrPdf = ocrService.ocrPdfIfNeeded(inPdf);
        try (var outPdf = new PdfDocument(ocrPdf.out())) {
            var fileMetaData = summariseService.summarise(outPdf.readText());
            

            var targetDir = toDestinationDir(inPdf, sourceDir, destinationDir);
            var pdfFile = new File(targetDir 
                    + DocumentInfo.cleanFileName(fileMetaData.getFileName()) 
                    + ".pdf");

            if (fileMetaData.hasValidFileName()) {
                FileUtils.createParentDirectories(pdfFile);
                outPdf.set(fileMetaData);
                outPdf.save(pdfFile);
                inPdf.delete();
                log.info("Finished {} and moved to {}", inPdf.getName(), pdfFile.getAbsolutePath());
                return pdfFile;
            } else {
                log.warn("Failed to generate name for {} - result was {}", inPdf.getName(), fileMetaData);
                return null;
            }
        } catch (Exception e) {
            log.error("Failed to process {}", inPdf.getAbsolutePath(), e);
            return null;
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
