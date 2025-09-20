package org.sterl.ai.desk;

import static org.assertj.core.api.Assertions.*;

public class AiAsserts {

    public static void assertContains(String source, String value) {
        assertThat(source.toLowerCase()).contains(value.toLowerCase());
    }
    public static void assertContains(String source, String... values) {
        for (String value : values) {
            assertContains(source, value);
        }
    }
}
