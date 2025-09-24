package org.sterl.ai.desk.config;

import java.io.File;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Component
@ToString(of = {"source", "destination"})
@Slf4j
public class AiDeskConfig {

    @Value("${ai-desk.file.source:./}")
    private String source;
    @Value("${ai-desk.file.destination:./}")
    private String destination;
    
    @Getter
    private File sourceDir;
    @Getter
    private File destinationDir;

    @PostConstruct
    void init() {
        sourceDir = new File(source);
        destinationDir = new File(destination);
        if (hasSourceDir() && !destinationDir.exists()) destinationDir.mkdirs();
        log.info("Source dir:      " + sourceDir.getAbsolutePath());
        log.info("Destination dir: " + destinationDir.getAbsolutePath());
    }
    
    public boolean hasSourceDir() {
        return sourceDir.exists() && sourceDir.isDirectory();
    }
}
