package com.matmuh.matmuhsite.core.config;

import org.hibernate.type.format.AbstractJsonFormatMapper;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.lang.reflect.Type;

public class Jackson3JsonFormatMapper extends AbstractJsonFormatMapper {

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    @Override
    protected <T> T fromString(CharSequence charSequence, Type type) {
        return objectMapper.readValue(charSequence.toString(),
                objectMapper.getTypeFactory().constructType(type));
    }

    @Override
    protected <T> String toString(T value, Type type) {
        return objectMapper.writerFor(objectMapper.getTypeFactory().constructType(type))
                .writeValueAsString(value);
    }
}
