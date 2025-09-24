package org.sterl.ai.desk.shared;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class RectTest {

    @Test
    void test_iNotLandscape() throws Exception {
        var document = new PDDocument();
        var image = new ClassPathResource("/A4_Landesjustizkasse_Mainz_vertical.jpg").getFile();
        var pdImage = PDImageXObject.createFromFileByExtension(image, document);
        var imageRect = Rect.of(pdImage);
        
        assertThat(imageRect.isLandscape()).isFalse();
    }
    
    @Test
    void test_isLandscape() throws Exception {
        var document = new PDDocument();
        var image = new ClassPathResource("/A4_Landesjustizkasse_Mainz_horizontal.jpg").getFile();
        var pdImage = PDImageXObject.createFromFileByExtension(image, document);
        var imageRect = Rect.of(pdImage);
        
        assertThat(imageRect.isLandscape()).isTrue();
    }

}
