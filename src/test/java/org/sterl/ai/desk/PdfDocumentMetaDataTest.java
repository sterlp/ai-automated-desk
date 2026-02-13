package org.sterl.ai.desk;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Test;
import org.sterl.ai.desk.pdf.PdfDocument;

class PdfDocumentMetaDataTest {

    private final String metadataPrefix = "metadata.";
    
    @Test
    void test() {
        var key = metadataPrefix + "foo";
        
        assertThat(key.substring(key.indexOf("."))).isEqualTo("foo");
        assertThat(key.substring(metadataPrefix.length())).isEqualTo("foo");
    }
    
    @Test
    void testPdfAttributes() throws IOException {
        var pdfAttributes = new File("./src/test/resources/Musterrechnung_ocr.pdf");

        if (!pdfAttributes.isFile()) {
            pdfAttributes.createNewFile();
            final PDDocument doc = new PDDocument();
            var page = new PDPage();
            doc.addPage(page);
            PDPageContentStream contentStream = new PDPageContentStream(doc, page);
            contentStream.beginText();
            contentStream.newLineAtOffset(10, 10);
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.TIMES_BOLD), 12);
            contentStream.showText("Hallo World");
            contentStream.endText();
            contentStream.close();
            
            try (var pdf = new PdfDocument(pdfAttributes, doc)) {
                var info = pdf.getDocument().getDocumentInformation();
                info.setAuthor("Author");
                info.setCreationDate(Calendar.getInstance());
                info.setCreator("Creator");
                info.setCustomMetadataValue("foo", "bar");
                info.setProducer("Producer");
                info.setSubject("Subject");
                info.setTitle("title");
                pdf.getDocument().setDocumentInformation(info);
                pdf.save();
            }
        }
        
        
        try (var pdf = new PdfDocument(pdfAttributes)) {
            var info = pdf.getDocument().getDocumentInformation();
            System.err.println("1. " + info.getAuthor());
            System.err.println("2. " + info.getCreator());
            System.err.println("3. " + info.getCustomMetadataValue("foo"));
            System.err.println("4. " + info.getProducer());
            System.err.println("5. " + info.getSubject());
            System.err.println("6. " + info.getTitle());
        }
    }
}
