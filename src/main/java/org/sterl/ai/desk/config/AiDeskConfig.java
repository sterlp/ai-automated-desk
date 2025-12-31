package org.sterl.ai.desk.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Configuration
@ToString(of = {"source", "destination"})
@Slf4j
public class AiDeskConfig {

    @Value("${ai-desk.file-rename.source:./}")
    @Getter
    private Path source;
    @Getter
    @Value("${ai-desk.file-rename.destination:./}")
    private Path destination;
    
    @PostConstruct
    void init() throws IOException {
        if (!Files.isDirectory(destination)) Files.createDirectories(destination);
        log.info("Source dir:      {}" + source);
        log.info("Destination dir: {}" + destination);
    }
    
    public boolean hasPdfNameFiles() {
        return Files.isDirectory(source) && Files.isDirectory(destination) && Files.isWritable(destination);
    }
}
