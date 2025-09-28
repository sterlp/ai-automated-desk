package org.sterl.ai.desk.shared;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MkDocksTable implements Closeable {

    private final BufferedWriter writer;
    
    public MkDocksTable(Path path) {
        this(path, StandardOpenOption.TRUNCATE_EXISTING);
    }
    public MkDocksTable(Path path, StandardOpenOption option) {
        try {
            if (!Files.exists(path)) {
                path.toFile().createNewFile();
            }
            writer = Files.newBufferedWriter(path, option);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create " + path, e);
        }
    }
    
    public void setHeader(Collection<String> headerCells) {
        try {
            writeMkRow(headerCells, writer);
            
            // end the header
            writer.append("|");
            for (String h : headerCells) {
                writer.append(" ").append(StringUtils.repeat('-', h.length())).append(" |");
            }
            writer.append('\n');
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void addRow(Collection<String> rowCells) {
        try {
            writeMkRow(rowCells, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    private static void writeMkRow(Collection<String> values, BufferedWriter result) throws IOException {
        result.append("|");
        for (String h : values) {
            result.append(" ").append(h).append(" |");
        }
        result.append('\n');
        result.flush();
    }
    
    @Override
    public void close() {
        try {
            writer.flush();
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
