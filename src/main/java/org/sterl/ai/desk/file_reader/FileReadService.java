package org.sterl.ai.desk.file_reader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileReadService {

    private final List<FileDocumentReader> readers;

    public Stream<List<Document>> read(Path path) {
        if (Files.isDirectory(path)) {
            return readFolder(path);
        } else {
            return Stream.of(readDoc(path));
        }
    }

    private Stream<List<Document>> readFolder(Path path) {
        if (!Files.isDirectory(path)) Stream.of(Collections.emptyList());
        try {
            return Files.walk(path)
                    .filter(Files::isRegularFile)
                    .filter(f -> f.toFile().length() > 10)
                    .map(this::readDoc)
                    .map(docs -> {
                        for (var d : docs) {
                            var docPath = Path.of(d.getMetadata().get("path").toString()).getParent();
                            d.getMetadata().put("parent_path",  path.relativize(docPath).toString());
                        }
                        return docs;
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Document> readDoc(Path path) {
        for (var r : readers) {
            if (r.supports(path)) {
                try {
                    return r.read(path);
                } catch (Exception e) {
                    log.error("Failed to read {}", path, e);
                }
            } else {
                log.warn("File {} not supported, no reader exists.", path);
            }
        }
        return Collections.emptyList();
    }
}
