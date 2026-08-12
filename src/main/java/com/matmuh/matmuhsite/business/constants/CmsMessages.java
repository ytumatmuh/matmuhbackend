package com.matmuh.matmuhsite.business.constants;

public class CmsMessages {
    public static final String BLOCKS_NOT_NULL = "Blocks cannot be null";
    public static final String SLUG_NOT_BLANK = "Slug cannot be null or empty";
    public static final String BLOCK_PATH_NOT_BLANK =  "Block path cannot be null or empty";
    public static final String VALUE_NOT_NULL = "Value cannot be null";
    public static final String BLOCK_TYPE_NOT_NULL = "Block type cannot be null";
    public static final String DEFAULT_VALUE_NOT_NULL = "Default value cannot be null";
    public static final String DATA_NOT_NULL = "Data cannot be null";

    public static final String COLLECTION_NOT_FOUND = "Collection not found: ";
    public static final String COLLECTION_ITEM_NOT_FOUND = "Collection item not found: ";
    public static final String VERSION_CONFLICT = "Version conflict: item was modified by someone else.";
    public static final String LOCALE_NOT_DECLARED = "Locale is not declared for this site: ";
    public static final String LOCALE_INVALID = "Invalid locale code: ";
    public static final String COLLECTION_NOT_ARCHIVABLE = "Collection is provider-backed and cannot be archived: ";
    public static final String AUTO_GENERATED_USE_POST = "This collection uses auto-generated slugs; use POST to create items.";
    public static final String USER_DEFINED_USE_PUT = "This collection uses user-defined slugs; use PUT to create items.";
    public static final String SLUG_SOURCE_FIELD_MISSING = "Cannot generate slug: source field is missing or empty.";
    public static final String SLUG_REQUIRED_FOR_NEW_DRAFT = "Slug is required for a new item draft in this collection.";
    public static final String SLUG_ALREADY_IN_USE = "Slug is already in use: ";
    public static final String FILE_EMPTY = "Uploaded file is empty.";
    public static final String FILE_TOO_LARGE = "Uploaded file exceeds the size limit.";

    public static final String SYNCED_BY_DEPLOY_PIPELINE = "deploy-pipeline";
}
