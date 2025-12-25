package org.sterl.ai.desk.summarise.mode;

public record AiResult<T> (long timeInMs, String model, T result) {

}
