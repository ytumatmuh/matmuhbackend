package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.CmsCollectionService;
import com.matmuh.matmuhsite.core.dtos.cms.request.CreateCollectionItemRequestDto;
import com.matmuh.matmuhsite.core.dtos.cms.request.UpsertCollectionItemRequestDto;
import com.matmuh.matmuhsite.core.utilities.schema.SystemUserSeed;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Bot (CONTENT_WRITE) ders ve personel oluşturabiliyor ama güncellemesi 500 veriyordu;
// akademik dönem güncellemesi çalışıyordu (Egehan, 23 Eylül).
@SpringBootTest
class CmsProviderUpsertByServiceKeyTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final String CODE = "ZZT9001";
    private static final String LAST_NAME = "upsert-by-service-key-test";

    @Autowired
    private CmsCollectionService cms;

    @Autowired
    private JdbcTemplate jdbc;

    private final String bot = "service:" + UUID.randomUUID();

    @BeforeEach
    void authenticateAsServiceKey() {
        cleanUp();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                bot, null, List.of(new SimpleGrantedAuthority("ROLE_CONTENT_WRITE"))));
    }

    @AfterEach
    void cleanUp() {
        SecurityContextHolder.clearContext();
        jdbc.update("DELETE FROM lecture_degree_levels WHERE lecture_id IN (SELECT id FROM lectures WHERE code = ?)", CODE);
        jdbc.update("DELETE FROM lecture_programs WHERE lecture_id IN (SELECT id FROM lectures WHERE code = ?)", CODE);
        jdbc.update("DELETE FROM lecture_languages WHERE lecture_id IN (SELECT id FROM lectures WHERE code = ?)", CODE);
        jdbc.update("DELETE FROM lecture_syllabus WHERE lecture_id IN (SELECT id FROM lectures WHERE code = ?)", CODE);
        jdbc.update("DELETE FROM lectures WHERE code = ?", CODE);
        jdbc.update("DELETE FROM staff_groups WHERE staff_id IN (SELECT id FROM staff WHERE last_name = ?)", LAST_NAME);
        jdbc.update("DELETE FROM staff_office_hours WHERE staff_id IN (SELECT id FROM staff WHERE last_name = ?)", LAST_NAME);
        jdbc.update("DELETE FROM staff WHERE last_name = ?", LAST_NAME);
    }

    private static CreateCollectionItemRequestDto create(String json) {
        var request = new CreateCollectionItemRequestDto();
        request.setData(JSON.readTree(json));
        return request;
    }

    private static UpsertCollectionItemRequestDto upsert(String json, int version) {
        var request = new UpsertCollectionItemRequestDto();
        request.setData(JSON.readTree(json));
        request.setVersion(version);
        return request;
    }

    @Test
    void theBotCanUpdateALectureItCreated() {
        var created = cms.createWithAutoSlug("lectures",
                create("{\"code\": \"" + CODE + "\", \"name\": \"İlk ad\"}"), bot, null, null);

        var updated = cms.upsert("lectures", created.getSlug(),
                upsert("{\"code\": \"" + CODE + "\", \"name\": \"Dinamik Sistemler ve Kaos\"}", created.getVersion()),
                bot, null, null);

        assertEquals("Dinamik Sistemler ve Kaos", updated.getData().get("name").asText());
    }

    @Test
    void theBotCanUpdateAStaffMemberItCreated() {
        var created = cms.createWithAutoSlug("staff",
                create("{\"firstName\": \"İnci\", \"lastName\": \"" + LAST_NAME + "\", \"groups\": [\"ACADEMIC\"]}"), bot, null, null);

        var updated = cms.upsert("staff", created.getSlug(),
                upsert("{\"firstName\": \"İnci\", \"lastName\": \"" + LAST_NAME + "\", \"groups\": [\"ACADEMIC\"], \"role\": \"Öğr. Üyesi\"}", created.getVersion()),
                bot, null, null);

        assertEquals("Öğr. Üyesi", updated.getData().get("role").asText());
        assertEquals(SystemUserSeed.SYSTEM_USER_ID, jdbc.queryForObject(
                "SELECT updated_by_id FROM staff WHERE last_name = ?", UUID.class, LAST_NAME));
    }
}
