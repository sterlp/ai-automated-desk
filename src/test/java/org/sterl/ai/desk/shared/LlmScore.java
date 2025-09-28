package org.sterl.ai.desk.shared;

public class LlmScore {
    int total = 0;
    int points = 0;
    
    public void containsNot(int score, String source, String vaue) {
        add(!hasValue(source, vaue), score);
    }
    
    /**
     * Ensure not one of the given values is in the given string.
     * 
     * Breaks on the first negative score. Grants one positive, of n one found.
     */
    public void containsNotAny(String source, String... values) {
        for (String value : values) {
            if (hasValue(source, value)) {
                add(false, 1);
                return;
            }
        }
        add(true, 1);
    }
    
    /**
     * Negative score if the value is found
     */
    public void containsNot(String source, String... values) {
        for (String value : values) {
            add(!hasValue(source, value), 1);
        }
    }

    public void contains(String source, String vaue) {
        contains(1, source, vaue);
    }
    public void contains(int score, String source, String vaue) {
        add(hasValue(source, vaue), score);
    }
    /**
     * Rates any found value with one point
     */
    public void contains(String source, String vaue, String... others) {
        contains(1, source, vaue, others);
    }
    /**
     * Rates any found value with the given points
     */
    public void contains(int points, String source, String vaue, String... others) {
        contains(source, vaue);
        for (String v : others) {
            contains(points, source, v);
        }
    }
    public void containsAny(int points, String fileName, String... values) {
        for (String v : values) {
            if (hasValue(fileName, v)) {
                add(true, points);
                return;
            }
        }
        add(false, points);
    }
    public void add(boolean score) {
        add(score, 1);
    }
    public void add(boolean score, int addScore) {
        total += addScore;
        if (score) points += addScore;
    }
    
    public String toString() {
        return points + "/" + total + " = " + (int)( (float)points / (float)total * 100f) + "%";
    }
    
    boolean hasValue(String source, String value) {
        if (Strings.isBlank(source)) return false;
        return source.toLowerCase().contains(value);
    }
}
