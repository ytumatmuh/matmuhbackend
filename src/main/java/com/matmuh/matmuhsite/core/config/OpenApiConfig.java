package com.matmuh.matmuhsite.core.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI matmuhOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Matmuh Backend API")
                        .description("YTÜ Matematik Mühendisliği site backend'i: dersler, akademisyenler, ders notları, sınav istatistikleri ve inscribed CMS endpointleri.")
                        .version("v1")
                        .contact(new Contact().name("Matmuh").url("https://matmuh.yusufacmaci.com")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                .components(new Components().addSecuritySchemes(BEARER_SCHEME,
                        new SecurityScheme()
                                .name(BEARER_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
