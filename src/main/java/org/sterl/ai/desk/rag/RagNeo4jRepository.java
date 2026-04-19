package org.sterl.ai.desk.rag;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.neo4j.cypherdsl.core.Cypher;
import org.neo4j.cypherdsl.core.ResultStatement;
import org.neo4j.cypherdsl.core.renderer.Renderer;
import org.neo4j.driver.Driver;
import org.neo4j.driver.types.Node;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.neo4j.autoconfigure.Neo4jVectorStoreProperties;
import org.springframework.stereotype.Component;
import org.sterl.ai.desk.rag.model.RagFolderEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class RagNeo4jRepository {

    static final String RAG_FOLDER_LABEL = "RagFolder";

    private final ToDocument toDocument;
    private final Driver driver;
    private final Neo4jVectorStoreProperties storeProperties;
    private final Renderer renderer = Renderer.getDefaultRenderer();
    
    public int delete(Map<String, Object> metaData) {
        try (var s = driver.session()) {
            var statement = newDeleteStatement(metaData);
            return s.executeWrite(tx ->
                    tx.run(renderer.render(statement))
                      .single()
                      .get("deletedCount")
                      .asInt()
            );
        }
    }

    ResultStatement newDeleteStatement(Map<String, Object> metaData) {
        var n = Cypher.node(storeProperties.getLabel())
                .withProperties(metaData)
                .named("n");

        var statement = Cypher.match(n)
                .detachDelete(n)
                .returning(Cypher.count(n).as("deletedCount"))
                .build();

        return statement;
    }
    
    // OPTIONAL MATCH (n:Document {`metadata.changed`: 1223}) RETURN n is NOT NULL LIMIT 1;
    public boolean exists(Map<String, Object> metaData) {
        try (var s = driver.session()) {
            var statement = newExistsStatement(metaData);
            var query = renderer.render(statement);
            var result = s.executeRead(tx ->
                    tx.run(renderer.render(statement))
                      .single()
                      .get("exists")
                      .asBoolean()
                  );
            log.info("{} returned {}", query, result);
            return result;
        }
    }

    ResultStatement newExistsStatement(Map<String, Object> metaData) {
        var node = Cypher.node(storeProperties.getLabel())
                .withProperties(metaData)
                .named("n");

        var statement = Cypher.optionalMatch(node)
                .returning(Cypher.isNotNull(Cypher.elementId(node)).as("exists"))
                .limit(1)
                .build();
        return statement;
    }
    
    public List<Document> findBy(Map<String, Object> metaData) {
        try (var s = driver.session()) {
            var statement = newReadStatement(metaData, 25);
            return s.executeRead(tx -> tx
                    .run(renderer.render(statement))
                    .list(toDocument::convert));
        }
    }
    
    ResultStatement newReadStatement(Map<String, Object> metaData, int limit) {
        var node = Cypher.anyNode()
                .named(storeProperties.getLabel())
                .withProperties(metaData);

        return Cypher.match(node).returning(node).limit(limit).build();
    }

    // --- RagFolder CRUD ---

    public RagFolderEntity createFolder(RagFolderEntity folder) {
        try (var s = driver.session()) {
            var statement = newCreateFolderStatement(folder);
            return s.executeWrite(tx -> {
                var record = tx.run(renderer.render(statement)).single();
                return toRagFolder(record.get("f").asNode());
            });
        }
    }

    ResultStatement newCreateFolderStatement(RagFolderEntity folder) {
        var node = Cypher.node(RAG_FOLDER_LABEL)
                .withProperties(Map.of(
                        "id", folder.getId(),
                        "name", folder.getName(),
                        "path", folder.getPath()))
                .named("f");
        return Cypher.create(node).returning(node).build();
    }

    public List<RagFolderEntity> findAllFolders() {
        try (var s = driver.session()) {
            var statement = newFindAllFoldersStatement();
            return s.executeRead(tx -> tx
                    .run(renderer.render(statement))
                    .list(r -> toRagFolder(r.get("f").asNode())));
        }
    }

    ResultStatement newFindAllFoldersStatement() {
        var node = Cypher.node(RAG_FOLDER_LABEL).named("f");
        return Cypher.match(node).returning(node).build();
    }

    public Optional<RagFolderEntity> findFolderById(String id) {
        try (var s = driver.session()) {
            var statement = newFindFolderByIdStatement(id);
            return s.executeRead(tx -> {
                var result = tx.run(renderer.render(statement));
                if (result.hasNext()) {
                    return Optional.of(toRagFolder(result.next().get("f").asNode()));
                }
                return Optional.<RagFolderEntity>empty();
            });
        }
    }

    ResultStatement newFindFolderByIdStatement(String id) {
        var node = Cypher.node(RAG_FOLDER_LABEL)
                .withProperties(Map.of("id", id))
                .named("f");
        return Cypher.match(node).returning(node).limit(1).build();
    }

    public RagFolderEntity updateFolder(RagFolderEntity folder) {
        try (var s = driver.session()) {
            var statement = newUpdateFolderStatement(folder);
            return s.executeWrite(tx -> {
                var result = tx.run(renderer.render(statement));
                if (!result.hasNext()) {
                    throw new IllegalArgumentException("RagFolder not found: " + folder.getId());
                }
                return toRagFolder(result.next().get("f").asNode());
            });
        }
    }

    ResultStatement newUpdateFolderStatement(RagFolderEntity folder) {
        var node = Cypher.node(RAG_FOLDER_LABEL)
                .withProperties(Map.of("id", folder.getId()))
                .named("f");
        return Cypher.match(node)
                .set(node.property("name").to(Cypher.literalOf(folder.getName())))
                .set(node.property("path").to(Cypher.literalOf(folder.getPath())))
                .returning(node)
                .build();
    }

    public boolean deleteFolder(String id) {
        try (var s = driver.session()) {
            var statement = newDeleteFolderStatement(id);
            return s.executeWrite(tx ->
                    tx.run(renderer.render(statement))
                      .single()
                      .get("deletedCount")
                      .asInt() > 0);
        }
    }

    ResultStatement newDeleteFolderStatement(String id) {
        var node = Cypher.node(RAG_FOLDER_LABEL)
                .withProperties(Map.of("id", id))
                .named("f");
        return Cypher.match(node)
                .detachDelete(node)
                .returning(Cypher.count(node).as("deletedCount"))
                .build();
    }

    public int deleteDocumentsByFolderId(String folderId) {
        return delete(Map.of("metadata.folder_id", folderId));
    }

    ResultStatement newDeleteByFolderIdStatement(String folderId) {
        return newDeleteStatement(Map.of("metadata.folder_id", folderId));
    }

    public int countDocumentsByFolderId(String folderId) {
        try (var s = driver.session()) {
            var statement = newCountByFolderIdStatement(folderId);
            return s.executeRead(tx ->
                    tx.run(renderer.render(statement))
                      .single()
                      .get("count")
                      .asInt());
        }
    }

    ResultStatement newCountByFolderIdStatement(String folderId) {
        var node = Cypher.node(storeProperties.getLabel())
                .withProperties(Map.of("metadata.folder_id", folderId))
                .named("n");
        return Cypher.match(node)
                .returning(Cypher.count(node).as("count"))
                .build();
    }

    private RagFolderEntity toRagFolder(Node node) {
        return RagFolderEntity.builder()
                .id(node.get("id").asString())
                .name(node.get("name").asString())
                .path(node.get("path").asString())
                .build();
    }
}
