package org.sterl.ai.desk.ocr;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

class OcrServiceTest {

    private OcrService subject = new OcrService();

    @Test
    void testOcr() throws Exception {
        // GIVEN
        var pdf = new File(getClass().getResource("/Musterrechnung.pdf").toURI());
        assertThat(pdf.isFile()).isTrue();

        // WHEN
        var result = subject.ocrPdfIfNeeded(pdf);
        
        // THEN
        assertThat(result.ocrDone()).isTrue();
        FileUtils.forceDeleteOnExit(result.out());
    }
    
    @Test
    void testOcrSkip() throws Exception {
        // GIVEN
        var pdf = new File(getClass().getResource("/Musterrechnung_ocr.pdf").toURI());
        assertThat(pdf.isFile()).isTrue();

        // WHEN
        var result = subject.ocrPdfIfNeeded(pdf);
        
        // THEN
        assertThat(result.ocrDone()).isFalse();
    }

}
