package com.matmuh.matmuhsite.core.utilities.schema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@Order(0)
@Transactional
public class SystemUserSeed implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(SystemUserSeed.class);

    public static final UUID SYSTEM_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private static final String TABLE_EXISTS = "SELECT to_regclass('users') IS NOT NULL";

    // Kimliği olmayan yazmaların (servis anahtarı, açılış runner'ı) denetim alanları bu satıra
    // bağlanır. Prod'da hiç oluşturulmamıştı: bot ile personel yazmak 404 "Sistem kullanıcısı
    // bulunamadı" veriyordu. Giriş yapamaz: parola geçerli bir BCrypt özeti değil ve rolü yok.
    private static final String INSERT_SYSTEM_USER = """
            INSERT INTO users (id, is_deleted, first_name, last_name, email, password, is_email_verified, provider)
            VALUES (?, false, 'Sistem', 'Servis', 'system@matmuh.local', 'disabled', false, 'LOCAL')
            ON CONFLICT (id) DO NOTHING
            """;

    private final JdbcTemplate jdbcTemplate;

    public SystemUserSeed(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (seed() > 0) {
            logger.info("System user created: {}", SYSTEM_USER_ID);
        } else {
            logger.debug("System user already present");
        }
    }

    public int seed() {
        if (!Boolean.TRUE.equals(jdbcTemplate.queryForObject(TABLE_EXISTS, Boolean.class))) {
            return 0;
        }
        var revived = jdbcTemplate.update("UPDATE users SET is_deleted = false WHERE id = ? AND is_deleted", SYSTEM_USER_ID);
        return revived + jdbcTemplate.update(INSERT_SYSTEM_USER, SYSTEM_USER_ID);
    }
}
