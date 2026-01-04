package org.sterl.ai.desk.summarise;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

class DocumentConverterTest {

    ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    DocumentConverter subject = new DocumentConverter(mapper);
    
    @Test
    void testFormat() {
        var format = subject.getFormat();
        
        assertThat(format).contains("description");
        assertThat(format).contains("\"type\" : \"object\"");
    }

}
