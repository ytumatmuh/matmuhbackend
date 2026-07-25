package com.matmuh.matmuhsite.entities.cms;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum FieldType {

    SHORT_TEXT("ShortText"),
    LONG_TEXT("LongText"),
    RICH_TEXT("RichText"),
    NUMBER("Number"),
    BOOL("Bool"),
    URL("Url"),
    DATE("Date"),
    STRING_ARRAY("StringArray"),
    OBJECT_ARRAY("ObjectArray"),
    IMAGE("Image");

    private final String wireName;

    FieldType(String wireName) { this.wireName = wireName; }

    @JsonValue
    public String getWireName() { return wireName; }

    @JsonCreator
    public static FieldType fromWireName(String value) {
        if (value != null && value.equalsIgnoreCase("Text")) return LONG_TEXT;
        for (FieldType t : values()) {
            if (t.wireName.equalsIgnoreCase(value)) return t;
        }
        throw new IllegalArgumentException("Unknown FieldType: " + value);
    }
}