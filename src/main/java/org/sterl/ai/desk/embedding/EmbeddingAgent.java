package org.sterl.ai.desk.embedding;

import java.util.Arrays;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.ollama.api.OllamaEmbeddingOptions;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmbeddingAgent {
    private final EmbeddingModel embeddingModel;

    public float[] toVector(String text) {
        var result = embeddingModel.embed(
                Arrays.asList(new Document(text)), 
                OllamaEmbeddingOptions.builder()
                    .truncate(Boolean.TRUE)
                    .model("bge-m3:latest")
                .build(),
                new TokenCountBatchingStrategy());
        return result.get(0);
    }
}
