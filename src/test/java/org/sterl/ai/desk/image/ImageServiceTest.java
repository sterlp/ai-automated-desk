package org.sterl.ai.desk.image;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.Arrays;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.sterl.ai.desk.pdf.PdfDocument;

class ImageServiceTest {

    final ImageService subject = new ImageService();
    
    @Test
    void testOne() throws Exception {
        // given
        var image = new ClassPathResource("/kassenzettel_lidl.jpg").getFile();

        try (var pdf = new PdfDocument(new File("./kassenzettel.pdf"), new PDDocument())) {
            // when
            subject.addToPdf(pdf, image);
            
            // THEN
            assertThat(pdf.getNumberOfPages()).isOne();
        }
    }
    
    @Test
    void testSeveral() throws Exception {
        // given
        var i1 = new ClassPathResource("/kassenzettel_lidl.jpg").getFile();
        var i2 = new ClassPathResource("/kassenzettel_lidl.jpg").getFile();

        try (var pdf = new PdfDocument(new File("./kassenzettel.pdf"), new PDDocument())) {
            // when
            subject.addToPdf(pdf, Arrays.asList(i1, i2));
            // THEN
            assertThat(pdf.getNumberOfPages()).isEqualTo(2);
            //var f = pdf.save();
            //System.err.println(f.getAbsolutePath());
            //f.delete();
        }
    }
}
