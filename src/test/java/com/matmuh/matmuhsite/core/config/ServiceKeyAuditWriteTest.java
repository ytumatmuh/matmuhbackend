package com.matmuh.matmuhsite.core.config;

import com.matmuh.matmuhsite.business.abstracts.StaffService;
import com.matmuh.matmuhsite.core.dtos.staff.request.CreateStaffRequestDto;
import com.matmuh.matmuhsite.core.utilities.schema.SystemUserSeed;
import com.matmuh.matmuhsite.entities.StaffGroup;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

// Servis anahtarının principal'ı bir kullanıcı değil ("service:<id>"), denetim alanı sistem
// kullanıcısına düşer. Prod'da o satır yoktu: bot personel yazınca 404 dönüyordu (22 Eylül).
@SpringBootTest
class ServiceKeyAuditWriteTest {

    private static final String LAST_NAME = "service-key-audit-test";

    @Autowired
    private StaffService staffService;

    @Autowired
    private SystemUserSeed systemUserSeed;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void authenticateAsServiceKey() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "service:" + UUID.randomUUID(), null, List.of(new SimpleGrantedAuthority("ROLE_CONTENT_WRITE"))));
    }

    @AfterEach
    void cleanUp() {
        SecurityContextHolder.clearContext();
        jdbc.update("DELETE FROM staff_groups WHERE staff_id IN (SELECT id FROM staff WHERE last_name = ?)", LAST_NAME);
        jdbc.update("DELETE FROM staff WHERE last_name = ?", LAST_NAME);
        systemUserSeed.seed();
    }

    private UUID createStaff(String firstName) {
        var request = new CreateStaffRequestDto();
        request.setFirstName(firstName);
        request.setLastName(LAST_NAME);
        request.setGroups(Set.of(StaffGroup.ACADEMIC));
        return staffService.createStaff(request).getId();
    }

    private UUID createdBy(UUID staffId) {
        return jdbc.queryForObject("SELECT created_by_id FROM staff WHERE id = ?", UUID.class, staffId);
    }

    @Test
    void aServiceKeyWriteIsAttributedToTheSystemUser() {
        var id = createStaff("Bot");

        assertNotNull(id);
        assertEquals(SystemUserSeed.SYSTEM_USER_ID, createdBy(id));
    }

    // Sistem kullanıcısı silinse bile yazma düşmemeli; denetim alanı boş kalır.
    @Test
    void aMissingSystemUserDoesNotFailTheWrite() {
        jdbc.update("DELETE FROM authorities WHERE user_id = ?", SystemUserSeed.SYSTEM_USER_ID);
        jdbc.update("DELETE FROM users WHERE id = ?", SystemUserSeed.SYSTEM_USER_ID);

        var id = createStaff("Botsuz");

        assertNotNull(id);
        assertNull(createdBy(id));
    }
}
