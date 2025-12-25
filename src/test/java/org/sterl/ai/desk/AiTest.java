package org.sterl.ai.desk;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.beans.factory.annotation.Autowired;

class AiTest extends AbstractSpringTest {

    @Autowired
    private ChatClient.Builder b;
    
    @Test
    void test() {
        ChatClient chatClient = b.build();
     // Generate variants of the same request
        String orderVariants = chatClient
                .prompt("""
                        We have a band merchandise t-shirt webshop, and to train a
                        chatbot we need various ways to order: "One Metallica t-shirt
                        size S". Generate 10 variants, with the same semantics but keep
                        the same meaning.
                        """)
                .options(ChatOptions.builder()
                        .temperature(1.0)  // High temperature for creativity
                        .build())
                .call()
                .content();

        // Evaluate and select the best variant
        String output = chatClient
                .prompt()
                .options(OllamaChatOptions.builder()
                        .build())
                .user(u -> u.text("""
                        Please perform BLEU (Bilingual Evaluation Understudy) evaluation on the following variants:
                        ----
                        {variants}
                        ----

                        Select the instruction candidate with the highest evaluation score.
                        """).param("variants", orderVariants))
                .call()
                .content();
        
        System.err.println(output);
    }

}
