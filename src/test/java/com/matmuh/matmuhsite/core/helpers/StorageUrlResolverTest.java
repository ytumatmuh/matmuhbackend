package com.matmuh.matmuhsite.core.helpers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class StorageUrlResolverTest {

    private final StorageUrlResolver resolver = new StorageUrlResolver("https://cdn.example/", "https://api.example/");

    @Test
    void urlForAndKeyForRoundTrip() {
        for (var key : new String[]{"public/a.docx", "files/b.pdf"}) {
            assertEquals(key, resolver.keyFor(resolver.urlFor(key)), key);
        }
    }

    // jsonb'deki adres domain taşınınca eski kalır; anahtar alan adından bağımsız çözülmeli.
    @Test
    void keyForIgnoresTheHost() {
        assertEquals("public/a.docx", resolver.keyFor("https://matmuh.yusufacmaci.com/api/uploads/public/a.docx"));
        assertEquals("public/a.docx", resolver.keyFor("https://matmuh.yildiz.edu.tr/api/uploads/public/a.docx?x=1"));
    }

    @Test
    void keyForDecodesPercentEncoding() {
        assertEquals("public/Özet.pdf", resolver.keyFor("https://api.example/api/uploads/public/%C3%96zet.pdf"));
    }

    @Test
    void keyForIsNullOutsideTheUploadsRoute() {
        assertNull(resolver.keyFor("https://cdn.example/images/x.png"));
        assertNull(resolver.keyFor("https://mtm.yildiz.edu.tr/form.docx"));
        assertNull(resolver.keyFor("https://api.example/api/uploads/"));
        assertNull(resolver.keyFor(null));
    }
}
