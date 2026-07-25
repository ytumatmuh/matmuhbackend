package com.matmuh.matmuhsite.entities.cms;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum BlockType {

    SHORT_TEXT("ShortText"),
    LONG_TEXT("LongText"),
    RICH_TEXT("RichText"),
    IMAGE("Image"),
    LINK("Link"),
    DATE("Date"),
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
        for (BlockType type : values()) {
            if (type.wireName.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown blockType: " + value);
    }


}
