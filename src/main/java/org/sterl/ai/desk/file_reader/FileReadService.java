package org.sterl.ai.desk.file_reader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import org.sterl.ai.desk.shared.FileHelper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileReadService {

    private final List<FileDocumentReader> readers;

    public Stream<List<Document>> read(Path path, Predicate<? super Path> filterBeforeRead) {
        if (Files.isDirectory(path)) {
            return walkDirectory(path, filterBeforeRead);
        } else {
            return Stream.of(readDoc(path, path, filterBeforeRead));
        }
    }

    private Stream<List<Document>> walkDirectory(Path baseDir, Predicate<? super Path> filterBeforeRead) {
        if (!Files.isDirectory(baseDir)) return Stream.of(Collections.emptyList());
        try {
            return Files.walk(baseDir)
                    .filter(Files::isRegularFile)
                    .filter(f -> f.toFile().length() > 10)
                    .map(f -> this.readDoc(f, baseDir, filterBeforeRead));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Document> readDoc(Path path, Path context, Predicate<? super Path> filterBeforeRead) {
        for (var r : readers) {
            if (r.supports(path)) {
                // should we handle it - even if we could?
                if (filterBeforeRead.test(path)) {
                    return  useReader(path, context, r);
                } else {
                    return Collections.emptyList();
                }
            }
        }
        log.warn("File {} not supported, no reader exists for this type.", path);
        return Collections.emptyList();
    }

    private List<Document> useReader(Path path, Path context, FileDocumentReader reader) {
        try {
            var result = reader.read(path);
            if (result.size() > 0) {
                var metaData = FileHelper.fileMetaData(path, context);
                for (var doc : result) {
                    doc.getMetadata().putAll(metaData);
                }
            }
            return result;
        } catch (Exception | StackOverflowError e) {
            log.error("{} failed to read {}", reader.getClass().getSimpleName(), path, e);
            return Collections.emptyList();
        }
    }
}
