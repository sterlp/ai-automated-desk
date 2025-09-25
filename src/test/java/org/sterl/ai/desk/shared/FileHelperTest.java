package org.sterl.ai.desk.shared;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.junit.jupiter.api.Test;

class FileHelperTest {

    @Test
    void test() {
        var f = new File("/foo/in/file.txt");
        var s = new File("/foo/in");
        var d = new File("/foo/out");
        
        var result = FileHelper.toDestinationDir(f, s, d);
        
        assertThat(result).isEqualTo("/foo/out/");
    }

    @Test
    void testSubDir() {
        var f = new File("/foo/in/subDir1/file.txt");
        var s = new File("/foo/in");
        var d = new File("/foo/out");
        
        var result = FileHelper.toDestinationDir(f, s, d);
        
        assertThat(result).isEqualTo("/foo/out/subDir1/");
    }
    
    @Test
    void testAnyFile() {
        var f = new File("/aa/bb/file.txt");
        var s = new File("/foo/in");
        var d = new File("/foo/out");
        
        var result = FileHelper.toDestinationDir(f, s, d);
        
        assertThat(result).isEqualTo("/foo/out/");
    }

}
