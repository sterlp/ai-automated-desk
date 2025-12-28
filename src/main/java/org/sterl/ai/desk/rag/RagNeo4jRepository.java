package org.sterl.ai.desk.rag;

import java.util.List;
import java.util.Map;

import org.neo4j.cypherdsl.core.Cypher;
import org.neo4j.cypherdsl.core.ResultStatement;
import org.neo4j.cypherdsl.core.renderer.Renderer;
import org.neo4j.driver.Driver;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.neo4j.autoconfigure.Neo4jVectorStoreProperties;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class RagNeo4jRepository {

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
}
