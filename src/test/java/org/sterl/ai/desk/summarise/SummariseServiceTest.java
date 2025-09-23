package org.sterl.ai.desk.summarise;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.sterl.ai.desk.AiAsserts;
import org.sterl.ai.desk.pdf.PdfDocument;
import org.sterl.ai.desk.pdf.PdfUtil;

@SpringBootTest
class SummariseServiceTest {

    @Autowired
    private SummariseService subject;

    @Test
    void testUsePdfText() throws Exception {
        var pdfFile = new ClassPathResource("/Musterrechnung_ocr.pdf").getFile();
        try (var pdf = new PdfDocument(pdfFile)) {
            var text = pdf.readText();
            var summerize = subject.summarise(text);
            
            System.err.println(text);
            System.err.println("----");
            System.err.println(summerize);
            System.err.println(summerize.toFileName());
            
            AiAsserts.assertContains(summerize.getFrom(), "Hotel", "Gasthof");
            AiAsserts.assertContains(summerize.getDocumentType(), "Rechnung");
            
            assertThat(summerize.getDocumentNumber()).isEqualTo("207581");
            assertThat(summerize.getDate()).isEqualTo(LocalDate.parse("2013-12-31"));
        }
    }
    
    //@Test
    void testUseAiOcr() throws Exception {
        var pdfResource = new ClassPathResource("/Musterrechnung_ocr.pdf");
        var images = PdfUtil.generateImages(pdfResource, 300);
            
        var summerize = subject.summarise(images);
        
        System.err.println(summerize);
        System.err.println(summerize.toFileName());
        
        AiAsserts.assertContains(summerize.getFrom(), "Hotel", "Gasthof");
        AiAsserts.assertContains(summerize.getDocumentType(), "Rechnung");
        
        assertThat(summerize.getDocumentNumber()).isEqualTo("207581");
        assertThat(summerize.getDate()).isEqualTo(LocalDate.parse("2013-12-31"));
    }

}
