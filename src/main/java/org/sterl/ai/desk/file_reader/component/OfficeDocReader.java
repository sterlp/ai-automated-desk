package org.sterl.ai.desk.file_reader.component;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.core.io.PathResource;
import org.springframework.stereotype.Component;
import org.sterl.ai.desk.file_reader.FileDocumentReader;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class OfficeDocReader implements FileDocumentReader {

    private final static Set<String> EXTENTIONS = 
        Set.of("doc", "docx", "xlsx", "xls", "ppt", "pptx", "html", "txt", "text");

    public boolean supports(Path path) {
        var extentision = FilenameUtils.getExtension(path.getFileName().toString());
        if (extentision == null) return false;
        return EXTENTIONS.contains(extentision.toLowerCase());
    }

    public List<Document> read(Path path) {
        var tikaDocumentReader = new TikaDocumentReader(new PathResource(path));
        var result = tikaDocumentReader.read()
                .stream().filter(d -> StringUtils.isNotBlank(d.getFormattedContent()))
                .filter(d -> StringUtils.isNotBlank(d.getText()))
                .toList();
        
        if (result.isEmpty()) {
            log.info("Empty document {} is ignored", path);
        } else {
            log.info("Read document {} with {} pages.", path, result.size());
        }
        return result;
    }
}
