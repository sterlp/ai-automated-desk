package org.sterl.ai.desk;

import static org.assertj.core.api.Assertions.*;

public class AiAsserts {

    public static void assertContains(String source, String value) {
        assertThat(source).contains(value);
    }
}
