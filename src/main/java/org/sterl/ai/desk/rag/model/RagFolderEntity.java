package org.sterl.ai.desk.rag.model;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RagFolderEntity {
    /** Unique identifier, generated as UUID string */
    private String id;
    /** Human-readable label for UI display, e.g. "Bank Briefe" */
    private String name;
    /** Filesystem path to the folder to index */
    private String path;

    public static RagFolderEntity create(String name, String path) {
        return RagFolderEntity.builder()
                .id(UUID.randomUUID().toString())
                .name(name)
                .path(path)
                .build();
    }
}
