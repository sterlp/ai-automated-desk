package org.sterl.ai.desk.summarise;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;

import lombok.Data;

@Data
public class DocumentInfo {
    // The sender's or issuing company/organization name. never a number alone, but may contain a number in the zip.
    // This is maybe also the creator of the document. This should never be null.
    private String from;
    // The receiver’s company/organization name. never a number alone, but may contain a number in the zip.
    // This could be null, if the document has no receiver.
    private String to;
    
    // The date of the letter or document (use ISO format as Java LocalDate: YYYY-MM-DD).
    // Verify the dates you find, it should be the date of the document itself.
    // If no date is in the document return JSON null.
    private LocalDate date;
    // The type of document (e.g., Rechnung, Mahnung, Lieferschein, Abrechnung, Versicherungsrechnung, etc.), or any other type
    // This is categorization of the document,what it is. It should never be null. 
    private String documentType;
    // Any invoice number, reference number, policy number, Rechnungsnummer, Rechnung Nr., or other identifier. Return only the document ID wihtout any prefix or other text. Usually this is a number or alpha number sequience of charaters
    // Some document may even have a "Betreff:" return then the value after this string or the representation in the corresponding language 
    // not all documents include a document number, if none is found return null.
    private String documentNumber;
    /**
     Summarize the document and the informations in one sentence, what is this document about.
     Focus on the key facts and numbers in the document.
     */
    private String summary;
    /**
    A short title, either directly from the document, if available, or create one max 200 characters
    */
    private String title;
    /**
    Generate a concise, unique, and descriptive file name for this document following these rules:
                
    1. Begin with the document date (if available) in YYYY-MM-dd format.
    2. Add a short category or topic describing the document’s main subject.
    3. Include the document number (if available).
    4. Add the creator/sender name.
    5. Add the receiver name (if available).
    6. Include any other key identifiers that make the file name unique and meaningful.
    7. Keep the total length under 120 characters.
    8. Ensure the name clearly conveys the document’s purpose and content without ambiguity.
    9. Verify that all included elements are accurate, relevant, and unique to this document.
     */
    private String fileName;
    
    public String toFileName() {
        var result = new ArrayList<String>();
        
        if (date != null) result.add(date.toString());
        if (documentType != null) result.add(documentType);
        if (documentNumber != null) result.add(documentNumber);
        if (from != null) result.add(from);
        if (to != null) result.add(to);

        return cleanFileName(String.join(" ", result));
    }
    
    public static String cleanFileName(String value) {
        var resultString = value.replace(File.separatorChar, '_');
        resultString = resultString.replace('|', Character.MIN_VALUE);
        resultString = resultString.replace('$', Character.MIN_VALUE);
        resultString = resultString.replace('#', Character.MIN_VALUE);
        resultString = resultString.replace('?', Character.MIN_VALUE);
        resultString = resultString.replace('*', Character.MIN_VALUE);
        return resultString;
    }
}