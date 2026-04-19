package org.sterl.ai.desk.rag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.neo4j.Neo4jVectorStore;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RagService {
    private final List<Character> DEFAULT_PUNCTUATION_MARKS = List.of('.', '?', '!', '\n');
    private final Neo4jVectorStore vectorStore;
    private final TokenTextSplitter splitter = new TokenTextSplitter(
            7000, 1000, 500, 5000, true, DEFAULT_PUNCTUATION_MARKS);
    
    private final RagNeo4jRepository ragRepository;
    
    public boolean isKnown(Map<String, Object> metaData) {
        return ragRepository.exists(metaData);
    }
    
    public List<Document> findBy(Map<String, Object> metaData) {
        return ragRepository.findBy(metaData);
    }
    
    public void index(List<Document> docs) {
        if (docs.isEmpty()) return;
        var toIndex = new ArrayList<Document>(docs.size());
        for (Document document : docs) {
            // raw hash code, before we change it for the AI
            var contentHash = document.getFormattedContent().hashCode();
            document.getMetadata().put("content_hash", document.getFormattedContent().hashCode());
            toIndex.add(document);

            // delete by length, name and path
            ragRepository.delete(document.getMetadata());
            // delete by length, name and parent_path
            // if 0 = delete by parent and source
            // if 0 = delete by hash
            ragRepository.delete(Map.of("content_hash", contentHash));
        }
        vectorStore.accept(splitter.apply(toIndex));
    }

    public void index(Stream<List<Document>> stream) {
        try {
            stream.forEach(this::index);
        } finally {
            stream.close();
        }
    }

    public void index(List<Document> docs, String folderId) {
        for (Document doc : docs) {
            doc.getMetadata().put("folder_id", folderId);

            // delete old chunks by stable identifiers (source + folder_id)
            // so changed files get their old chunks replaced, not duplicated
            var source = doc.getMetadata().get("source");
            if (source != null) {
                ragRepository.delete(Map.of(
                        "metadata.source", source,
                        "metadata.folder_id", folderId));
            }
        }
        index(docs);
    }

    public void index(Stream<List<Document>> stream, String folderId) {
        try {
            stream.forEach(docs -> index(docs, folderId));
        } finally {
            stream.close();
        }
    }

    public int deleteDocumentsByFolderId(String folderId) {
        return ragRepository.deleteDocumentsByFolderId(folderId);
    }

    public int countDocumentsByFolderId(String folderId) {
        return ragRepository.countDocumentsByFolderId(folderId);
    }
}
