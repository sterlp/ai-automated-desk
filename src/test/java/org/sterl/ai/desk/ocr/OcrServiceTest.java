package org.sterl.ai.desk.ocr;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.sterl.ai.desk.metric.MetricService;
import org.sterl.ai.desk.pdf.PdfDocument;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class OcrServiceTest {

    private OcrService subject = new OcrService(new MetricService(new SimpleMeterRegistry()));

    @Test
    void testOcrCreateTime() throws Exception {
        System.err.println(new ClassPathResource("/Musterrechnung.pdf").getFile().getAbsoluteFile());
    }
    @Test
    void testOcr() throws Exception {
        // GIVEN
        var pdf = new File(getClass().getResource("/Musterrechnung.pdf").toURI());
        assertThat(pdf.isFile()).isTrue();

        // WHEN
        var result = subject.ocrPdfIfNeeded(pdf);
        
        // THEN
        assertThat(result.ocrDone()).isTrue();
        try (var pdfOcr = new PdfDocument(result.out())) {
            assertThat(pdfOcr.readText())
                .contains("Hotel-Gasthof", "4158458458", "1851139172105", "DEXXXXIOXKXXOOKKXXXE2B8");
        }
        assertThat(result.ocrDone()).isTrue();
        FileUtils.delete(result.out());
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
