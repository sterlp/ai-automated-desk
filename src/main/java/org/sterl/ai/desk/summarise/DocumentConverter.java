package org.sterl.ai.desk.summarise;

import org.springframework.ai.converter.StructuredOutputConverter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;

@Component
public class DocumentConverter implements StructuredOutputConverter<DocumentInfo> {
    
    private final ObjectMapper mapper;
    private final JsonSchemaGenerator schemaGenerator;
    

    public DocumentConverter(ObjectMapper mapper) {
        super();
        this.mapper = mapper;
        this.schemaGenerator = new JsonSchemaGenerator(mapper);
    }

    @Override
    public DocumentInfo convert(@NonNull String source) {
        try {
            return mapper.readValue(source, DocumentInfo.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse " + source, e);
        }
    }

    private volatile String cache = null;

    @Override
    public String getFormat() {
        if (cache == null) {
            try {
                cache = mapper.writeValueAsString(schemaGenerator.generateSchema(DocumentInfo.class));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return cache;
    }
}
