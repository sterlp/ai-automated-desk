package org.sterl.ai.desk.embedding;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmbeddingAgent {

    private final EmbeddingModel embeddingModel;

    public float[] toVector(String text) {
        return embeddingModel.embed(text);
    }
}
