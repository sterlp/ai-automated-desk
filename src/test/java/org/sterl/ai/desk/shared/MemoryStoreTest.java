package org.sterl.ai.desk.shared;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MemoryStoreTest {

    @Test
    void test() {
        var subject = new MemoryStore<>(10);
        
        for (int i = 0; i < 100; i++) subject.store("data-" + i);
        
        assertThat(subject.size()).isEqualTo(10);
    }

}
