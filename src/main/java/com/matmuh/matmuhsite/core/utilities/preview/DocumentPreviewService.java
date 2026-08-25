package com.matmuh.matmuhsite.core.utilities.preview;

import com.matmuh.matmuhsite.core.properties.PreviewProperties;
import com.matmuh.matmuhsite.core.utilities.storage.FolderType;
import com.matmuh.matmuhsite.core.utilities.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.Locale;

@Service
public class DocumentPreviewService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentPreviewService.class);

    private static final String CONVERT_PATH = "/forms/libreoffice/convert";

    private final PreviewProperties previewProperties;
    private final StorageService storageService;
    private final RestClient restClient;

    public DocumentPreviewService(PreviewProperties previewProperties, StorageService storageService) {
        this.previewProperties = previewProperties;
        this.storageService = storageService;
        this.restClient = RestClient.builder()
                .requestFactory(requestFactory(previewProperties.getTimeoutSeconds()))
                .build();
    }


    public String createPdfPreview(byte[] source, String originalFileName) {
        if (!previewProperties.isEnabled() || !previewProperties.isConvertible(extensionOf(originalFileName))) {
            return null;
        }

        try {
            var body = new LinkedMultiValueMap<String, Object>();
            body.add("files", new NamedByteArrayResource(source, originalFileName));

            var pdf = restClient.post()
                    .uri(previewProperties.getConverterUrl() + CONVERT_PATH)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(byte[].class);

            if (pdf == null || pdf.length == 0) {
                logger.warn("Preview converter returned an empty document for {}", originalFileName);
                return null;
            }

            var key = storageService.uploadFile(pdf, previewFileName(originalFileName),
                    MediaType.APPLICATION_PDF_VALUE, FolderType.FILE);
            logger.info("Preview generated for {} -> {}", originalFileName, key);
            return key;
        } catch (RuntimeException exception) {
            logger.warn("Preview conversion failed for {}: {}", originalFileName, exception.getMessage());
            return null;
        }
    }

    private String previewFileName(String originalFileName) {
        var dot = originalFileName == null ? -1 : originalFileName.lastIndexOf('.');
        var base = dot < 0 ? "preview" : originalFileName.substring(0, dot);
        return base + ".pdf";
    }

    private String extensionOf(String fileName) {
        if (fileName == null) {
            return null;
        }
        var dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return null;
        }
        return fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private SimpleClientHttpRequestFactory requestFactory(int timeoutSeconds) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(timeoutSeconds));
        factory.setReadTimeout(Duration.ofSeconds(timeoutSeconds));
        return factory;
    }

    private static final class NamedByteArrayResource extends ByteArrayResource {

        private final String fileName;

        private NamedByteArrayResource(byte[] bytes, String fileName) {
            super(bytes);
            this.fileName = fileName == null || fileName.isBlank() ? "document" : fileName;
        }

        @Override
        public String getFilename() {
            return fileName;
        }
    }
}
