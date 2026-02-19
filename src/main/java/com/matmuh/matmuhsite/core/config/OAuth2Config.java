package com.matmuh.matmuhsite.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

@Configuration
public class OAuth2Config {

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository(
            @Value("${spring.security.oauth2.client.registration.azure.client-id}") String clientId,
            @Value("${spring.security.oauth2.client.registration.azure.client-secret}") String clientSecret,
            @Value("${spring.security.oauth2.client.provider.azure.authorization-uri}") String authorizationUri,
            @Value("${spring.security.oauth2.client.provider.azure.token-uri}") String tokenUri,
            @Value("${spring.security.oauth2.client.provider.azure.jwk-set-uri}") String jwkSetUri) {

        ClientRegistration azureRegistration = ClientRegistration.withRegistrationId("azure")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope("openid", "profile", "email", "offline_access")
                .authorizationUri(authorizationUri)
                .tokenUri(tokenUri)
                .jwkSetUri(jwkSetUri)
                .userNameAttributeName("preferred_username")
                .clientName("YTU Giris")
                .build();

        return new InMemoryClientRegistrationRepository(azureRegistration);
    }
}