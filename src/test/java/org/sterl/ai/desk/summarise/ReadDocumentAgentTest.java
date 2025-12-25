package org.sterl.ai.desk.summarise;

import static org.mockito.Mockito.CALLS_REAL_METHODS;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.PathResource;
import org.sterl.ai.desk.AbstractSpringTest;
import org.sterl.ai.desk.embedding.EmbeddingAgent;
import org.sterl.ai.desk.file_reader.ReadImageAgent;
import org.sterl.ai.desk.pdf.PdfUtil;
import org.sterl.ai.desk.shared.VectorHelper;

class ReadDocumentAgentTest extends AbstractSpringTest {
    @Autowired
    private ReadImageAgent subject;
    
    @Autowired
    private EmbeddingAgent embeddingAgent;
    
    @Test
    void test_ministral3_vs_gemma3() throws Exception {
        
        var musterrechnungMd = new ClassPathResource("musterrechnung.md");
        var sollVector = embeddingAgent.toVector(new String(musterrechnungMd.getContentAsByteArray()));
        
        var pdfResource = new ClassPathResource("/Musterrechnung_ocr.pdf");
        var images = PdfUtil.generateImages(pdfResource, 300);

        System.out.println("gemma3:12b");
        subject.setLlmModel("gemma3:12b");
        subject.read(images);
        var gemma = subject.read(images);
        System.err.println(gemma);
        var gemmaVector = embeddingAgent.toVector(gemma.result());
        
        System.out.println("ministral-3:14b-instruct-2512-q8_0");
        subject.setLlmModel("ministral-3:14b-instruct-2512-q8_0");
        subject.read(images);
        var ministral3 = subject.read(images);
        System.err.println(ministral3);
        var deepseekVector = embeddingAgent.toVector(ministral3.result());
        
        System.err.println("cosineSimilarity: gemma " + 
                VectorHelper.cosineSimilarity(gemmaVector, sollVector)
            );
        
        System.err.println("cosineSimilarity: ministral3 " + 
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
    @ValueSource(strings = {"gemma3:12b", "deepseek-ocr", "ministral-3:14b-instruct-2512-q8_0"})
    void test_LIDL_Rechnung_AI(String llm) throws Exception {
        var pdfResource = new ClassPathResource("/kassenzettel_lidl_ocr_done.pdf");
        var images = PdfUtil.generateImages(pdfResource, 300);

        subject.setLlmModel(llm);
        subject.read(images);
        System.err.println(llm);
        var summerize = subject.read(images);
        System.err.println(summerize.result());
        System.err.println("-------------------");
    }
    
    
    static List<Float> toList(float[] value) {
        var result = new ArrayList<Float>(value.length);
        for (var f : value) result.add(Float.valueOf(f));
        return result;
    }
}
