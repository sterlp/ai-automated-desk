package org.sterl.ai.desk.file_reader;

import java.nio.file.Path;
import java.util.List;

import org.springframework.ai.document.Document;

/**
 * Reads document with the ability to check if it is supported or not.
 */
public interface FileDocumentReader {
    boolean supports(Path path);
    List<Document> read(Path path);
}
