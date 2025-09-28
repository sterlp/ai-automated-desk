package org.sterl.ai.desk.summarise.mode;

public record AiResult<T> (long timeInMs, T result) {

}
