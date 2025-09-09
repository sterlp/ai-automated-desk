package org.sterl.ai.desk.summarise;

import java.time.LocalDate;

public record DocumentInfo (String from, String to, 
        LocalDate date, String documentType, String documentNumber, String reason, String summary) {
}