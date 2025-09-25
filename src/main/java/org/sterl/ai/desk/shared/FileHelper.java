package org.sterl.ai.desk.shared;

import java.io.File;
import java.util.Objects;

import org.apache.commons.io.FilenameUtils;

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
}
