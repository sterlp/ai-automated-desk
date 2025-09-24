package org.sterl.ai.desk.pdf;

import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImage;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.lang.NonNull;
import org.sterl.ai.desk.shared.Strings;
import org.sterl.ai.desk.summarise.DocumentInfo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PdfDocument implements Closeable {
    
    private final PDDocument document;
    private final PDFRenderer renderer;
    @Getter
    private final File file;
    
    public PdfDocument(File file) {
        if (!file.isFile()) throw new IllegalArgumentException("Not a file " + file.getAbsolutePath());
        this.file = file;
        try (var s = new FileInputStream(file)) {
            document =  Loader.loadPDF(new RandomAccessReadBuffer(s));
            renderer = new PDFRenderer(document);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public PdfDocument(File file, PDDocument document) {
        this.file = file;
        this.document = document;
        this.renderer = new PDFRenderer(document);
    }

    public String readText(int page) {
        PDFTextStripper stripper = new PDFTextStripper();

        stripper.setStartPage(page + 1);
        stripper.setEndPage(page + 1);
        try {
            return stripper.getText(document).trim();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    @NonNull
    public String readText() {
        try {
            var stripper = new PDFTextStripper();
            stripper.setStartPage(1);
            stripper.setEndPage(getNumberOfPages() + 1);
            return stripper.getText(document).trim();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public BufferedImage getPageAsImage(int page) {
        try {
            return renderer.renderImageWithDPI(page, 300, ImageType.RGB);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public PDPage getPage(int pageIndex) {
        return document.getPage(pageIndex);
    }

    public PDPageTree getPages() {
        return document.getPages();
    }

    int pages = -1;
    public int getNumberOfPages() {
        if (pages < 0) pages = document.getNumberOfPages();
        return pages;
    }

    @Override
    public void close() throws IOException {
        document.close();
    }

    public void set(DocumentInfo fileMetaData) {
        var info = document.getDocumentInformation();
        if (Strings.notBlank(fileMetaData.getFrom())) info.setCreator(fileMetaData.getFrom());
        if (Strings.notBlank(fileMetaData.getTitle())) info.setTitle(fileMetaData.getTitle());
        if (Strings.notBlank(fileMetaData.getSummary())) info.setSubject(fileMetaData.getSummary());
        document.setDocumentInformation(info);
        save();
    }

    public File save() {
        try {
            document.save(file);
            return file;
        } catch (IOException e) {
            throw new RuntimeException("Filed to save PDF to: " + file.getAbsolutePath(), e);
        }
    }

    /**
     * Adds an image to the PDF, supported files:
     * 
     * <pre>
     * jpg jpeg tif tiff png gif bmp png
     * </pre>
     */
    public void addImage(File image) {
        try {
            var pdImage = PDImageXObject.createFromFileByExtension(image, document);
            var imageRect = Rect.of(pdImage);

            // Decide if we should rotate the page to landscape
            var pageSize = imageRect.isLandscape()
                    ? new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth())
                    : PDRectangle.A4;

            var pdPage = new PDPage(pageSize);
            document.addPage(pdPage);

            try (var contentStream = new PDPageContentStream(document, pdPage)) {
                var scaledDim = imageRect.scaleTo(Rect.of(pageSize));

                // Center the image on the page
                float x = (pageSize.getWidth() - scaledDim.width()) / 2;
                float y = (pageSize.getHeight() - scaledDim.height()) / 2;

                contentStream.drawImage(pdImage, x, y, scaledDim.width(), scaledDim.height());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to add image: " + image.getAbsolutePath(), e);
        }
    }
    
    public record Rect(float width, float height) {
        public static Rect of(PDImage in) {
            return new Rect(in.getWidth(), in.getHeight());
        }
        public static Rect of(PDRectangle in) {
            return new Rect(in.getWidth(), in.getHeight());
        }
        
        public boolean isLandscape() {
            return width > height;
        }
        /**
         * Scales this rect to fit the given boundary
         * 
         * @return the scaled rect
         */
        public Rect scaleTo(Rect boundary) {
            float originalWidth = this.width();
            float originalHeight = this.height();
            float boundWidth = boundary.width();
            float boundHeight = boundary.height();
            float newWidth = originalWidth;
            float newHeight = originalHeight;

            if (newWidth > boundWidth) {
                newWidth = boundWidth;
                newHeight = newWidth * originalHeight / originalWidth;
            }
            if (newHeight > boundHeight) {
                newHeight = boundHeight;
                newWidth = originalWidth * newHeight / originalHeight;
            }
            return new Rect(newWidth, newHeight);
        }
    }
}
