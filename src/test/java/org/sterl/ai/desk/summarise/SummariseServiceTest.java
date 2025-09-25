package org.sterl.ai.desk.summarise;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.time.LocalDate;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.sterl.ai.desk.AbstractSpringTest;
import org.sterl.ai.desk.AiAsserts;
import org.sterl.ai.desk.pdf.PdfDocument;
import org.sterl.ai.desk.pdf.PdfUtil;
import org.sterl.ai.desk.shared.Strings;

class SummariseServiceTest extends AbstractSpringTest {

    @Autowired
    private SummariseService subject;
    
    @BeforeEach
    void before() {
        subject.setLlmModel(null);
    }
    
    public static class Score {
        int total = 0;
        int points = 0;
        
        public void contains(String source, String vaue) {
            if (Strings.isBlank(source)) add(false, 2);
            else add(source.toLowerCase().contains(vaue.toLowerCase()), 1);
        }
        public void contains(String source, String vaue, String... others) {
            contains(source, vaue);
            for (String v : others) {
                contains(source, v);
            }
        }
        public void add(boolean score) {
            add(score, 1);
        }
        public void add(boolean score, int addScore) {
            total += addScore;
            if (score) points += addScore;
        }
        
        public String toString() {
            return points + "/" + total + " = " + (int)( (float)points / (float)total * 100f) + "%";
        }
    }
    
    @ParameterizedTest
    @ValueSource(strings = {
            "qwen3:4b", "gemma3:4b", 
            "qwen3:8b", "llama3.1:8b", 
            "granite3.3:8b", "mistral:7b",
            "gemma3:12b"}) // "deepseek-r1:7b"
    void test_LLMs_Musterrechnung_ocr(String llm) throws Exception {
        var pdfFile = new ClassPathResource("/Musterrechnung_ocr.pdf").getFile();
        
        var destination = new File("." + File.separatorChar + llm + File.separatorChar);
        if (destination.exists()) FileUtils.cleanDirectory(destination);
        else destination.mkdirs();

        subject.setLlmModel(llm);
        
        System.err.println("## " + llm);
        var resultFile = subject.summariseAndNamePdf(pdfFile, new File("./" + llm));
        
        var score = new Score();
        score.contains(resultFile.getName(), "2013-", "-12-", "-31");
        score.contains(resultFile.getName(), "Hotel");
        score.add(resultFile.getName().toLowerCase().contains("rechnung"), 3);
        score.add(resultFile.getName().toLowerCase().contains("stern"), 2);
        score.add(resultFile.getName().contains("207581"), 2);

        try (var pdf = new PdfDocument(resultFile)) {
            var infos = pdf.getDocumentInformation();
            score.contains(infos.getCreator(), "Gasthof", "Stern", "Postfach", "86310", "Pfaffenhausen");
            score.contains(infos.getTitle(), "Rechnung", "207581");
            score.contains(infos.getSubject(), "01.12.2013", "31.12.2013", "Rechnung", "Betrag", "701,68");
        }
        System.err.println(llm + " score: " + score);
        System.err.flush();
        System.out.flush();
    }
    
    @ParameterizedTest
    @ValueSource(strings = {
            "qwen3:4b", "gemma3:4b", 
            "qwen3:8b", "llama3.1:8b", 
            "granite3.3:8b", "mistral:7b",
            "gemma3:12b"}) // "deepseek-r1:7b"
    void test_LLMs_kassenzettel_lidl(String llm) throws Exception {
        var pdfFile = new ClassPathResource("/kassenzettel_lidl_ocr_done.pdf").getFile();
        
        var destination = new File("." + File.separatorChar + llm + File.separatorChar);
        if (destination.exists()) FileUtils.cleanDirectory(destination);
        else destination.mkdirs();

        subject.setLlmModel(llm);
        
        System.err.println("## " + llm);
        var resultFile = subject.summariseAndNamePdf(pdfFile, new File("./" + llm));
        
        var score = new Score();
        score.add(resultFile.getName().toLowerCase().contains("2018-02-16"), 3);
        score.add(resultFile.getName().toLowerCase().contains("rechnung"), 3);
        score.add(resultFile.getName().toLowerCase().contains("lidl"), 3);
        try (var pdf = new PdfDocument(resultFile)) {
            var infos = pdf.getDocumentInformation();
            score.contains(infos.getCreator(), "LIDL", "Heegermühlerstr. 1", "16225", "Eberswalde");
            score.contains(infos.getTitle(), "LIDL", "Rechnung", "6,38");
            score.contains(infos.getSubject(), "LIDL", "Rechnung", "6,38", "Einkäufe", "182607");
        }
        System.err.println(llm + " score: " + score);
        System.err.flush();
        System.out.flush();
    }

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
    
    @Test
    void test_kassenzettel_lidl_ocr_done() throws Exception {
        var pdfFile = new ClassPathResource("/kassenzettel_lidl_ocr_done.pdf").getFile();
        try (var pdf = new PdfDocument(pdfFile)) {
            var text = pdf.readText();
            System.err.println(text);
            System.err.println("----");
            var summerize = subject.summarise(text);
            System.err.println(summerize);
            System.err.println(summerize.toFileName());
            
            AiAsserts.assertContains(summerize.getFrom(), "lidl");
            assertThat(summerize.getDocumentType()).containsAnyOf("Kundenbeleg", "Rechnung");
            
            AiAsserts.assertContains(summerize.getFileName(), "2018-02-16", "LIDL");
            assertThat(summerize.getFileName()).containsAnyOf("Kundenbeleg", "Rechnung");
            
            assertThat(summerize.getDate()).isEqualTo(LocalDate.parse("2018-02-16"));
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
