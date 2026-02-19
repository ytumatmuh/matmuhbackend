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
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Set;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserService userService;

    public OAuth2LoginSuccessHandler(JwtService jwtService, UserService userService) {
        this.jwtService = jwtService;
        this.userService = userService;
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
        logger.info("OAuth2 kullanıcı bilgileri alındı: email=" + email + ", name=" + name + ", providerId=" + providerId + ", tenantId=" + tenantId);
        if (!"85602908-e15b-43ba-9148-38bc773a816e".equals(tenantId)) {
            logger.info("Hatalı organizasyon girişi tespit edildi: " + tenantId);
            throw new RuntimeException("Hatalı organizasyon girişi!");
        }


        if (!email.endsWith("@std.yildiz.edu.tr")) {
            logger.info("Geçersiz e-posta alanı tespit edildi: " + email);
            throw new EmailDoesntFromYildizException("Sadece Yıldız Teknik Üniversitesi öğrencileri giriş yapabilir!");
        }

        String token;
        try {
            logger.info("Giriş işlemi başarılı, kullanıcı bulundu: " + email);
            UserDto user = userService.getUserByEmail(email);
            User userEntity = new User();
            userEntity.setId(user.getId());
            userEntity.setFirstName(user.getFirstName());
            userEntity.setLastName(user.getLastName());
            userEntity.setEmail(user.getEmail());
            userEntity.setAuthorities(user.getAuthorities());

            token = jwtService.generateToken(userEntity);

        }catch (ResourceNotFoundException e){
            logger.info("Yeni kullanıcı tespit edildi, veritabanına kaydediliyor: " + email);
            User newUser = new User().builder()
                    .email(email)
                    .firstName(name)
                    .authorities(Set.of(Role.ROLE_USER))
                    .provider(AuthProvider.YTU_MAIL)
                    .providerId(providerId)
                    .isEmailVerified(true)
                    .build();

            userService.createUserFromOauth2(newUser);

            token = jwtService.generateToken(newUser);
        }catch (Exception e){
            logger.info("Giriş işlemi sırasında beklenmeyen bir hata oluştu: " + e.getMessage());
            throw new RuntimeException("Giriş işlemi sırasında bir hata oluştu: " + e.getMessage());
         }

        String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:3000/oauth-success")
                .queryParam("token", token)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);



        }


}
