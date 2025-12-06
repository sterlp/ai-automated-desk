package org.sterl.ai.desk.summarise;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.sterl.ai.desk.AbstractSpringTest;
import org.sterl.ai.desk.pdf.PdfUtil;

class ReadDocumentAgentTest extends AbstractSpringTest {
    @Autowired
    private ReadDocumentAgent subject;

    @Test
    void test_Musterrechnung_AI() throws Exception {
        var pdfResource = new ClassPathResource("/Musterrechnung_ocr.pdf");
        var images = PdfUtil.generateImages(pdfResource, 300);

        var text = subject.read(images);
        System.err.println(text.result());
    }
    
    @Test
    void test_LIDL_Rechnung_AI() throws Exception {
        var pdfResource = new ClassPathResource("/kassenzettel_lidl_ocr_done.pdf");
        var images = PdfUtil.generateImages(pdfResource, 300);

        var summerize = subject.read(images);
        System.err.println(summerize.result());
    }
}
