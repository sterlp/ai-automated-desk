package org.sterl.ai.desk.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
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
    
    @Value("${ai-desk.docker:docker}")
    @Setter
    private String docker = "wsl docker";
    
    /*
    @Bean
    EmbeddingModel openAiEmbeddingModel(OpenAiApi openAiApi) {
        OpenAiEmbeddingOptions options = OpenAiEmbeddingOptions.builder()
          .model("text-embedding-3-small")
          .build();
        return new OpenAiEmbeddingModel(openAiApi, MetadataMode.ALL, options);
    }*/

    public List<String> dockerCommand() {
        var commands = docker.split(" ");
        var result = new ArrayList<String>();
        for (var s : commands) {
            result.add(s);
        }
        return result;
    }
    
    public List<String> dockerCommand(String value) {
        var result = dockerCommand();
        result.add(value);
        return result;
    }
    
    public List<String> dockerCommand(String... values) {
        var result = dockerCommand();
        for (String v : values) {
            result.add(v);
        }
        return result;
    }
    
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
