package com.matmuh.matmuhsite.core.security;

import com.matmuh.matmuhsite.business.abstracts.ServiceKeyService;
import com.matmuh.matmuhsite.core.helpers.ServiceKeyFormat;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ServiceKeyAuthFilter extends OncePerRequestFilter {

    private final Logger logger = LoggerFactory.getLogger(ServiceKeyAuthFilter.class);

    private final ServiceKeyService serviceKeyService;

    public ServiceKeyAuthFilter(ServiceKeyService serviceKeyService) {
        this.serviceKeyService = serviceKeyService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        var rawKey = bearerToken(request);

        if (ServiceKeyFormat.looksLikeServiceKey(rawKey)) {
            serviceKeyService.authenticate(rawKey).ifPresentOrElse(key -> {
                var authorities = key.getCapabilities().stream()
                        .map(capability -> new SimpleGrantedAuthority("ROLE_" + capability.name()))
                        .toList();

                var authentication = new UsernamePasswordAuthenticationToken(
                        "service:" + key.getId(), null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                logger.debug("Service key authenticated: {} capabilities={}", key.getName(), key.getCapabilities());
            }, () -> logger.warn("Rejected service key with prefix: {}", ServiceKeyFormat.lookupPrefix(rawKey)));
        }

        filterChain.doFilter(request, response);
    }

    private String bearerToken(HttpServletRequest request) {
        var header = request.getHeader("Authorization");
        return header != null && header.startsWith("Bearer ") ? header.substring(7).trim() : null;
    }
}
