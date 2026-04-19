package org.sterl.ai.desk.rag.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RagFolder {
    private String id;
    private String name;
    private String path;
}
