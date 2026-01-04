package org.sterl.ai.desk.summarise;

import java.util.ArrayList;

import org.sterl.ai.desk.shared.FileHelper;
import org.sterl.ai.desk.shared.Strings;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import lombok.Data;

@Data
public class DocumentInfo {
    @JsonPropertyDescription("""
        The sender/creator or issuing company/organization name. 
        Never a number alone, but may contain a number in the zip.
        This is maybe also the creator of the document. This should never be null.
        If you find a company name and the address of the company which created the document, include both.
        """)
    private String author;
    @JsonPropertyDescription("""
        The receiver’s company/organization name. never a number alone, but may contain a number in the zip.
        This could be null, if the document has no receiver.
        A letter or invoice is typically from from author/sender to receiver.
        """)
    private String receiver;
    
    @JsonPropertyDescription("""
        The date of the letter or document use ISO format as Java LocalDate format "YYYY-MM-DD".
        Verify the dates you find more than one, this should be the date this document was created at.
        If you can't find any date return JSON null.
        """)
    private String date;
    @JsonPropertyDescription("""
        The type of document (e.g., Rechnung, Mahnung, Lieferschein, Abrechnung, Versicherungsrechnung, etc.), or any other type
        This is categorization of the document,what it is. It should never be null.
        """)
    private String documentType;
    @JsonPropertyDescription("""
        Any invoice number, reference number, policy number, Rechnungsnummer, Rechnung Nr., or other identifier. Return only the document ID wihtout any prefix or other text. Usually this is a number or alpha number sequience of charaters
        Some document may even have a "Betreff:" return then the value after this string or the representation in the corresponding language 
        not all documents include a document number, if none is found return null.
        """)
    private String documentNumber;
    @JsonPropertyDescription("""
         Summarize the document and the informations in one sentence, what is this document about.
         Focus on the key facts and numbers in the document.
         """)
    private String summary;
    @JsonPropertyDescription("""
        A short title, either directly from the document, if available, or create one max 200 characters
        """)
    private String title;
    @JsonPropertyDescription("""
        Generate a concise, unique, and descriptive file name for this document following these rules:
        1. Begin with the document date (if available) in ISO 8601 format YYYY-MM-dd.
        2. Add a short category or topic describing the document’s main subject.
        3. Include the document number (if available).
        4. Add a the author of the document. Creator or sender name, if available.
        5. Add additional key elements from the text which helps to identify or to find the document
        6. Keep the total length under 130 characters.
        7. The name should use the language of the document.
        """)
    private String fileName;
    
    public String toFileName() {
        var result = new ArrayList<String>();
        
        if (Strings.notBlank(date)) result.add(date.toString());
        if (Strings.notBlank(documentType)) result.add(documentType);
        if (Strings.notBlank(documentNumber)) result.add(documentNumber);
        if (Strings.notBlank(author)) result.add(author);

        return FileHelper.cleanFileName(String.join(" ", result));
    }
    
    public String buildFileName() {
        var result = FileHelper.cleanFileName(fileName);
        if (result == null || result.length() < 3) return toFileName();
        return result;
    }
    
    public boolean hasValidFileName() {
        var name = buildFileName();
        return name != null && name.length() > 3;
    }
    
    public String keyWords() {
        var result = new ArrayList<String>();
        
        if (documentType != null) result.add(documentType);
        if (documentNumber != null) result.add(documentNumber);

        return String.join(" ", result);
    }
}