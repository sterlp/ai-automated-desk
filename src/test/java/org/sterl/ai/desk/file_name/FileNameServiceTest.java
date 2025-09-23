package org.sterl.ai.desk.file_name;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

import org.junit.jupiter.api.Test;

class FileNameServiceTest {

    @Test
    void test() {
        var s = new File("/foo/in");
        var d = new File("/foo/out");
        var f = new File("/foo/in/file.txt");
        
        
        var result = FileNameService.toDestinationDir(f, s, d);
        
        assertThat(result).isEqualTo("/foo/out/");
    }

    @Test
    void testSubDir() {
        var s = new File("/foo/in");
        var d = new File("/foo/out");
        var f = new File("/foo/in/subDir1/file.txt");
        
        
        var result = FileNameService.toDestinationDir(f, s, d);
        
        assertThat(result).isEqualTo("/foo/out/subDir1/");
    }
}
