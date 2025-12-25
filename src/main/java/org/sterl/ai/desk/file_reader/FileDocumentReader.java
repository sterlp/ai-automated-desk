package org.sterl.ai.desk.file_reader;

import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.springframework.ai.document.Document;

/**
 * Reads document with the ability to check if it is supported or not.
 */
public interface FileDocumentReader {
    boolean supports(Path path);
    List<Document> read(Path path);
    
    default Map<String, Object> fileMetaData(Path path) {
        var result = new LinkedHashMap<String, Object>();
        var file = path.toFile();
        var fileName = path.getFileName().toString();
        result.put("source", fileName);
        result.put("path", path.toString());
        result.put("type", FilenameUtils.getExtension(fileName));
        result.put("name", FilenameUtils.getBaseName(fileName));
        result.put("length", Long.valueOf(file.length()));
        result.put("changed", Instant.ofEpochMilli(file.lastModified()).toString());
        return result;
    }
}
