package org.sterl.ai.desk.image;

import java.io.File;
import java.util.Collection;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.sterl.ai.desk.pdf.PdfDocument;
import org.sterl.ai.desk.shared.FileHelper;

@Service
public class ImageService {

    public void addToPdf(PdfDocument doc, File image) {
        Objects.requireNonNull(doc, "Pdf cannot be null");
        Objects.requireNonNull(image, "Image cannot be null");
        FileHelper.assertIsFile(image);

        doc.addImage(image);
    }
    
    public void addToPdf(PdfDocument doc, Collection<File> images) {
        Objects.requireNonNull(images, "Images cannot be null");
        
        for (final var file : images) {
            if (file != null) doc.addImage(file);
        }
    }
}
