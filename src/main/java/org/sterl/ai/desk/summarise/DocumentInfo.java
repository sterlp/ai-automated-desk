package org.sterl.ai.desk.summarise;

import java.time.LocalDate;
import java.util.ArrayList;

import lombok.Data;

@Data
public class DocumentInfo {
    private String from;
    private String to;
    
    private LocalDate date;
    private String documentType;
    private String documentNumber;
    private String summary;
    
    public String toFileName() {
        var result = new ArrayList<String>();
        
        if (date != null) result.add(date.toString());
        if (documentType != null) result.add(documentType);
        if (documentNumber != null) result.add(documentNumber);
        if (from != null) result.add(from);
        if (to != null) result.add(to);

        return String.join("_", result);
    }
}