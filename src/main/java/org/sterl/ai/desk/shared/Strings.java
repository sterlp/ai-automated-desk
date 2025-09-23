package org.sterl.ai.desk.shared;

public class Strings {

    public static boolean notBlank(String value) {
        return !isBlank(value);
    }
    public static boolean isBlank(String value) {
        if (value == null) return true;
        if (value.length() == 0) return true;
        if (value.strip().length() == 0) return true;
        return false;
    }
}
