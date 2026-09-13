package com.matmuh.matmuhsite.core.helpers;

import com.matmuh.matmuhsite.core.dtos.cms.response.CollectionSchema;
import com.matmuh.matmuhsite.core.dtos.cms.response.FieldDefinition;
import com.matmuh.matmuhsite.dataAccess.abstracts.FileDao;
import com.matmuh.matmuhsite.entities.File;
import com.matmuh.matmuhsite.entities.cms.FieldType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.matmuh.matmuhsite.core.helpers.CollectionSchemaValidator.FILE_PREVIEW_URL;
import static com.matmuh.matmuhsite.core.helpers.CollectionSchemaValidator.FILE_URL;

@Component
public class FilePreviewEnricher {

    private final FileDao fileDao;
    private final StorageUrlResolver storageUrlResolver;

    public FilePreviewEnricher(FileDao fileDao, StorageUrlResolver storageUrlResolver) {
        this.fileDao = fileDao;
        this.storageUrlResolver = storageUrlResolver;
    }

    public ObjectNode enrich(CollectionSchema schema, ObjectNode data) {
        var files = new ArrayList<ObjectNode>();
        collect(schema.fields(), data, files);
        if (files.isEmpty()) {
            return data;
        }

        var keys = files.stream()
                .map(this::keyOf)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        var previews = keys.isEmpty() ? Map.<String, String>of() : fileDao.findByFileUrlIn(keys).stream()
                .filter(file -> file.getPreviewUrl() != null && !file.getPreviewUrl().isBlank())
                .collect(Collectors.toMap(File::getFileUrl, File::getPreviewUrl, (first, second) -> first));

        for (var file : files) {
            var key = keyOf(file);
            var preview = key == null ? null : previews.get(key);
            if (preview == null) {
                file.remove(FILE_PREVIEW_URL);
            } else {
                file.put(FILE_PREVIEW_URL, storageUrlResolver.urlFor(preview));
            }
        }
        return data;
    }

    private String keyOf(ObjectNode file) {
        var url = file.get(FILE_URL);
        return url == null || !url.isTextual() ? null : storageUrlResolver.keyFor(url.asText());
    }

    private static void collect(List<FieldDefinition> fields, JsonNode data, List<ObjectNode> out) {
        if (data == null || !data.isObject()) {
            return;
        }
        for (var field : fields) {
            var value = data.get(field.name());
            if (value == null || value.isNull()) {
                continue;
            }
            if (field.type() == FieldType.FILE && value.isObject()) {
                out.add((ObjectNode) value);
            } else if (field.type() == FieldType.OBJECT_ARRAY && value.isArray() && field.itemFields() != null) {
                for (var item : value) {
                    collect(field.itemFields(), item, out);
                }
            }
        }
    }
}
