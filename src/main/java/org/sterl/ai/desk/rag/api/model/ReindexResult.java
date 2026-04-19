package org.sterl.ai.desk.rag.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReindexResult {
    private String folderId;
    private int documentsFound;
    private int documentsReindexed;
}
