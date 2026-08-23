package com.matmuh.matmuhsite.webAPI.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.matmuh.matmuhsite.business.abstracts.SecurityService;
import com.matmuh.matmuhsite.business.abstracts.UserService;
import com.matmuh.matmuhsite.business.constants.UserMessages;
import com.matmuh.matmuhsite.core.dtos.common.PageDto;
import com.matmuh.matmuhsite.core.dtos.user.response.UserDto;
import com.matmuh.matmuhsite.core.exceptions.BusinessRuleException;
import com.matmuh.matmuhsite.core.helpers.MessageResolver;
import com.matmuh.matmuhsite.core.helpers.PageableSanitizer;
import com.matmuh.matmuhsite.core.mappers.UserMapper;
import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.SuccessDataResult;
import com.matmuh.matmuhsite.entities.Role;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.UUID;

@Tag(name = "Users", description = "Kullanıcı işlemleri")
@RestController
@RequestMapping("api/users")
public class UserController {

    private static final Set<String> SORTABLE = Set.of("email", "firstName", "lastName");

    private final SecurityService securityService;
    private final UserService userService;
    private final UserMapper userMapper;
    private final MessageResolver messageResolver;

    public UserController(SecurityService securityService, UserService userService, UserMapper userMapper, MessageResolver messageResolver) {
        this.securityService = securityService;
        this.userService = userService;
        this.userMapper = userMapper;
        this.messageResolver = messageResolver;
    }

    @Operation(summary = "Mevcut kullanıcı", description = "Giriş yapmış kullanıcının bilgilerini döner.")
    @GetMapping("/me")
    public ResponseEntity<DataResult<UserDto>> getCurrentUser() {
        var user = securityService.getAuthenticatedUserFromDatabase();
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessDataResult<>(userMapper.toDto(user), messageResolver.resolve(UserMessages.USER_FETCHED_SUCCESSFULLY), HttpStatus.OK));
    }

    @Operation(summary = "Kullanıcıları listele",
            description = "Sayfalı kullanıcı listesi. Filtreler: role (ADMIN/EDITOR/USER), search (ad, soyad, e-posta). "
                    + "Sıralanabilir alanlar: email, firstName, lastName (ADMIN).")
    @GetMapping
    public ResponseEntity<DataResult<PageDto<UserDto>>> getUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String search,
            @ParameterObject @PageableDefault(size = 20, sort = "email", direction = Sort.Direction.ASC) Pageable pageable) {
        var result = userService.getUsers(parseRole(role, true), search, PageableSanitizer.sanitize(pageable, SORTABLE, "email"));
        return ResponseEntity.ok(new SuccessDataResult<>(result, messageResolver.resolve(UserMessages.USERS_FETCHED_SUCCESSFULLY), HttpStatus.OK));
    }

    @Operation(summary = "Kullanıcı getir", description = "ID ile tek kullanıcı döner (ADMIN).")
    @GetMapping("/{id}")
    public ResponseEntity<DataResult<UserDto>> getUserById(@PathVariable UUID id) {
        var user = userService.getUserById(id);
        return ResponseEntity.ok(new SuccessDataResult<>(user, messageResolver.resolve(UserMessages.USER_FETCHED_SUCCESSFULLY), HttpStatus.OK));
    }

    @Operation(summary = "Rol ver",
            description = "Kullanıcıya rol ekler. Yalnızca EDITOR verilebilir; USER zaten herkeste var, ADMIN bu uçtan verilemez. "
                    + "Tekrar çağrılırsa durum değişmez (ADMIN).")
    @PutMapping("/{id}/roles/{role}")
    public ResponseEntity<DataResult<UserDto>> grantRole(@PathVariable UUID id, @PathVariable String role) {
        var updated = userService.grantRole(id, parseRole(role, false));
        return ResponseEntity.ok(new SuccessDataResult<>(updated, messageResolver.resolve(UserMessages.ROLE_GRANTED), HttpStatus.OK));
    }

    @Operation(summary = "Rolü al",
            description = "Kullanıcıdan rolü çıkarır. Yalnızca EDITOR alınabilir; USER hiçbir zaman çıkarılmaz. "
                    + "Tekrar çağrılırsa durum değişmez (ADMIN).")
    @DeleteMapping("/{id}/roles/{role}")
    public ResponseEntity<DataResult<UserDto>> revokeRole(@PathVariable UUID id, @PathVariable String role) {
        var updated = userService.revokeRole(id, parseRole(role, false));
        return ResponseEntity.ok(new SuccessDataResult<>(updated, messageResolver.resolve(UserMessages.ROLE_REVOKED), HttpStatus.OK));
    }

    private Role parseRole(String raw, boolean optional) {
        if (raw == null || raw.isBlank()) {
            if (optional) {
                return null;
            }
            throw new BusinessRuleException(UserMessages.ROLE_UNKNOWN);
        }
        var parsed = Role.fromValue(raw);
        if (parsed == null) {
            throw new BusinessRuleException(UserMessages.ROLE_UNKNOWN);
        }
        return parsed;
    }
}
