package org.sterl.ai.desk.summarise;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.sterl.ai.desk.AbstractSpringTest;
import org.sterl.ai.desk.embedding.EmbeddingAgent;
import org.sterl.ai.desk.pdf.PdfUtil;
import org.sterl.ai.desk.shared.VectorHelper;

class ReadDocumentAgentTest extends AbstractSpringTest {
    @Autowired
    private ReadDocumentAgent subject;
    
    @Autowired
    private EmbeddingAgent embeddingAgent;
    
    @Test
    void test_deepseek_vs_gemma3() throws Exception {
        
        var musterrechnungMd = new ClassPathResource("musterrechnung.md");
        var sollVector = embeddingAgent.toVector(new String(musterrechnungMd.getContentAsByteArray()));
        
        var pdfResource = new ClassPathResource("/Musterrechnung_ocr.pdf");
        var images = PdfUtil.generateImages(pdfResource, 300);

        System.out.println("gemma3:12b");
        subject.setLlmModel("gemma3:12b");
        subject.read(images);
        var gemma = subject.read(images);
        var gemmaVector = embeddingAgent.toVector(gemma.result());
        
        System.out.println("deepseek-ocr");
        subject.setLlmModel("deepseek-ocr");
        subject.read(images);
        var deepseek = subject.read(images);
        var deepseekVector = embeddingAgent.toVector(deepseek.result());
        
        System.err.println("cosineSimilarity: gemma " + 
                VectorHelper.cosineSimilarity(gemmaVector, sollVector)
            );
        
        System.err.println("cosineSimilarity: deepseek " + 
                VectorHelper.cosineSimilarity(deepseekVector, sollVector)
            );
        
        System.err.println("cosineSimilarity: " + 
                VectorHelper.cosineSimilarity(deepseekVector, gemmaVector)
            );
    }

    @Test
    void test_Musterrechnung_AI() throws Exception {
        var pdfResource = new ClassPathResource("/Musterrechnung_ocr.pdf");
        var musterrechnungMd = new ClassPathResource("musterrechnung.md");
        var images = PdfUtil.generateImages(pdfResource, 300);

        var text = subject.read(images);
        System.err.println(text.result());
        
        var v1 = embeddingAgent.toVector(text.result());
        var vMd = embeddingAgent.toVector(new String(musterrechnungMd.getContentAsByteArray()));
        System.err.println(toList(v1));
        System.err.println(toList(vMd));
        
        System.err.println("cosineSimilarity: " + 
                VectorHelper.cosineSimilarity(v1, vMd)
            );
        System.err.println("euclideanDistance: " + 
                VectorHelper.euclideanDistance(v1, vMd)
        );
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"gemma3:12b", "deepseek-ocr"})
    void test_LIDL_Rechnung_AI(String llm) throws Exception {
        var pdfResource = new ClassPathResource("/kassenzettel_lidl_ocr_done.pdf");
        var images = PdfUtil.generateImages(pdfResource, 300);

        subject.setLlmModel(llm);
        subject.read(images);
        var summerize = subject.read(images);
        System.err.println(llm);
        System.err.println(summerize.result());
    }
    
    
    static List<Float> toList(float[] value) {
        var result = new ArrayList<Float>(value.length);
        for (var f : value) result.add(Float.valueOf(f));
        return result;
    }
}
