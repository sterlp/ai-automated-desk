package org.sterl.ai.desk.shared;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.sterl.ai.desk.pdf.PdfDocument;

import lombok.Getter;

public class LlmStatistics {
    private static final String LLM_SCORE = "LLM Score";
    private static final String RUNTIME = "Runtime";
    
    private static final Set<String> AS_LIST = Set.of(LLM_SCORE, RUNTIME);

    public static record LlmStatistic(String llm, String name, String value) {
    }

    @Getter
    private List<LlmStatistic> stats = new ArrayList<>();
    
    public void addScore(String llm, LlmScore value) {
        this.stats.add(new LlmStatistic(llm, LLM_SCORE, value.toString()));
    }
    public Optional<LlmStatistic> getScore() {
        return this.stats.stream().filter(s -> LLM_SCORE.equals(s.name())).findAny();
    }
    public void addCreator(String llm, String value) {
        this.stats.add(new LlmStatistic(llm, "Creator", value));
    }
    public void addFileName(String llm, String value) {
        this.stats.add(new LlmStatistic(llm, "File name", value));
    }
    public void addSubject(String llm, String value) {
        this.stats.add(new LlmStatistic(llm, "Subject", value));
    }
    public void addTitle(String llm, String value) {
        this.stats.add(new LlmStatistic(llm, "Title", value));
    }
    public void addTime(String llm, long time) {
        this.stats.add(new LlmStatistic(llm, RUNTIME, time / 1000 + "s"));
    }
    public Optional<LlmStatistic> getTime() {
        return this.stats.stream().filter(s -> RUNTIME.equals(s.name())).findAny();
    }
    
    public void add(String llm, PdfDocument doc) {
        var info = doc.getDocumentInformation();
        addFileName(llm, doc.getFile().getName());
        addTitle(llm, info.getTitle());
        addCreator(llm, info.getCreator());
        addSubject(llm, info.getSubject());
    }
    
    @Override
    public String toString() {
        var result = new StringBuilder();
        result.append("### " + this.stats.get(0).llm).append('\n');
        
        for (var asList : AS_LIST) {
            var v = this.stats.stream().filter(s -> asList.equalsIgnoreCase(s.name())).findFirst();
            if (v.isPresent()) {
                result.append("- ").append(v.get().name())
                      .append(": ").append(v.get().value())
                      .append('\n');
            }
        }
        result.append('\n');
        for (LlmStatistic s : stats) {
            if (AS_LIST.contains(s.name())) continue; // skip list items
            result.append("#### " + s.name).append('\n');
            result.append(s.value).append('\n');
        }
        result.append('\n');
        return result.toString();
    }
    public void print(PrintStream out) {
        out.println(this);
    }
}
