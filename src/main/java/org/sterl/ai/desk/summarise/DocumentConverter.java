package org.sterl.ai.desk.summarise;

import org.springframework.ai.converter.StructuredOutputConverter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DocumentConverter implements StructuredOutputConverter<DocumentInfo> {
    
    private final ObjectMapper mapper;

    @Override
    public DocumentInfo convert(@NonNull String source) {
        try {
            return mapper.readValue(source, DocumentInfo.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse " + source, e);
        }
    }

    @Override
    public String getFormat() {
        return """
                Do not include any explanations.
                Only provide a RFC8259 compliant JSON response following this format without deviation.
                The data structure for the JSON object should match this Java class "DocumentInfo" with the properties:
                class DocumentInfo {
                    // The sender or issuing company/organization name. never a number alone, but may contain a number in the zip.
                    // This is maybe also the creator of the document. This should never be null.
                    // If you find a company name and the address of the company which created the document, include both.
                    private String from;
                    // The receiver’s company/organization name. never a number alone, but may contain a number in the zip.
                    // This could be null, if the document has no receiver.
                    private String to;
                    
                    // The date of the letter or document use ISO format as Java LocalDate format "YYYY-MM-DD".
                    // Verify the dates you find more than one, this should be the date this document was created at.
                    // If you can't find any date return JSON null.
                    private String date;
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
                    7. In one or two words the central topic or reason for the document
                    
                    Keep the total length under 130 characters.
                    The name should use the language of the user message and contain the most relevant key elements of the use text.
                    */
                    private String fileName;
                }
                If a property is not available in the text, use "null", or leave property out of the json response. Not use any fill in text.
                Some examples for a "documentType" are (other values are possible):
                    - Mahnung
                    - Rechnung
                    - Kaufvertrag
                    - Artzbesuch
                    - Lieferschein
                    - Bestellung
                Any kind of a Rechnung, Lieferschein, etc. should contain a unique "documentNumber" from the sender (from).
                Return only the JSON object, no other format or any markdown. The JSON should be parsable by a JAVA JSON parser.
                """;
    }
}
