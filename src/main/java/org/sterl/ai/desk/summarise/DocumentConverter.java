package org.sterl.ai.desk.summarise;

import org.springframework.ai.converter.StructuredOutputConverter;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DocumentConverter implements StructuredOutputConverter<DocumentInfo> {
    
    private final ObjectMapper mapper;

    @Override
    public DocumentInfo convert(String source) {
        try {
            return mapper.readValue(source, DocumentInfo.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse " + source, e);
        }
    }

    @Override
    public String getFormat() {
        return """
                Your response should be in JSON format.
                Do not include any explanations, only provide a RFC8259 compliant JSON response following this format without deviation.
                The data structure for the JSON should match this Java class: DocumentInfo with the properties
                - from: The sender's or issuing company/organization name. never a number alone, but may contain a number in the zip.
                - to:   The receiver’s company/organization name. never a number alone, but may contain a number in the zip.
                - date: The date of the letter or document (use ISO format as Java LocalDate: YYYY-MM-DD).
                - documentType: The type of document (e.g., Rechnung, Mahnung, Lieferschein, Abrechnung, Versicherungsrechnung, etc.), or any other type
                - documentNumber: Any invoice number, reference number, policy number, rechnungsnummer, Rechnung Nr., or other identifier ...
                - reason: For what reason this document was sent, e.g. Vertrag, Kreditvertrag, Kaufvertrag, Rechnung, or other identifier ...
                - summary: one short sentence, which summerizes the document in the most exact way.
                If a field is not available in the text, return it with the value null.
                documentType, reason, are usually be different. Like:
                    - documentType: Mahnung  for a reason: Rechnung
                    - documentType: Rechnung for a reason: Kaufvertrag
                    - documentType: Rechnung for a reason: Artzbesuch
                    - documentType: Lieferschein for a reason: Bestellung
                Any kind of a Rechnung, Lieferschein, etc. should contain a unique number from the sender (from).
                Return only the JSON object, no other format or any markdown. The JSON should be parsable by a JAVA JSON parser.
                """;
    }
}
