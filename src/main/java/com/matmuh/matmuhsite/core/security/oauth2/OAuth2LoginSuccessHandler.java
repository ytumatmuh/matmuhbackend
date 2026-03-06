package com.matmuh.matmuhsite.core.security.oauth2;

import com.matmuh.matmuhsite.business.abstracts.UserService;
import com.matmuh.matmuhsite.core.dtos.user.response.UserDto;
import com.matmuh.matmuhsite.core.exceptions.EmailDoesntFromYildizException;
import com.matmuh.matmuhsite.core.exceptions.ResourceNotFoundException;
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
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserService userService;

    private final OAuth2AuthorizedClientService authorizedClientService;

    private Logger logger = LoggerFactory.getLogger(OAuth2LoginSuccessHandler.class);

    public OAuth2LoginSuccessHandler(JwtService jwtService, UserService userService, OAuth2AuthorizedClientService authorizedClientService) {
        this.jwtService = jwtService;
        this.userService = userService;
        this.authorizedClientService = authorizedClientService;
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


        if (!"85602908-e15b-43ba-9148-38bc773a816e".equals(tenantId)) {
            throw new RuntimeException("Hatalı organizasyon girişi!");
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


        String token;
        try {
            UserDto user = userService.getUserByEmail(email);
            User userEntity = new User();
            userEntity.setId(user.getId());
            userEntity.setFirstName(user.getFirstName());
            userEntity.setLastName(user.getLastName());
            userEntity.setEmail(user.getEmail());
            userEntity.setAuthorities(user.getAuthorities());
            userEntity.setDepartment(department);

            token = jwtService.generateToken(userEntity);
            logger.info("Mevcut kullanıcı için token üretildi: {}", email);

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
            token = jwtService.generateToken(newUser);
        } catch (Exception e) {
            logger.error("Giriş hatası: {}", e.getMessage());
            throw new RuntimeException("Giriş işlemi sırasında hata: " + e.getMessage());
        }

        String cookieValue = "jwt=" + token + "; Path=/; HttpOnly; Secure; SameSite=None; MaxAge=604800";
        response.addHeader("Set-Cookie", cookieValue);

        logger.info("Cookie set edildi, frontend'e yönlendiriliyor.");
        getRedirectStrategy().sendRedirect(request, response, "https://matmuh.yusufacmaci.com/oauth-success");
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
