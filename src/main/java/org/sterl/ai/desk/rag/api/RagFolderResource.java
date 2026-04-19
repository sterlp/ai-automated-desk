package org.sterl.ai.desk.rag.api;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.sterl.ai.desk.rag.RagFolderService;
import org.sterl.ai.desk.rag.api.model.RagFolder;
import org.sterl.ai.desk.rag.api.model.ReindexResult;
import org.sterl.ai.desk.rag.model.RagFolderEntity;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/rag-folders")
@RequiredArgsConstructor
public class RagFolderResource {

    private final RagFolderService ragFolderService;

    @PostMapping
    public ResponseEntity<RagFolder> create(@RequestBody RagFolder request) {
        var entity = ragFolderService.createFolder(request.getName(), request.getPath());
        return ResponseEntity
                .created(URI.create("/api/rag-folders/" + entity.getId()))
                .body(toApi(entity));
    }

    @GetMapping
    public List<RagFolder> findAll() {
        return ragFolderService.findAll().stream()
                .map(this::toApi)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<RagFolder> findById(@PathVariable String id) {
        return ragFolderService.findById(id)
                .map(this::toApi)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public RagFolder update(@PathVariable String id, @RequestBody RagFolder request) {
        var entity = ragFolderService.updateFolder(id, request.getName(), request.getPath());
        return toApi(entity);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        boolean deleted = ragFolderService.deleteFolder(id);
        return deleted
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/reindex")
    public ReindexResult reindex(@PathVariable String id) {
        return ragFolderService.reindex(id);
    }

    private RagFolder toApi(RagFolderEntity entity) {
        return RagFolder.builder()
                .id(entity.getId())
                .name(entity.getName())
                .path(entity.getPath())
                .build();
    }
}
