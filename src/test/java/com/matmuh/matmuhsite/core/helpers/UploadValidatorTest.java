package com.matmuh.matmuhsite.core.helpers;

import com.matmuh.matmuhsite.core.exceptions.FileEmptyException;
import com.matmuh.matmuhsite.core.exceptions.FileSizeExceededException;
import com.matmuh.matmuhsite.core.exceptions.UnsupportedFileTypeException;
import com.matmuh.matmuhsite.core.properties.UploadProperties;
import com.matmuh.matmuhsite.core.utilities.storage.FolderType;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UploadValidatorTest {

    private final UploadValidator validator = new UploadValidator(new UploadProperties());

    private MockMultipartFile file(String name, int bytes) {
        return new MockMultipartFile("file", name, "application/octet-stream", new byte[bytes]);
    }

    @Test
    void acceptsWhitelistedExtensions() {
        for (var name : new String[]{"not.pdf", "not.PDF", "sunum.pptx", "arsiv.zip", "resim.png"}) {
            assertDoesNotThrow(() -> validator.validate(file(name, 10), FolderType.FILE, "empty", "size"), name);
        }
    }

    @Test
    void rejectsExecutableAndMarkupExtensions() {
        for (var name : new String[]{"kotu.exe", "betik.html", "sayfa.svg", "kabuk.sh"}) {
            assertThrows(UnsupportedFileTypeException.class,
                    () -> validator.validate(file(name, 10), FolderType.FILE, "empty", "size"), name);
        }
    }

    @Test
    void rejectsMissingExtension() {
        assertThrows(UnsupportedFileTypeException.class,
                () -> validator.validate(file("uzantisiz", 10), FolderType.FILE, "empty", "size"));
    }

    @Test
    void doesNotTrustDeclaredContentType() {
        var disguised = new MockMultipartFile("file", "kotu.exe", "application/pdf", new byte[10]);
        assertThrows(UnsupportedFileTypeException.class,
                () -> validator.validate(disguised, FolderType.FILE, "empty", "size"));
    }

    @Test
    void imagesAreStricterThanFiles() {
        assertDoesNotThrow(() -> validator.validate(file("a.png", 10), FolderType.IMAGE, "empty", "size"));
        assertThrows(UnsupportedFileTypeException.class,
                () -> validator.validate(file("a.pdf", 10), FolderType.IMAGE, "empty", "size"));
        assertThrows(UnsupportedFileTypeException.class,
                () -> validator.validate(file("a.svg", 10), FolderType.IMAGE, "empty", "size"));
    }

    @Test
    void publicDocumentsAllowOfficeButNotArchives() {
        for (var name : new String[]{"dilekce.pdf", "form.doc", "form.docx", "takvim.xlsx"}) {
            assertDoesNotThrow(() -> validator.validate(file(name, 10), FolderType.PUBLIC_FILE, "empty", "size"), name);
        }
        for (var name : new String[]{"arsiv.zip", "kotu.exe", "sayfa.svg"}) {
            assertThrows(UnsupportedFileTypeException.class,
                    () -> validator.validate(file(name, 10), FolderType.PUBLIC_FILE, "empty", "size"), name);
        }
    }

    @Test
    void reportsTheLimitInMegabytes() {
        var properties = new UploadProperties();
        properties.setMaxFileSizeMb(1);
        var limited = new UploadValidator(properties);

        var exception = assertThrows(FileSizeExceededException.class,
                () -> limited.validate(file("not.pdf", 2 * 1024 * 1024), FolderType.FILE, "empty", "size"));
        assertEquals(1L, exception.getMessageArguments()[0]);
    }

    @Test
    void rejectsEmptyFile() {
        assertThrows(FileEmptyException.class,
                () -> validator.validate(file("not.pdf", 0), FolderType.FILE, "empty", "size"));
    }
}
