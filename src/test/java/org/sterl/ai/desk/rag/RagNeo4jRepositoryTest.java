package org.sterl.ai.desk.rag;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.neo4j.cypherdsl.core.renderer.Renderer;
import org.springframework.ai.vectorstore.neo4j.autoconfigure.Neo4jVectorStoreProperties;
import org.sterl.ai.desk.shared.MapsHelper;

class RagNeo4jRepositoryTest {

    private static final Renderer DEFAULT_RENDERER = Renderer.getDefaultRenderer();

    @Test
    void test_newDeleteStatement() {
        // GIVEN
        var props = new Neo4jVectorStoreProperties();
        var subject = new RagNeo4jRepository(new ToDocument(props), null, props);
        // WHEN
        var q = subject.newDeleteStatement(MapsHelper.toMap(
                "metadata.type", "pdf",
                "metadata.length", 798797));
        // THEN
        assertThat(DEFAULT_RENDERER.render(q))
            .isEqualTo("MATCH (n:`Document` {`metadata.type`: 'pdf', `metadata.length`: 798797}) DETACH DELETE n RETURN count(n) AS deletedCount");
    } // source=Betriebliche Projektarbeit.doc, length=148992, last_modified=2001-06-19T07:02:09Z
    
    @Test
    void test_newExistsStatement() {
        // GIVEN
        var props = new Neo4jVectorStoreProperties();
        var subject = new RagNeo4jRepository(new ToDocument(props), null, props);
        // WHEN
        var q = subject.newExistsStatement(MapsHelper.toMap(
                "metadata.type", "pdf",
                "metadata.length", 798797));
        // THEN
        assertThat(DEFAULT_RENDERER.render(q))
            .isEqualTo("OPTIONAL MATCH (Document {`metadata.type`: 'pdf', `metadata.length`: 798797}) RETURN elementId(Document) IS NOT NULL AS exists LIMIT 1");
    }
    
    @Test
    void test_newReadStatement() {
        // GIVEN
        var props = new Neo4jVectorStoreProperties();
        var subject = new RagNeo4jRepository(new ToDocument(props), null, props);
        // WHEN
        var q = subject.newReadStatement(MapsHelper.toMap(
                "metadata.type", "pdf",
                "metadata.length", 798797),
                25);
        // THEN
        assertThat(DEFAULT_RENDERER.render(q))
            .isEqualTo("MATCH (Document {`metadata.type`: 'pdf', `metadata.length`: 798797}) RETURN Document LIMIT 25");
    }
    
}
