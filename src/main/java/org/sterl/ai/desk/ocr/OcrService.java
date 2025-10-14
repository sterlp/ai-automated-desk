package org.sterl.ai.desk.ocr;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.sterl.ai.desk.metric.MetricService;
import org.sterl.ai.desk.pdf.PdfDocument;
import org.sterl.ai.desk.shared.FileHelper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OcrService {
    
    @Value("${ai-desk.docker:docker}")
    private String docker = "/usr/local/bin/docker";
    private final MetricService metricService;

    public record ReadFile (File out, boolean ocrDone, @Nullable String message) {
        public long size() {
            return out.length();
        }
    }
    
    public ReadFile ocrPdfIfNeeded(File inPdf) {
        try (var pdf = new PdfDocument(inPdf)) {
            var txt = pdf.readText();
            if (txt.length() < 10) {
                return ocrPdf(inPdf);
            }
            log.info("OCR not needed for {}", inPdf.getName());
            return new ReadFile(inPdf, false, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public ReadFile ocrPdf(File file) {
        FileHelper.assertIsFile(file);
        var timer = metricService.timer("ocr", getClass());

        var directory = file.getParent();
        var fileName = FilenameUtils.getBaseName(file.getName());
        
        var pb = new ProcessBuilder(
                docker, "run", "-i", "--rm",
                "jbarlow83/ocrmypdf-alpine",
                "--force-ocr", "-l", "deu", "-", "-"
        );

        var output = new File(directory + File.separatorChar + fileName + "_ocr.pdf");

        log.debug("Input={} to={} size={}kb",
                file.getName(), output.getAbsolutePath(), file.length() / 1024);

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
            if (output.length() < 2) {
                throw new RuntimeException("Output file seems to be empty + " + file.getAbsolutePath() + ":\n" + errorOutput);
            }
            timer.stop("OCR " + file.getName());
            return new ReadFile(output, true, errorOutput);
        } catch (Exception e) {
            timer.stop(e);
            if (e instanceof RuntimeException ex) throw ex;
            throw new RuntimeException("Failed to OCR " + file.getAbsolutePath() + ":\n" + errorOutput, e);
        }
    }
}
