package org.sterl.ai.desk.pdf;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class PdfUtil {

    public static List<BufferedImage> generateImages(ClassPathResource pdfResource, int dpi) {
        var imageResources = new ArrayList<BufferedImage>();

        try (PDDocument document =  Loader.loadPDF(pdfResource.getContentAsByteArray())) {
            var renderer = new PDFRenderer(document);

            for (int page = 0; page < document.getNumberOfPages(); ++page) {
                var bim = renderer.renderImageWithDPI(page, dpi, ImageType.RGB);
                imageResources.add(bim);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return imageResources;
    }

    public static Resource image2Resource(BufferedImage image) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            return new ByteArrayResource(baos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
