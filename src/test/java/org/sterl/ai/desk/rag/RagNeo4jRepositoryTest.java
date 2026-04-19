package org.sterl.ai.desk.rag;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.neo4j.cypherdsl.core.renderer.Renderer;
import org.springframework.ai.vectorstore.neo4j.autoconfigure.Neo4jVectorStoreProperties;
import org.sterl.ai.desk.rag.model.RagFolderEntity;
import org.sterl.ai.desk.shared.MapsHelper;

class RagNeo4jRepositoryTest {

    private static final Renderer DEFAULT_RENDERER = Renderer.getDefaultRenderer();
    private final Neo4jVectorStoreProperties props = new Neo4jVectorStoreProperties();
    private final RagNeo4jRepository subject = new RagNeo4jRepository(new ToDocument(props), null, props);

    @Test
    void test_newDeleteStatement() {
        // WHEN
        var q = subject.newDeleteStatement(MapsHelper.toMap(
                "metadata.type", "pdf",
                "metadata.length", 798797));
        // THEN
        assertThat(DEFAULT_RENDERER.render(q))
            .isEqualTo("MATCH (n:`Document` {`metadata.type`: 'pdf', `metadata.length`: 798797}) DETACH DELETE n RETURN count(n) AS deletedCount");
    }

    @Test
    void test_newExistsStatement() {
        // WHEN
        var q = subject.newExistsStatement(MapsHelper.toMap(
                "metadata.type", "pdf",
                "metadata.length", 798797));
        // THEN
        assertThat(DEFAULT_RENDERER.render(q))
            .isEqualTo("OPTIONAL MATCH (n:`Document` {`metadata.type`: 'pdf', `metadata.length`: 798797}) RETURN elementId(n) IS NOT NULL AS exists LIMIT 1");
    }

    @Test
    void test_newReadStatement() {
        // WHEN
        var q = subject.newReadStatement(MapsHelper.toMap(
                "metadata.type", "pdf",
                "metadata.length", 798797),
                25);
        // THEN
        assertThat(DEFAULT_RENDERER.render(q))
            .isEqualTo("MATCH (Document {`metadata.type`: 'pdf', `metadata.length`: 798797}) RETURN Document LIMIT 25");
    }

    // --- RagFolder statement tests ---

    @Test
    void test_newCreateFolderStatement() {
        // GIVEN
        var folder = RagFolderEntity.builder()
                .id("test-id")
                .name("Bank Briefe")
                .path("/data/bank")
                .build();
        // WHEN
        var q = subject.newCreateFolderStatement(folder);
        // THEN
        assertThat(DEFAULT_RENDERER.render(q))
            .contains("CREATE (f:`RagFolder`")
            .contains("id: 'test-id'")
            .contains("name: 'Bank Briefe'")
            .contains("path: '/data/bank'")
            .contains("RETURN f");
    }

    @Test
    void test_newFindAllFoldersStatement() {
        // WHEN
        var q = subject.newFindAllFoldersStatement();
        // THEN
        assertThat(DEFAULT_RENDERER.render(q))
            .isEqualTo("MATCH (f:`RagFolder`) RETURN f");
    }

    @Test
    void test_newFindFolderByIdStatement() {
        // WHEN
        var q = subject.newFindFolderByIdStatement("test-id");
        // THEN
        assertThat(DEFAULT_RENDERER.render(q))
            .isEqualTo("MATCH (f:`RagFolder` {id: 'test-id'}) RETURN f LIMIT 1");
    }

    @Test
    void test_newDeleteFolderStatement() {
        // WHEN
        var q = subject.newDeleteFolderStatement("test-id");
        // THEN
        assertThat(DEFAULT_RENDERER.render(q))
            .isEqualTo("MATCH (f:`RagFolder` {id: 'test-id'}) DETACH DELETE f RETURN count(f) AS deletedCount");
    }

    @Test
    void test_newDeleteByFolderIdStatement() {
        // WHEN
        var q = subject.newDeleteByFolderIdStatement("folder-123");
        // THEN
        assertThat(DEFAULT_RENDERER.render(q))
            .isEqualTo("MATCH (n:`Document` {`metadata.folder_id`: 'folder-123'}) DETACH DELETE n RETURN count(n) AS deletedCount");
    }

    @Test
    void test_newCountByFolderIdStatement() {
        // WHEN
        var q = subject.newCountByFolderIdStatement("folder-123");
        // THEN
        assertThat(DEFAULT_RENDERER.render(q))
            .isEqualTo("MATCH (n:`Document` {`metadata.folder_id`: 'folder-123'}) RETURN count(n) AS count");
    }

    @Test
    void test_newUpdateFolderStatement() {
        // GIVEN
        var folder = RagFolderEntity.builder()
                .id("test-id")
                .name("New Name")
                .path("/new/path")
                .build();
        // WHEN
        var q = subject.newUpdateFolderStatement(folder);
        // THEN
        assertThat(DEFAULT_RENDERER.render(q))
            .contains("MATCH (f:`RagFolder` {id: 'test-id'})")
            .contains("SET f.name = 'New Name'")
            .contains("SET f.path = '/new/path'")
            .contains("RETURN f");
    }
}
