package com.matmuh.matmuhsite.business.constants;

import com.matmuh.matmuhsite.core.dtos.cms.response.CollectionSchema;
import com.matmuh.matmuhsite.core.dtos.cms.response.FieldDefinition;
import com.matmuh.matmuhsite.entities.cms.FieldType;

import java.util.List;

public final class NewsCollectionSchema {

    private NewsCollectionSchema() {}

    public static final String KEY = "news";
    public static final String SLUG_SOURCE_FIELD = "title";
    public static final String DISPLAY_FIELD = "title";

    public static final CollectionSchema SCHEMA = new CollectionSchema(List.of(
            FieldDefinition.required(SLUG_SOURCE_FIELD, FieldType.SHORT_TEXT, "Başlık")
                    .asSearchable()
                    .asSortable()
                    .withHelp("Sayfa adresi bu başlıktan üretilir. Çevirinin adresi kendi başlığından oluşur."),
            FieldDefinition.of("summary", FieldType.LONG_TEXT, "Özet").asSearchable(),
            FieldDefinition.of("body", FieldType.RICH_TEXT, "İçerik"),
            FieldDefinition.of("coverImage", FieldType.IMAGE, "Kapak görseli"),
            FieldDefinition.of("gallery", FieldType.OBJECT_ARRAY, "Galeri")
                    .withItemFields(List.of(
                            FieldDefinition.required("image", FieldType.IMAGE, "Görsel"),
                            FieldDefinition.of("caption", FieldType.SHORT_TEXT, "Açıklama")))
                    .withHelp("Ek fotoğraflar. Yazı içine gömülen görseller içerik editöründen eklenir, buraya konmaz."),
            FieldDefinition.of("attachments", FieldType.OBJECT_ARRAY, "Ekler")
                    .withItemFields(List.of(
                            FieldDefinition.required("url", FieldType.URL, "Dosya bağlantısı"),
                            FieldDefinition.required("name", FieldType.SHORT_TEXT, "Dosya adı"),
                            FieldDefinition.of("previewUrl", FieldType.URL, "Önizleme (PDF)"),
                            FieldDefinition.of("type", FieldType.SHORT_TEXT, "Dosya türü"),
                            FieldDefinition.of("size", FieldType.NUMBER, "Boyut (bayt)")))
                    .withHelp("Habere eklenecek dosyalar: dilekçe, form, sınav takvimi. Word ve Excel dosyaları okuyucuya indirmeden gösterilir."),
            FieldDefinition.of("publishedAt", FieldType.DATE, "Yayın tarihi")
                    .asFilterable()
                    .asSortable(),
            FieldDefinition.of("featured", FieldType.BOOL, "Öne çıkan").asFilterable().asSortable(),
            FieldDefinition.of("tags", FieldType.STRING_ARRAY, "Etiketler").asFilterable(),
            FieldDefinition.of("sourceUrl", FieldType.URL, "Kaynak bağlantısı")
    ));
}
