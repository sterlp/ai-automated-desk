package org.sterl.ai.desk.file_name;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class FileNameServiceTest {

    @Test
    void test() {
        System.err.println(Path.of("foo", "bar").toFile().getAbsolutePath());
    }
}
