package com.matmuh.matmuhsite.core.config;

import com.matmuh.matmuhsite.business.abstracts.ServiceKeyService;
import com.matmuh.matmuhsite.core.dtos.serviceKey.request.CreateServiceKeyRequestDto;
import com.matmuh.matmuhsite.entities.ServiceKeyCapability;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import jakarta.servlet.Filter;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Egehan'ın botu: CONTENT_WRITE anahtarı koleksiyon yazma, medya, toplu aktarım ve dört
// sağlayıcı koleksiyonun REST silmesini yapabilir; katalog REST POST'u ve sync yapamaz.
@SpringBootTest
class ContentWriteServiceKeyAccessTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    @org.springframework.beans.factory.annotation.Qualifier("springSecurityFilterChain")
    private Filter securityFilterChain;

    private MockMvc mvc;

    @Autowired
    private ServiceKeyService serviceKeys;

    private UUID keyId;
    private String bearer;

    @BeforeEach
    void issueKey() {
        mvc = MockMvcBuilders.webAppContextSetup(context).addFilters(securityFilterChain).build();
        var created = serviceKeys.create(new CreateServiceKeyRequestDto(
                "test-bot", "content-write-access-test", Set.of(ServiceKeyCapability.CONTENT_WRITE), null));
        keyId = created.id();
        bearer = "Bearer " + created.key();
    }

    @AfterEach
    void revokeKey() {
        serviceKeys.revoke(keyId);
    }

    private int statusOf(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request) throws Exception {
        return mvc.perform(request.header("Authorization", bearer)).andReturn().getResponse().getStatus();
    }

    @Test
    void contentWriteKeyPassesTheGateOnTheBotsEndpoints() throws Exception {
        for (var request : new org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder[]{
                post("/api/cms/collections/news?locale=tr").contentType(MediaType.APPLICATION_JSON).content("{\"data\":{}}"),
                post("/api/cms/media"),
                post("/api/lecture-offerings/import").contentType(MediaType.APPLICATION_JSON).content("{\"rows\":[]}"),
                delete("/api/lectures/" + UUID.randomUUID()),
                delete("/api/staff/" + UUID.randomUUID()),
                delete("/api/elective-groups/" + UUID.randomUUID()),
                delete("/api/lecture-offerings/" + UUID.randomUUID()),
                delete("/api/calendar-admin/terms/" + UUID.randomUUID())}) {
            var status = statusOf(request);
            assertNotEquals(401, status, request.toString());
            assertNotEquals(403, status, request.toString());
        }
    }

    @Test
    void contentWriteKeyIsRefusedElsewhere() throws Exception {
        assertEquals(403, statusOf(post("/api/lectures").contentType(MediaType.APPLICATION_JSON).content("{}")));
        assertEquals(403, statusOf(post("/api/cms/sync?locales=tr").contentType(MediaType.APPLICATION_JSON).content("[]")));
        mvc.perform(post("/api/cms/media")).andExpect(status().isUnauthorized());
    }
}
