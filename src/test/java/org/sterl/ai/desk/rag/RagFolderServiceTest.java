package org.sterl.ai.desk.rag;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.sterl.ai.desk.AbstractSpringTest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class RagFolderServiceTest extends AbstractSpringTest {

    private static final String TEST_FOLDER_PATH = "./src/test/resources";

    @Autowired
    RagFolderService subject;
    @Autowired
    RagService ragService;

    @Test
    void test_reindex() {
        // GIVEN: clean up any existing folder, then create a new one
        subject.findAll().forEach(f -> subject.deleteFolder(f.getId()));
        var folder = subject.createFolder("Test Dokumente", Path.of(TEST_FOLDER_PATH).toAbsolutePath().toString());

        // WHEN: first reindex
        var result = subject.reindex(folder.getId());

        // THEN: documents should be found and indexed
        log.info("Reindex result: {}", result);
        assertThat(result.getDocumentsFound()).isGreaterThan(0);
        assertThat(result.getDocumentsReindexed()).isGreaterThan(0);
        assertThat(result.getDocumentsReindexed()).isEqualTo(result.getDocumentsFound());

        // WHEN: reindex again without changes
        var result2 = subject.reindex(folder.getId());

        // THEN: all documents found but none reindexed (already up to date)
        log.info("Second reindex result: {}", result2);
        assertThat(result2.getDocumentsFound()).isEqualTo(result.getDocumentsFound());
        assertThat(result2.getDocumentsReindexed()).isZero();

        // THEN: documents are searchable in neo4j
        int docCount = ragService.countDocumentsByFolderId(folder.getId());
        assertThat(docCount).isGreaterThan(0);
        log.info("Total document chunks in store: {}", docCount);
    }

    @Test
    void test_deleteFolder_cascadesOnlyOwnDocuments() {
        // GIVEN: clean up, then create two folders pointing to the same test resources
        subject.findAll().forEach(f -> subject.deleteFolder(f.getId()));
        var folderA = subject.createFolder("Folder A", Path.of(TEST_FOLDER_PATH).toAbsolutePath().toString());
        var folderB = subject.createFolder("Folder B", Path.of(TEST_FOLDER_PATH).toAbsolutePath().toString());

        // index documents for both folders
        var resultA = subject.reindex(folderA.getId());
        var resultB = subject.reindex(folderB.getId());
        assertThat(resultA.getDocumentsReindexed()).isGreaterThan(0);
        assertThat(resultB.getDocumentsReindexed()).isGreaterThan(0);

        int docsA = ragService.countDocumentsByFolderId(folderA.getId());
        int docsB = ragService.countDocumentsByFolderId(folderB.getId());
        assertThat(docsA).isGreaterThan(0);
        assertThat(docsB).isGreaterThan(0);

        // WHEN: delete folder A
        boolean deleted = subject.deleteFolder(folderA.getId());

        // THEN: folder A is gone
        assertThat(deleted).isTrue();
        assertThat(subject.findById(folderA.getId())).isEmpty();

        // THEN: all documents of folder A are deleted
        assertThat(ragService.countDocumentsByFolderId(folderA.getId())).isZero();

        // THEN: folder B and its documents are untouched
        assertThat(subject.findById(folderB.getId())).isPresent();
        assertThat(ragService.countDocumentsByFolderId(folderB.getId())).isEqualTo(docsB);
    }

    @Test
    void test_reindex_changedFile_shouldReplaceNotDuplicate(@TempDir Path tempDir) throws IOException {
        // GIVEN: a folder with a single test.txt
        subject.findAll().forEach(f -> subject.deleteFolder(f.getId()));
        var testFile = tempDir.resolve("test.txt");
        Files.writeString(testFile, "Original content " + UUID.randomUUID());

        var folder = subject.createFolder("Change Test", tempDir.toAbsolutePath().toString());

        // WHEN: first reindex
        var result1 = subject.reindex(folder.getId());
        assertThat(result1.getDocumentsFound()).isEqualTo(1);
        assertThat(result1.getDocumentsReindexed()).isEqualTo(1);
        int chunksAfterFirstIndex = ragService.countDocumentsByFolderId(folder.getId());
        log.info("Chunks after first index: {}", chunksAfterFirstIndex);
        assertThat(chunksAfterFirstIndex).isGreaterThan(0);

        // WHEN: modify the file and reindex
        Files.writeString(testFile, "Changed content " + UUID.randomUUID());
        var result2 = subject.reindex(folder.getId());

        // THEN: file was detected as changed and reindexed
        assertThat(result2.getDocumentsFound()).isEqualTo(1);
        assertThat(result2.getDocumentsReindexed()).isEqualTo(1);

        // THEN: chunk count should stay the same — old chunks replaced, not duplicated
        int chunksAfterSecondIndex = ragService.countDocumentsByFolderId(folder.getId());
        log.info("Chunks after second index: {}", chunksAfterSecondIndex);
        assertThat(chunksAfterSecondIndex).isEqualTo(chunksAfterFirstIndex);
    }
}
