package org.sterl.ai.desk.shared;

import java.io.File;
import java.util.Objects;

public class FileHelper {

    public static void assertIsFile(File file) {
        Objects.requireNonNull(file, "File cannot be null!");
        if (!file.exists()) {
            throw new IllegalArgumentException(file.getAbsolutePath() + " does not exisit!");
        }
        if (!file.isFile()) {
            throw new IllegalArgumentException(file.getAbsolutePath() + " is not a file!");
        }
    }
}
