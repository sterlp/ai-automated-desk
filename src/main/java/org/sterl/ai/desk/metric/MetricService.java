package org.sterl.ai.desk.metric;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MetricService {

    private final MeterRegistry meterRegistry;
    
    public SpringTimer timer(String name, Class<?> forClass) {
        return new SpringTimer("ai-desk." + name, LoggerFactory.getLogger(forClass)).start();
    }
    
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public class SpringTimer {
        private final String name;
        private final Logger log;

        private long startMs;
        
        public SpringTimer start() {
            startMs = System.currentTimeMillis();
            return this;
        }
        
        public long stop(Exception e) {
            var timeMs = timeEnd(e);
            log.info(e.getMessage() + " after {}ms", timeMs);
            return timeMs;
        }
        public long stop(String message) {
            var timeMs = timeEnd(null);
            log.info(message + " finished after {}ms", timeMs);
            return timeMs;
        }
        
        public long stop() {
            return timeEnd(null);
        }

        private long timeEnd(Exception e) {
            var timeMs = System.currentTimeMillis() - startMs;
            meterRegistry.timer(name, "status", 
                    e == null ? "success" : "failed").record(timeMs, TimeUnit.MILLISECONDS);
            return timeMs;
        }
    }
}
