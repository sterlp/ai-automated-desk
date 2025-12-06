package org.sterl.ai.desk.shared;

import java.time.Duration;

import org.springframework.ai.chat.model.ChatResponse;

public class AIHelper {

    public static long modelTime(ChatResponse result, long defaultMs) {
        var totalTime = result.getMetadata().get("total-duration");
        var loadTime = result.getMetadata().get("load-duration");
        if (totalTime instanceof Duration t && loadTime instanceof Duration l) {
            return t.minus(l).toMillis();
        }
        return defaultMs;
    }
}
