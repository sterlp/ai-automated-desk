package org.sterl.ai.desk.rag;

import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.tool.method.MethodToolCallback;
import org.springframework.ai.tool.support.ToolDefinitions;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ReflectionUtils;
import org.sterl.ai.desk.AbstractSpringTest;
import org.sterl.ai.desk.file_reader.FileReadService;
import org.sterl.ai.desk.shared.FileHelper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class RagServiceTest extends AbstractSpringTest {

    @Autowired
    RagService ragService;
    @Autowired
    FileReadService fileReadService;
    @Autowired
    ChatClient.Builder chatBuilder;
    @Autowired
    private VectorStore vectorStore;
    
    @Test
    void test_index_docs() {
        var stream = fileReadService.read(Path.of("./src/test/resources"), f -> {
            var props = FileHelper.uniqueFileMatchMetaData(f);
            props = props.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> "metadata." + e.getKey(),
                        Map.Entry::getValue
                ));
            
            var shouldIndex = ragService.isKnown(props) ? false : true;
            if (!shouldIndex) log.info("{} {} already indexed", f, props);
            return shouldIndex;
        });
        ragService.index(stream);
    }
    
    @Test
    void testRagSearch() throws Exception {
        var docs = vectorStore.similaritySearch(SearchRequest.builder()
                .query("""
                       Welches Interface hat das embedding model?
                       """)
                .similarityThreshold(0.7).topK(5)
                .build());
        
        for (var document : docs) {
            System.err.println(document.getMetadata());
        }
    }
    
    //@Tool(description = "RAG access to the personal documents of the user using a similarity search.")
    public String getRagData(
            @ToolParam(required = true, description = "The document RAG folder to use. Possible values: Arbeit, Brief, Arbeitsvertrag")
            String documentFolder, 
            String query) {
        System.err.println("AI does search: " + documentFolder + " -> " + query);
        var docs = vectorStore.similaritySearch(SearchRequest.builder()
                .query(query)
                .similarityThreshold(0.7).topK(5)
                .build());
        
        if (docs.isEmpty()) return "Nothing found!";
        
        String result = "";
        for (var document : docs) {
            result += document.getFormattedContent() + "\n----------------\n";
        }
        return result;
    }
    @Test
    void testRagChat() throws Exception {
        var method = ReflectionUtils.findMethod(this.getClass(), "getRagData", String.class, String.class);
        var toolCallback = MethodToolCallback.builder()
            .toolDefinition(ToolDefinitions.builder(method)
                    .description("RAG query the user documents using a similarity search.")
                    .build())
            .toolMethod(method)
            .toolObject(this)
            .build();
        
        System.err.println(toolCallback.getToolDefinition());
        System.err.println(toolCallback.getToolMetadata());
        
        var r = chatBuilder
                .build()
            .prompt()
            .toolCallbacks(toolCallback)
            .system("""
                    You are a helpful assitent for Paul. 
                    Use always language of the user query.
                    Use a short response containing all information but ensures that the user has not to read to much text.
                    Be precise.
                    The result will be displayed in an view which supports markdown.
                    """)
            .user("""
                Welches Interface hat das embedding model?
                """)
            .options(OllamaChatOptions.builder()
                .model("gpt-oss:latest")
                .thinkMedium()
                .build())
            .call();
        
        String c = r.content();
        System.err.println(c);
    }
}
