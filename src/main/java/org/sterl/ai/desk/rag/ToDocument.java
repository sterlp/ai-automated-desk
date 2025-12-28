package org.sterl.ai.desk.rag;

import java.util.HashMap;

import org.neo4j.driver.Record;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentMetadata;
import org.springframework.ai.vectorstore.neo4j.autoconfigure.Neo4jVectorStoreProperties;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class ToDocument implements Converter<org.neo4j.driver.Record, Document> {
    
    private final Neo4jVectorStoreProperties storeProperties;
    public final String metadataPrefix = "metadata.";

    @Override
    public Document convert(Record neoRecord) {
        var node = neoRecord.get("node").asNode();
        var score = neoRecord.get("score").asFloat();
        var metaData = new HashMap<String, Object>();
        metaData.put(DocumentMetadata.DISTANCE.value(), 1 - score);
        node.keys().forEach(key -> {
            if (key.startsWith(metadataPrefix)) {
                metaData.put(key.substring(metadataPrefix.length()), node.get(key).asObject());
            }
        });

        return Document.builder()
            .id(node.get(storeProperties.getIdProperty()).asString())
            .text(node.get(storeProperties.getTextProperty()).asString())
            .metadata(metaData)
            .score((double) score)
            .build();
    }
}
