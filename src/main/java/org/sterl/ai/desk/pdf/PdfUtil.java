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
import org.springframework.ai.content.Media;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.MimeTypeUtils;

public class PdfUtil {

    public static List<BufferedImage> generateImages(ClassPathResource pdfResource, int dpi) {
        try (var document = Loader.loadPDF(pdfResource.getContentAsByteArray())) {
            return pdfToPng(document, dpi);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static List<BufferedImage> pdfToPng(PDDocument document, int dpi) throws IOException {
        var renderer = new PDFRenderer(document);
        var imageResources = new ArrayList<BufferedImage>();

        for (int page = 0; page < document.getNumberOfPages(); ++page) {
            var bim = renderer.renderImageWithDPI(page, dpi, ImageType.RGB);
            imageResources.add(bim);
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

    public static Media toMedia(BufferedImage img) {
       return new Media(MimeTypeUtils.IMAGE_PNG, PdfUtil.image2Resource(img));
    }

    public static List<Media> toMedia(PdfDocument doc) {
        var media = new ArrayList<Media>();
        for (int i = 0; i < doc.getNumberOfPages(); i++) {
            media.add(new Media(MimeTypeUtils.IMAGE_PNG, 
                    PdfUtil.image2Resource(doc.getPageAsImage(i))));
        }
        return media;
    }
}
