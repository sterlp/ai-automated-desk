package org.sterl.ai.desk;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.sterl.ai.desk.shared.MkDocksTable;

@ActiveProfiles("junit")
@SpringBootTest(classes = AiDeskApplication.class)
public class AbstractSpringTest {
    
    protected final static String PDF_OUT_DIR = "llmOut";
    protected final static Path SUMMARY_FILE = Path.of("Summary.MD");
    protected final static Path BENCHMARK_FILE = Path.of("Benchmark.MD");

    protected final static MkDocksTable SUMMARY_TABLE = new MkDocksTable(SUMMARY_FILE);
    
    @BeforeAll
    public static void beforeAll() throws IOException {
        if (Path.of(PDF_OUT_DIR).toFile().exists()) {
            FileUtils.cleanDirectory(Path.of(PDF_OUT_DIR).toFile());
        }
        
        if (Files.exists(BENCHMARK_FILE)) {
            Files.writeString(BENCHMARK_FILE, "", StandardOpenOption.TRUNCATE_EXISTING);
        } else {
            BENCHMARK_FILE.toFile().createNewFile();
        }
    }
    
    @AfterAll
    public static void afterAll() {
        SUMMARY_TABLE.close();
    }

    protected static void initHeader(Stream<String> headRow) {
        SUMMARY_TABLE.setHeader(headRow.toList());
    }
}
