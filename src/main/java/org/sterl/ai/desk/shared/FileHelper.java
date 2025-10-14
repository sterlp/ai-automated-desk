package org.sterl.ai.desk.shared;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.util.Objects;

import org.apache.commons.io.FilenameUtils;

public class FileHelper {

    public static BasicFileAttributeView readFileAttributeView(Path source) throws IOException {
        // LinkOption.NOFOLLOW_LINKS?
        try {
            return Files.getFileAttributeView(source, PosixFileAttributeView.class);
        } catch (SecurityException ignored) {
            // okay to continue if RuntimePermission("accessUserInformation") not granted
            return Files.getFileAttributeView(source, BasicFileAttributeView.class);
        }
    }
    public static BasicFileAttributes readFileAttributes(Path source) throws IOException {
        try {
            return Files.readAttributes(source, PosixFileAttributes.class);
        } catch (SecurityException ignored) {
            // okay to continue if RuntimePermission("accessUserInformation") not granted
            return Files.readAttributes(source, BasicFileAttributes.class);
        }
    }
    
    public static void copyAttributes(Path source, Path target) throws IOException {
        var sourceAttrs = readFileAttributes(source);
        setAttributes(sourceAttrs, target);
    }
    
    public static void setAttributes(BasicFileAttributes sourceAttrs, Path target) throws IOException {
        BasicFileAttributeView targetView = readFileAttributeView(target);
        targetView.setTimes(sourceAttrs.lastModifiedTime(),
                         sourceAttrs.lastAccessTime(),
                         sourceAttrs.creationTime());

        if (sourceAttrs instanceof PosixFileAttributes sourcePosixAttrs &&
            targetView  instanceof PosixFileAttributeView targetPosixView) {
            try {
                targetPosixView.setPermissions(sourcePosixAttrs.permissions());
            } catch (SecurityException ignored) {
                // okay to continue if RuntimePermission("accessUserInformation") not granted
            }
        }
    }

    public static String cleanFileName(String value) {
        if (value == null || value.isBlank())
            return value;
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
                if (subDir.startsWith(File.separator))
                    result += subDir;
                else
                    result += File.separatorChar + subDir;
            }
        }

        if (result.charAt(result.length() - 1) == File.separatorChar)
            return result;
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
