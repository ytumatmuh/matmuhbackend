package com.matmuh.matmuhsite.core.utilities.storage;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StorageKeysTest {

    @Test
    void decodesPercentEncodedRequestPath() {
        assertEquals("files/9f3c-Analiz_Özet.pdf",
                StorageKeys.fromRequestPath("files/9f3c-Analiz_%C3%96zet.pdf"));
    }

    @Test
    void leavesPlainAsciiPathUntouched() {
        assertEquals("files/9f3c-analiz.pdf",
                StorageKeys.fromRequestPath("files/9f3c-analiz.pdf"));
    }

    @Test
    void keepsPlusSignInsteadOfTurningItIntoSpace() {
        assertEquals("files/9f3c-c++notlari.pdf",
                StorageKeys.fromRequestPath("files/9f3c-c++notlari.pdf"));
    }

    @Test
    void transliteratesTurkishCharactersInNewKeys() {
        assertEquals("Analiz_Ozet_Guz.pdf", StorageKeys.sanitize("Analiz Özet Güz.pdf"));
        assertEquals("cCgGiIoOsSuU.pdf", StorageKeys.sanitize("çÇğĞıİöÖşŞüÜ.pdf"));
    }

    @Test
    void replacesCharactersThatBreakUrls() {
        assertEquals("not_1_ozet_.pdf", StorageKeys.sanitize("not#1 özet?.pdf"));
        assertEquals("a_b.pdf", StorageKeys.sanitize("a   b.pdf"));
    }

    @Test
    void newKeyCarriesNoUserSuppliedText() {
        var key = StorageKeys.newKey(FolderType.FILE, "Analiz Özet Güz#1.pdf");

        assertTrue(key.startsWith("files/"));
        assertTrue(key.endsWith(".pdf"));
        assertTrue(key.chars().allMatch(c -> c < 128), "anahtar ASCII disi karakter tasiyor: " + key);
        assertFalse(key.toLowerCase().contains("analiz"), "anahtar orijinal adi tasiyor: " + key);
        assertEquals("files/".length() + 36 + 4, key.length());
    }

    @Test
    void separatesAccessClassesByPrefix() {
        assertTrue(StorageKeys.newKey(FolderType.IMAGE, "a.png").startsWith("images/"));
        assertTrue(StorageKeys.newKey(FolderType.FILE, "a.pdf").startsWith("files/"));
        assertTrue(StorageKeys.newKey(FolderType.PUBLIC_FILE, "a.pdf").startsWith("public/"));

        assertTrue(StorageKeys.isPrivate("files/a.pdf"));
        assertFalse(StorageKeys.isPrivate("public/a.pdf"));
        assertTrue(StorageKeys.isPublicFile("public/a.pdf"));
        assertFalse(StorageKeys.isPublicFile("files/a.pdf"));
        assertFalse(StorageKeys.isPublicFile("images/a.png"));
    }

    @Test
    void newKeysAreUniqueForTheSameFileName() {
        assertNotEquals(StorageKeys.newKey(FolderType.FILE, "not.pdf"),
                StorageKeys.newKey(FolderType.FILE, "not.pdf"));
    }

    @Test
    void keepsOnlyASafeExtension() {
        assertEquals(".pdf", StorageKeys.extensionOf("not.PDF"));
        assertEquals(".png", StorageKeys.extensionOf("resim.png"));
        assertEquals("", StorageKeys.extensionOf("uzantisiz"));
        assertEquals("", StorageKeys.extensionOf("bozuk."));
        assertEquals("", StorageKeys.extensionOf("not.pdf?x=1"));
        assertEquals("", StorageKeys.extensionOf("not.cokuzunbiruzanti"));
    }

    @Test
    void writesBothAsciiAndUtf8FileNames() {
        assertEquals("inline; filename=\"Analiz_Ozet.pdf\"; filename*=UTF-8''Analiz%20%C3%96zet.pdf",
                FileDispositions.inline("Analiz Özet.pdf"));
    }

    @Test
    void leavesRfc8187AttrCharsLiteral() {
        assertEquals("inline; filename=\"not_1.pdf\"; filename*=UTF-8''not#1.pdf",
                FileDispositions.inline("not#1.pdf"));
    }

    @Test
    void fallsBackWhenFileNameIsMissing() {
        assertEquals("inline", FileDispositions.inline(null));
        assertEquals("inline", FileDispositions.inline("   "));
    }
}
