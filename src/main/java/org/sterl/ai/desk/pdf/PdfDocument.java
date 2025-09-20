package org.sterl.ai.desk.pdf;

import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.lang.NonNull;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PdfDocument implements Closeable {
    
    private final PDDocument document;
    private final PDFRenderer renderer;
    
    public PdfDocument(File file) {
        try (var s = new FileInputStream(file)) {
            document =  Loader.loadPDF(new RandomAccessReadBuffer(s));
            renderer = new PDFRenderer(document);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public PdfDocument(InputStream doc) {
        try (doc) {
            document =  Loader.loadPDF(new RandomAccessReadBuffer(doc));
            renderer = new PDFRenderer(document);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
}
