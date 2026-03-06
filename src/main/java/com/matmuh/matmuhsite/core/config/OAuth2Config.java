package com.matmuh.matmuhsite.core.config;

import com.matmuh.matmuhsite.core.properties.AzureProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

@Configuration
@RequiredArgsConstructor
public class OAuth2Config {

   private final AzureProperties azureProperties;

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        AzureProperties.Registration reg = azureProperties.getRegistration().get("azure");
        AzureProperties.Provider prov = azureProperties.getProvider().get("azure");

        ClientRegistration azureRegistration = ClientRegistration
                .withRegistrationId("azure")
                .clientId(reg.getClientId())
                .clientSecret(reg.getClientSecret())
                .clientAuthenticationMethod(new ClientAuthenticationMethod(reg.getClientAuthenticationMethod()))
                .authorizationGrantType(new AuthorizationGrantType(reg.getAuthorizationGrantType()))
                .redirectUri(reg.getRedirectUri())
                .scope(reg.getScope().toArray(new String[0]))
                .authorizationUri(prov.getAuthorizationUri())
                .tokenUri(prov.getTokenUri())
                .jwkSetUri(prov.getJwkSetUri())
                .userNameAttributeName(prov.getUserNameAttribute())
                .clientName(reg.getClientName())
                .build();

        return new InMemoryClientRegistrationRepository(azureRegistration);
    }

    @Bean
    public OAuth2AuthorizationRequestResolver authorizationRequestResolver(
            ClientRegistrationRepository clientRegistrationRepository) {

        String redirectUri = azureProperties.getRegistration().get("azure").getRedirectUri();

        DefaultOAuth2AuthorizationRequestResolver resolver =
                new DefaultOAuth2AuthorizationRequestResolver(
                        clientRegistrationRepository,
                        "/api/oauth2/microsoft"
                );

        resolver.setAuthorizationRequestCustomizer(customizer ->
                customizer
                        .redirectUri(redirectUri)
                        .attributes(attrs -> {
                            attrs.remove("code_challenge");
                            attrs.remove("code_challenge_method");
                        })
        );

        return resolver;
    }

    @Bean
    public OAuth2AuthorizedClientService authorizedClientService(
            ClientRegistrationRepository clientRegistrationRepository) {
        return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
    }
}