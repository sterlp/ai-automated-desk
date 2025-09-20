package org.sterl.ai.desk.summarise;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;

import org.apache.commons.io.FilenameUtils;

import lombok.Data;

@Data
public class DocumentInfo {
    // The sender's or issuing company/organization name. never a number alone, but may contain a number in the zip.
    private String from;
    // The receiver’s company/organization name. never a number alone, but may contain a number in the zip.
    private String to;
    
    // The date of the letter or document (use ISO format as Java LocalDate: YYYY-MM-DD).
    private LocalDate date;
    // The type of document (e.g., Rechnung, Mahnung, Lieferschein, Abrechnung, Versicherungsrechnung, etc.), or any other type
    private String documentType;
    // Any invoice number, reference number, policy number, Rechnungsnummer, Rechnung Nr., or other identifier. Return only the document ID wihtout any prefix or other text. Usually this is a number or alpha number sequience of charaters
    private String documentNumber;
    // One short sentence, which summarizes the document in the most exact way. In the language of the document.
    private String summary;
    
    public String toFileName() {
        var result = new ArrayList<String>();
        
        if (date != null) result.add(date.toString());
        if (documentType != null) result.add(documentType);
        if (documentNumber != null) result.add(documentNumber);
        if (from != null) result.add(from);
        if (to != null) result.add(to);

        var resultString = String.join(" ", result);
        resultString = resultString.replace(File.separatorChar, '_');
        resultString = resultString.replace('|', Character.MIN_VALUE);
        resultString = resultString.replace('$', Character.MIN_VALUE);
        resultString = resultString.replace('#', Character.MIN_VALUE);
        return resultString;
    }
}