package com.matmuh.matmuhsite.core.security.oauth2;

import com.matmuh.matmuhsite.business.abstracts.RefreshTokenService;
import com.matmuh.matmuhsite.business.abstracts.UserService;
import com.matmuh.matmuhsite.core.dtos.user.response.UserDto;
import com.matmuh.matmuhsite.core.exceptions.EmailDoesntFromYildizException;
import com.matmuh.matmuhsite.core.exceptions.ResourceNotFoundException;
import com.matmuh.matmuhsite.core.helpers.AuthCookieFactory;
import com.matmuh.matmuhsite.core.properties.FrontendProperties;
import com.matmuh.matmuhsite.core.security.JwtService;
import com.matmuh.matmuhsite.entities.AuthProvider;
import com.matmuh.matmuhsite.entities.Role;
import com.matmuh.matmuhsite.entities.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserService userService;

    private final OAuth2AuthorizedClientService authorizedClientService;

    private final RefreshTokenService refreshTokenService;

    private final AuthCookieFactory cookieFactory;

    @org.springframework.beans.factory.annotation.Value("${app.oauth2.allowed-tenant-id}")
    private String allowedTenantId;

    private final FrontendProperties frontendProperties;

    private Logger logger = LoggerFactory.getLogger(OAuth2LoginSuccessHandler.class);

    public OAuth2LoginSuccessHandler(JwtService jwtService, UserService userService, OAuth2AuthorizedClientService authorizedClientService, RefreshTokenService refreshTokenService,
                                     AuthCookieFactory cookieFactory,
                                     FrontendProperties frontendProperties) {
        this.jwtService = jwtService;
        this.userService = userService;
        this.authorizedClientService = authorizedClientService;
        this.refreshTokenService = refreshTokenService;
        this.cookieFactory = cookieFactory;
        this.frontendProperties = frontendProperties;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        if (email == null || email.isEmpty()) {
            email = oAuth2User.getAttribute("preferred_username");
        }

        String name = oAuth2User.getAttribute("name");
        String providerId = oAuth2User.getAttribute("oid");
        String tenantId = oAuth2User.getAttribute("tid");


        if (!allowedTenantId.equals(tenantId)) {
            throw new EmailDoesntFromYildizException("Hatalı organizasyon girişi!");
        }
        if (email == null || !email.endsWith("@std.yildiz.edu.tr")) {
            throw new EmailDoesntFromYildizException("Sadece Yıldız Teknik Üniversitesi öğrencileri giriş yapabilir!");
        }


        String rawDepartment = fetchDepartmentFromGraph(authentication);
        String department = normalizeDepartment(rawDepartment);

        logger.info("Raw department from graph: {}, Normalized department: {}", rawDepartment, department);

        if (department == null){
            logger.warn("Yetkisiz bölüm girişi saptandı: {}", rawDepartment);
            throw new RuntimeException("Hatalı bölüm girişi! Sadece Matematik Mühendisliği öğrencileri giriş yapabilir.");
        }

        String firstName = name;
        String lastName = "";

        if (name != null && name.contains(" ")) {
            int lastSpace = name.lastIndexOf(" ");
            firstName = name.substring(0, lastSpace);
            lastName = name.substring(lastSpace + 1);
        }


        User account;
        try {
            account = userService.getUserEntityByEmail(email);
            account.setDepartment(department);
            logger.info("Mevcut kullanıcı ile oturum açılıyor: {}", email);

        } catch (ResourceNotFoundException e) {
            logger.info("Yeni kullanıcı kaydediliyor: {}", email);
            User newUser = User.builder()
                    .email(email)
                    .firstName(firstName)
                    .lastName(lastName)
                    .department(department)
                    .authorities(Set.of(Role.ROLE_USER))
                    .provider(AuthProvider.YTU_MAIL)
                    .providerId(providerId)
                    .isEmailVerified(true)
                    .build();

            userService.createUserFromOauth2(newUser);
            account = userService.getUserEntityByEmail(email);
        }

        var tokens = refreshTokenService.issueTokens(account);

        response.addHeader("Set-Cookie", cookieFactory.access(tokens.getToken(), jwtService.getTokenValiditySeconds()));
        response.addHeader("Set-Cookie", cookieFactory.refresh(tokens.getRefreshToken()));

        var session = request.getSession(false);
        String target = frontendProperties.getCallbackUrl();
        if (session != null) {
            var stored = session.getAttribute(FrontendCallbackCapturingResolver.SESSION_KEY);
            if (stored instanceof String requested && frontendProperties.isAllowedCallback(requested)) {
                target = requested;
            }
            session.invalidate();
        }
        SecurityContextHolder.clearContext();

        logger.info("Oturum çerezleri set edildi, frontend'e yönlendiriliyor.");
        getRedirectStrategy().sendRedirect(request, response, target);
    }

    private String normalizeDepartment(String rawDepartment){
        if (rawDepartment == null){
            return null;
        }
        String dept = rawDepartment.trim();
        return switch (dept){
            case "Matematik Mühendisliği", "052" -> "Matematik Mühendisliği";
            case "Matematik Mühendisliği (İngilizce)", "058" -> "Matematik Mühendisliği (İngilizce)";
            default -> null;
        };
    }

    private String fetchDepartmentFromGraph(Authentication authentication) {
        try {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
            OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                    oauthToken.getAuthorizedClientRegistrationId(),
                    oauthToken.getName()
            );

            String accessToken = client.getAccessToken().getTokenValue();

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));

            ResponseEntity<Map> response = restTemplate.exchange(
                    "https://graph.microsoft.com/v1.0/me?$select=department",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    Map.class
            );

            if (response.getBody() != null) {
                logger.info("Graph API tüm response: {}", response.getBody());
                return (String) response.getBody().get("department");
            }
        } catch (Exception e) {
            logger.warn("Graph API'den bölüm alınamadı: {}", e.getMessage());
        }
        return null;
    }


}
