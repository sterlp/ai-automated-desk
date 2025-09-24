package org.sterl.ai.desk.ocr;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.sterl.ai.desk.pdf.PdfDocument;
import org.sterl.ai.desk.shared.FileHelper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OcrService {
    
    private String docker = "/usr/local/bin/docker";

    public record ReadFile (File out, boolean ocrDone, @Nullable String message) {
        public long size() {
            return out.length();
        }
    }
    
    public ReadFile ocrPdfIfNeeded(File inPdf) {
        try (var pdf = new PdfDocument(inPdf)) {
            var txt = pdf.readText();
            if (txt.length() < 10) {
                return ocrAndReplacePdf(inPdf);
            }
            log.info("OCR not needed for {}", inPdf.getName());
            return new ReadFile(inPdf, false, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ReadFile ocrAndReplacePdf(File inPdf) {
        var ocredPdf = ocrPdf(inPdf);
        if (ocredPdf.size() > 0) {
            inPdf.delete();
            if (ocredPdf.out().renameTo(inPdf)) {
                return new ReadFile(inPdf, true, ocredPdf.message());
            } else {
                log.warn("Failed to rename {}", ocredPdf.out().getAbsolutePath());
                return ocredPdf;
            }
        } else {
            log.warn("Failed to OCR {}", inPdf.getAbsolutePath());
            ocredPdf.out().delete();
            return new ReadFile(inPdf, false, "OCR result is empty:\n" + ocredPdf.message());
        }
    }
    
    public ReadFile ocrPdf(File file) {
        FileHelper.assertIsFile(file);

        var directory = file.getParent();
        var fileName = FilenameUtils.getBaseName(file.getName());
        // Docker command
        log.debug("Input={} exists={} size={}kb",
                file.getAbsolutePath(), file.exists(), file.length() / 1024);
        
        var pb = new ProcessBuilder(
                docker, "run", "-i", "--rm",
                "jbarlow83/ocrmypdf-alpine",
                "--force-ocr", "-l", "deu", "-", "-"
        );

        var output = new File(directory + fileName + "_ocr.pdf");
        var errorOutput = "";
        try {
            output.createNewFile();
            pb.redirectInput(file);
            pb.redirectOutput(output);

            Process process = pb.start();
            try (var reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                errorOutput = reader.lines().collect(Collectors.joining("\n"));
            }
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                output.delete();
                throw new RuntimeException("Failed to OCR + " + file.getAbsolutePath() + ":\n" + errorOutput);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to OCR " + file.getAbsolutePath() + ":\n" + errorOutput, e);
        }
        return new ReadFile(output, true, errorOutput);
    }
}
