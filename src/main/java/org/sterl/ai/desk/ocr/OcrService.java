package org.sterl.ai.desk.ocr;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OcrService {

    public record ReadFile (File out, String message) {};
    
    public ReadFile ocrPdf(File file) {

        var fileName = file.getName();
        var directory = file.getAbsolutePath().replace(fileName, "");
        // Docker command
        log.debug("Input={} exists={} size={}kb",
                file.getAbsolutePath(), file.exists(), file.length() / 1024);
        
        if (!file.isFile()) {
            throw new IllegalArgumentException(file.getAbsolutePath() + " is not a file!");
        }
        
        var pb = new ProcessBuilder(
                "docker", "run", "-i", "--rm",
                "jbarlow83/ocrmypdf",
                "--force-ocr", "-l", "deu", "-", "-"
        );
        
        var output = new File(directory + fileName + ".ocr");
        String errorOutput = "";
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
            throw new RuntimeException("Failed to OCR + " + file.getAbsolutePath() + ":\n" + errorOutput, e);
        }
        return new ReadFile(output, errorOutput);
    }
}
