package com.matmuh.matmuhsite.business.constants;

import com.matmuh.matmuhsite.core.dtos.cms.response.CollectionSchema;
import com.matmuh.matmuhsite.core.dtos.cms.response.ChoiceSource;
import com.matmuh.matmuhsite.core.dtos.cms.response.FieldDefinition;
import com.matmuh.matmuhsite.entities.StaffGroup;
import com.matmuh.matmuhsite.entities.cms.FieldType;

import java.util.Arrays;
import java.util.List;

public final class StaffCollectionSchema {

    private StaffCollectionSchema() {}

    public static final String KEY = "staff";
    public static final String SLUG_SOURCE_FIELD = "lastName";
    public static final String DISPLAY_FIELD = "fullName";

    public static final String FIELD_FIRST_NAME = "firstName";
    public static final String FIELD_LAST_NAME = "lastName";
    public static final String FIELD_EMAIL = "email";
    public static final String FIELD_AVESIS_LINK = "avesisLink";
    public static final String FIELD_GROUPS = "groups";

    private static final String[] GROUP_OPTIONS =
            Arrays.stream(StaffGroup.values()).map(Enum::name).toArray(String[]::new);

    public static final CollectionSchema SCHEMA = new CollectionSchema(List.of(
            FieldDefinition.readOnly("rawName", FieldType.SHORT_TEXT, "Ham ad")
                    .withHelp("İçe aktarmada gelen ham ad; editörden değiştirilmez."),
            FieldDefinition.required(FIELD_FIRST_NAME, FieldType.SHORT_TEXT, "Ad").asFilterable(),
            FieldDefinition.required(FIELD_LAST_NAME, FieldType.SHORT_TEXT, "Soyad").asFilterable(),
            FieldDefinition.readOnly(DISPLAY_FIELD, FieldType.SHORT_TEXT, "Ad Soyad")
                    .asComputed()
                    .withHelp("Unvan, ad ve soyaddan oluşur."),
            FieldDefinition.required(FIELD_GROUPS, FieldType.STRING_ARRAY, "Kategori")
                    .withSource(ChoiceSource.ofValues(GROUP_OPTIONS))
                    .asFilterable()
                    .withHelp("Bir personel birden fazla kategoride yer alabilir; bölüm başkanı hem Yönetim hem Akademik Kadro altında listelenir."),
            FieldDefinition.of("role", FieldType.SHORT_TEXT, "Görev / Rol")
                    .withHelp("Yönetim kartlarında adın altında görünür, ör. Bölüm Başkanı."),
            FieldDefinition.of("academicTitle", FieldType.SHORT_TEXT, "Akademik Ünvan").asFilterable(),
            FieldDefinition.of(FIELD_EMAIL, FieldType.SHORT_TEXT, "E-posta").asFilterable(),
            FieldDefinition.of("phone", FieldType.SHORT_TEXT, "Telefon"),
            FieldDefinition.of("office", FieldType.SHORT_TEXT, "Ofis"),
            FieldDefinition.of("photo", FieldType.IMAGE, "Fotoğraf")
                    .withHelp("Boş bırakılırsa ad-soyad baş harfleri gösterilir."),
            FieldDefinition.of(FIELD_AVESIS_LINK, FieldType.URL, "AVESİS").asFilterable()
    ));
}
