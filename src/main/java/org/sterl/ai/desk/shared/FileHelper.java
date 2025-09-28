package org.sterl.ai.desk.shared;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

import org.apache.commons.io.FilenameUtils;

public class FileHelper {
    
    public static String cleanFileName(String value) {
        if (value == null || value.isBlank()) return value;
        var result = value.replace('/', '_');
        result = result.replace('\\', '_');
        result = result.replace(':', '_');
        result = result.replace('|', Character.MIN_VALUE);
        result = result.replace('$', Character.MIN_VALUE);
        result = result.replace('#', Character.MIN_VALUE);
        result = result.replace('?', Character.MIN_VALUE);
        result = result.replace('*', Character.MIN_VALUE);
        result = result.replace('\n', Character.MIN_VALUE);
        result = result.replace('\r', Character.MIN_VALUE);
        result = result.replace('\t', Character.MIN_VALUE);

        return result;
    }

    public static void assertIsFile(File file) {
        Objects.requireNonNull(file, "File cannot be null!");
        if (!file.exists()) {
            throw new IllegalArgumentException(file.getAbsolutePath() + " does not exisit!");
        }
        if (!file.isFile()) {
            throw new IllegalArgumentException(file.getAbsolutePath() + " is not a file!");
        }
    }
    
    /**
     * Returns the destination directory including the separator char 
     */
    public static String toDestinationDir(File file, File sourceDir, File destinationDir) {
        var s = FilenameUtils.getFullPath(file.getAbsolutePath());
        
        String result = destinationDir.getAbsolutePath();

        if (s.contains(sourceDir.getAbsolutePath())) {
            var subDir = s.replace(sourceDir.getAbsolutePath(), "");
            if (subDir.length() > 0) {
                if (subDir.startsWith(File.separator)) result += subDir;
                else result += File.separatorChar + subDir;
            }
        }

        if (result.charAt(result.length() - 1) == File.separatorChar) return result;
        return result + File.separatorChar;
    }

    public static void append(Path f, String text) {
        append(f.toFile(), text);
    }
    public static void append(File f, String text) {
        try {
            Files.writeString(f.toPath(), text, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void appendLine(File f, String text) {
        append(f, text + '\n');
    }
}
