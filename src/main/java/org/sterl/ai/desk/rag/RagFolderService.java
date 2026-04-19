package org.sterl.ai.desk.rag;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.sterl.ai.desk.file_reader.FileReadService;
import org.sterl.ai.desk.rag.api.model.ReindexResult;
import org.sterl.ai.desk.rag.model.RagFolderEntity;
import org.sterl.ai.desk.shared.FileHelper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Manages RagFolder configurations and their associated indexed documents. */
@Service
@RequiredArgsConstructor
@Slf4j
public class RagFolderService {

    private final RagNeo4jRepository ragRepository;
    private final RagService ragService;
    private final FileReadService fileReadService;

    public RagFolderEntity createFolder(String name, String path) {
        var folderPath = Path.of(path);
        if (!Files.isDirectory(folderPath)) {
            throw new IllegalArgumentException("Path is not a valid directory: " + path);
        }
        var entity = RagFolderEntity.create(name, path);
        return ragRepository.createFolder(entity);
    }

    public List<RagFolderEntity> findAll() {
        return ragRepository.findAllFolders();
    }

    public Optional<RagFolderEntity> findById(String id) {
        return ragRepository.findFolderById(id);
    }

    /** Update folder name and/or path (e.g. when the folder was moved on disk). */
    public RagFolderEntity updateFolder(String id, String name, String path) {
        var entity = RagFolderEntity.builder()
                .id(id)
                .name(name)
                .path(path)
                .build();
        return ragRepository.updateFolder(entity);
    }

    /**
     * Delete folder and cascade-delete all associated Document nodes.
     * @return true if folder existed and was deleted
     */
    public boolean deleteFolder(String id) {
        int docsDeleted = ragService.deleteDocumentsByFolderId(id);
        boolean folderDeleted = ragRepository.deleteFolder(id);
        log.info("Deleted folder {} (existed={}), cascade-deleted {} documents",
                id, folderDeleted, docsDeleted);
        return folderDeleted;
    }

    /**
     * Reindex a folder: reads all files, checks if each is already indexed
     * with the same metadata (source, length, last_modified). Only new or
     * changed files are read and sent to the LLM for vector embedding.
     */
    public ReindexResult reindex(String folderId) {
        var folder = ragRepository.findFolderById(folderId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "RagFolder not found: " + folderId));

        var folderPath = Path.of(folder.getPath());
        var found = new AtomicInteger(0);
        var reindexed = new AtomicInteger(0);

        var stream = fileReadService.read(folderPath, f -> {
            found.incrementAndGet();

            var props = FileHelper.uniqueFileMatchMetaData(f);
            props = props.entrySet().stream()
                    .collect(Collectors.toMap(
                            e -> "metadata." + e.getKey(),
                            Map.Entry::getValue));
            props.put("metadata.folder_id", folderId);

            boolean alreadyIndexed = ragService.isKnown(props);
            if (alreadyIndexed) {
                log.debug("{} already indexed, skipping", f.getFileName());
                return false;
            }
            reindexed.incrementAndGet();
            return true;
        });

        ragService.index(stream, folderId);

        log.info("Reindex folder '{}': found {} documents, reindexed {}",
                folder.getName(), found.get(), reindexed.get());
        return new ReindexResult(folderId, found.get(), reindexed.get());
    }
}
