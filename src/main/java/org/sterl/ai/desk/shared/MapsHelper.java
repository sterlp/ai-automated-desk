package org.sterl.ai.desk.shared;

import java.util.LinkedHashMap;

public class MapsHelper {

    public static LinkedHashMap<String, Object> toMap(Object... values) {
        if (values.length % 2 != 0) {
            throw new IllegalArgumentException("Values must be provided as key-value pairs");
        }
        var result = new LinkedHashMap<String, Object>();
        for (int i = 0; i < values.length; i += 2) {
            result.put(values[i].toString(), values[i + 1]);
        }
        return result;
    }
}
