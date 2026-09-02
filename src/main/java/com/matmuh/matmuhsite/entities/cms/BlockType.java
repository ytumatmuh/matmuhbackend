package com.matmuh.matmuhsite.entities.cms;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum BlockType {

    SHORT_TEXT("ShortText"),
    LONG_TEXT("LongText"),
    RICH_TEXT("RichText"),
    NUMBER("Number"),
    BOOL("Bool"),
    URL("Url"),
    DATE("Date"),
    IMAGE("Image"),
    LINK("Link"),
    SELECT("Select"),
    STRING_ARRAY("StringArray"),
    OBJECT_ARRAY("ObjectArray"),
    COLLECTION("Collection"),


    LIST("List");

    private final String wireName;

    BlockType(String wireName) {
        this.wireName = wireName;
    }

    @JsonValue
    public String getWireName() {
        return wireName;
    }

    @JsonCreator
    public static BlockType fromWireName(String value) {
        if (value == null) {
            throw new IllegalArgumentException("blockType is required");
        }
        if (value.equalsIgnoreCase("Text")) {
            return LONG_TEXT;
        }
        if (value.equalsIgnoreCase(LIST.wireName)) {
            return OBJECT_ARRAY;
        }
        for (BlockType type : values()) {
            if (type.wireName.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown blockType: " + value);
    }
}
