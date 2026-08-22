package com.matmuh.matmuhsite.business.constants;

import com.matmuh.matmuhsite.core.dtos.cms.response.CollectionSchema;
import com.matmuh.matmuhsite.core.dtos.cms.response.FieldDefinition;
import com.matmuh.matmuhsite.entities.cms.FieldType;

import java.util.List;

public final class NewsCollectionSchema {

    private NewsCollectionSchema() {}

    public static final String KEY = "news";
    public static final String SLUG_SOURCE_FIELD = "title";

    public static final CollectionSchema SCHEMA = new CollectionSchema(List.of(
            FieldDefinition.readOnly("id", FieldType.SHORT_TEXT, "ID"),
            FieldDefinition.readOnly("slug", FieldType.SHORT_TEXT, "Slug"),
            FieldDefinition.required(SLUG_SOURCE_FIELD, FieldType.SHORT_TEXT, "Başlık")
                    .withHelp("Slug bu başlıktan üretilir. Çeviri kendi başlığından kendi slug'ını alır."),
            FieldDefinition.of("summary", FieldType.LONG_TEXT, "Özet"),
            FieldDefinition.of("body", FieldType.RICH_TEXT, "İçerik"),
            FieldDefinition.of("coverImage", FieldType.IMAGE, "Kapak görseli"),
            FieldDefinition.of("gallery", FieldType.OBJECT_ARRAY, "Galeri")
                    .withItemFields(List.of(
                            FieldDefinition.required("image", FieldType.IMAGE, "Görsel"),
                            FieldDefinition.of("caption", FieldType.SHORT_TEXT, "Açıklama")))
                    .withHelp("Ek fotoğraflar. Yazı içine gömülen görseller içerik editöründen eklenir, buraya konmaz."),
            FieldDefinition.of("publishedAt", FieldType.DATE, "Yayın tarihi")
                    .asFilterable()
                    .asSortable(),
            FieldDefinition.of("featured", FieldType.BOOL, "Öne çıkan").asFilterable(),
            FieldDefinition.of("tags", FieldType.STRING_ARRAY, "Etiketler").asFilterable(),
            FieldDefinition.of("sourceUrl", FieldType.URL, "Kaynak bağlantısı")
    ));
}
