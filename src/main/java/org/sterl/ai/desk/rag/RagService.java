package org.sterl.ai.desk.rag;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.neo4j.Neo4jVectorStore;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RagService {
    private final Neo4jVectorStore vectorStore;
    private final TokenTextSplitter splitter = new TokenTextSplitter(7000, 1000, 500, 5000, true);
    
    public boolean isKnown(Path path) {
        return false;
    }
    
    public void index(List<Document> docs) {
        if (docs.isEmpty()) return;
        //-- vectorStore.delete(file);
        // delete by length, name and path
        // delete by length, name and parent_path
        // if 0 = delete by parent and source
        // if 0 = delete by hash
        var toIndex = new ArrayList<Document>(docs.size());
        for (Document document : docs) {
            // raw hash code, before we change it for the AI
            document.getMetadata().put("content_hash", document.getFormattedContent().hashCode());
            if (document.getMetadata().get("path") != null) {
                document = document.mutate().text(
                        document.getMetadata().get("path") + "\n" +
                        "Last changed: " + document.getMetadata().get("changed") + "\n" +
                        document.getText()).build();
            }
            toIndex.add(document);
        }
        vectorStore.accept(splitter.apply(toIndex));
    }

    public void index(Stream<List<Document>> stream) {
        stream.forEach(this::index);
    }
}
