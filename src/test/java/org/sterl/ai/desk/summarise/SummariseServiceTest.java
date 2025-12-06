package org.sterl.ai.desk.summarise;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
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
import org.sterl.ai.desk.shared.FileHelper;
import org.sterl.ai.desk.shared.LlmScore;
import org.sterl.ai.desk.shared.LlmStatistics;
import org.sterl.ai.desk.shared.LlmStatistics.LlmStatistic;
import org.sterl.ai.desk.shared.LlmTestDocument;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SummariseServiceTest extends AbstractSpringTest {

    @Autowired
    private SummariseService subject;
    
    final static LlmTestDocument HOTEL_STERN_MUSTER_RECHNUNG = 
            new LlmTestDocument("Rechnung Hotel Stern", "/Musterrechnung_ocr.pdf") {
        
        public LlmScore score(PdfDocument doc) {
            var fileName = doc.getFile().getName();
            var score = new LlmScore();

            score.contains(2, fileName, "2013-", "-12-", "-31");
            score.contains(3, fileName, "rechnung", "207581"); // error in OCR
            score.contains(fileName, "Hotel", "Gasthof", "Stern");
            score.containsNot(fileName, ".", ".pdf");
            
            score.contains(2, fileName, "stern", "207581");
            
            var infos = doc.getDocumentInformation();
            score.contains(infos.getCreator(), "SoCon", "GmbH", "Stern", "Pfaffenhausen");
            score.contains(infos.getTitle(), "Hotel", "Rechnung", "Gasthof", "Stern", "207581");
            score.contains(infos.getSubject(), 
                    "Rechnung", "Gasthof", "Stern", "207581", "SEPA",
                    "01.12.2013", "31.12.2013", "Rechnungsnummer", "Betrag", 
                    "Rabatt", "10",
                    "701,68", " EUR");
            
            score.containsNotAny(infos.getSubject(), "Invoice", "expenses");
            score.containsNot(infos.getTitle(), "Invoice");
            
            return score;
        }
    };
    
    final static LlmTestDocument LIDL_RECHNUNG = 
            new LlmTestDocument("LIDL Rechnung", "/kassenzettel_lidl_ocr_done.pdf") {
        
        public LlmScore score(PdfDocument doc) {
            var fileName = doc.getFile().getName();
            var score = new LlmScore();

            score.contains(2, fileName,"2018-", "-02-", "-16");
            score.contains(3, fileName, "LIDL");
            score.containsAny(3, fileName, "Kundenbeleg", "Rechnung");
            score.containsNot(fileName, ".", "0978", "182607");
            
            var infos = doc.getDocumentInformation();
            score.contains(infos.getCreator(), "LIDL", "Heegermühlerstr. 1", "16225", "Eberswalde");
            
            score.contains(infos.getTitle(), "LIDL", "6,38", "16.02.2018");
            score.containsAny(1, infos.getTitle(), "Rechnung", "Kundenbeleg");
            score.containsNot(1, infos.getTitle(), "Lieferschein");
            score.containsNot(1, infos.getTitle(), "Receipt");
            
            score.contains(infos.getSubject(), "LIDL", "6,38", "EUR", "Einkäufe", "Kartenzahlung", 
                    "16.02.2018", "15:25");
            score.containsAny(1, infos.getSubject(), "Rechnung", "Kundenbeleg");
            
            return score;
        }
    };
    
    static List<LlmTestDocument> TEST_DOCS = Arrays.asList(
            HOTEL_STERN_MUSTER_RECHNUNG,
            LIDL_RECHNUNG
        );

    @BeforeAll
    static void beforeAll() throws IOException {
        initHeader(Stream.concat(
                Stream.of("LLM"),
                TEST_DOCS.stream().map(h -> h.getName())
            )
        );
    }
    
    @BeforeEach
    void before() {
        subject.setLlmModel(null);
    }
    
    /*
     * gemma3:4b                   a2af6cc3eb7f    3.3 GB    11 days ago    
llama3.1:latest             46e0c10c039e    4.9 GB    2 weeks ago    
qwen3:8b                    500a1f067a9f    5.2 GB    2 weeks ago    
llama3.1:8b                 46e0c10c039e    4.9 GB    3 weeks ago    
    @ValueSource(strings = {
            "qwen3:4b", "qwen3:8b", "qwen3:14b",
            "gemma3:4b", "gemma3:12b",
            "mistral:7b",
            "granite3.3:8b",
            "llama3.1:8b",
            "deepseek-r1:14b",
            "gpt-oss:20b"}) // 
     */
    @ValueSource(strings = {
            "qwen3:4b", "qwen3:8b",
            "gemma3:4b", "gemma3:12b",
            "mistral:7b",
            "llama3.1:8b",
            "granite3.3:8b"
        }
    )

    @ParameterizedTest
    void test_benchmark_LLM(String llm) throws Exception {
        // GIVEN
        var destination = Path.of(PDF_OUT_DIR, FileHelper.cleanFileName(llm)).toFile();
        if (!destination.exists()) destination.mkdirs();

        // AND init model
        subject.setLlmModel(llm);
        try {
            subject.summarise("This is a test file description, return file name: " + LocalDate.now() + "-test_" + llm);
        } catch(Exception ignore) {}
        
        var docStats = new LinkedHashMap<String, LlmStatistics>();
        for (var doc : TEST_DOCS) {
            // THEN
            var llmStats = new LlmStatistics();
            docStats.put(doc.getName(), llmStats);
            final long start = System.currentTimeMillis();
            try {
                var result = subject.summariseAndNamePdf(doc.getPdfFile(), destination);
                var resultFile = result.result();
                
                // THEN check file name
                
                try (var pdf = new PdfDocument(resultFile)) {
                    var score = doc.score(pdf);
                    llmStats.addScore(llm, score);
                    llmStats.addTime(llm, result.timeInMs());
                    llmStats.add(llm, pdf);
                }
            } catch (Exception e) {
                llmStats.getStats().add(new LlmStatistic(llm, "Error", e.getMessage()));
                llmStats.addTime(llm, System.currentTimeMillis() - start);
                log.error("LLM {} failed {}", llm, doc.getName(), e);
            } finally {
                log.info("{} finished {} in {}ms", llm, doc.getName(), (System.currentTimeMillis() - start));
            }
        }
        
        var row = new ArrayList<String>();
        row.add(llm);
        for (var s : docStats.values()) {
            var score = s.getScore();
            row.add(score.isPresent() ? score.get().value() : "failed");
        }
        SUMMARY_TABLE.addRow(row);

        for (var e : docStats.entrySet()) {
            System.err.println("## " + e.getKey());
            e.getValue().print(System.err);
            
            FileHelper.appendLine(BENCHMARK_FILE.toFile(), "## " + e.getKey());
            FileHelper.appendLine(BENCHMARK_FILE.toFile(), e.getValue().toString());
        }
    }

    @Test
    void test_Musterrechnung_AI() throws Exception {
        var pdfResource = new ClassPathResource("/Musterrechnung_ocr.pdf");
        var images = PdfUtil.generateImages(pdfResource, 300);

        subject.setLlmModel("gemma3:4b");
        try (var pdf = new PdfDocument(pdfResource.getFile())) {
            var text = subject.read(images);
            System.err.println(text);
            var summerize = subject.summarise(text.result());
            System.err.println(summerize);
        }
    }
    
    @Test
    void test_LIDL_Rechnung_AI() throws Exception {
        var pdfResource = new ClassPathResource("/kassenzettel_lidl_ocr_done.pdf");
        var images = PdfUtil.generateImages(pdfResource, 300);

        try (var pdf = new PdfDocument(pdfResource.getFile())) {
            var summerize = subject.summarise(images, pdf.readText());
            
            System.err.println(summerize);
        }
    }

}
