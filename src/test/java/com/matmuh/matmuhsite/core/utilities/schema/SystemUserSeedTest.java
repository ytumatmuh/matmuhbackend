package com.matmuh.matmuhsite.core.utilities.schema;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

// Kimliksiz yazmaların denetim alanı bu satıra bağlanır; prod'da yoktu ve servis anahtarıyla
// personel/medya yazmak 404 "Sistem kullanıcısı bulunamadı" veriyordu (Egehan, 22 Eylül).
@SpringBootTest
class SystemUserSeedTest {

    @Autowired
    private SystemUserSeed seed;

    @Autowired
    private JdbcTemplate jdbc;

    private int systemUsers() {
        return jdbc.queryForObject("SELECT count(*) FROM users WHERE id = ? AND NOT is_deleted",
                Integer.class, SystemUserSeed.SYSTEM_USER_ID);
    }

    @Test
    void startupLeavesExactlyOneLiveSystemUser() {
        assertEquals(1, systemUsers());

        assertEquals(0, seed.seed());
        assertEquals(1, systemUsers());
    }

    @Test
    void aSoftDeletedSystemUserIsRevived() {
        jdbc.update("UPDATE users SET is_deleted = true WHERE id = ?", SystemUserSeed.SYSTEM_USER_ID);
        assertEquals(0, systemUsers());

        assertEquals(1, seed.seed());
        assertEquals(1, systemUsers());
    }

    // Satır yalnız denetim alanı için: rolü yok, parolası geçerli bir özet değil.
    @Test
    void theSystemUserCannotSignIn() {
        var roles = jdbc.queryForObject("SELECT count(*) FROM authorities WHERE user_id = ?",
                Integer.class, SystemUserSeed.SYSTEM_USER_ID);
        var password = jdbc.queryForObject("SELECT password FROM users WHERE id = ?",
                String.class, SystemUserSeed.SYSTEM_USER_ID);

        assertEquals(0, roles);
        assertFalse(password != null && password.startsWith("$2"));
    }
}
