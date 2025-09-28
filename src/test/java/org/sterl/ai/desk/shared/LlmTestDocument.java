package org.sterl.ai.desk.shared;

import java.io.File;
import java.io.IOException;

import org.springframework.core.io.ClassPathResource;
import org.sterl.ai.desk.pdf.PdfDocument;

import lombok.Getter;

@Getter
public abstract class LlmTestDocument {
    private final String name;
    private final ClassPathResource pdf;

    public LlmTestDocument(String name, String file) {
        super();
        this.name = name;
        pdf = new ClassPathResource(file);
    }

    public File getPdfFile() {
        try {
            FileHelper.assertIsFile(pdf.getFile());
            return pdf.getFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public abstract LlmScore score(PdfDocument doc);
}
