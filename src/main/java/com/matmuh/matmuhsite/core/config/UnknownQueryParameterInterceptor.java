package com.matmuh.matmuhsite.core.config;

import com.matmuh.matmuhsite.business.constants.CommonMessages;
import com.matmuh.matmuhsite.core.exceptions.BusinessRuleException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

@Component
public class UnknownQueryParameterInterceptor implements HandlerInterceptor {

    private static final Set<String> PAGEABLE_PARAMETERS = Set.of("page", "size", "sort");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        var queryString = request.getQueryString();
        if (!(handler instanceof HandlerMethod handlerMethod) || queryString == null || queryString.isBlank()) {
            return true;
        }

        var declared = declaredParameters(handlerMethod);
        if (declared == null) {
            return true;
        }

        var unknown = new TreeSet<String>();
        for (var pair : queryString.split("&")) {
            if (pair.isBlank()) {
                continue;
            }

            var separator = pair.indexOf('=');
            var name = URLDecoder.decode(separator < 0 ? pair : pair.substring(0, separator), StandardCharsets.UTF_8);

            if (!name.isBlank() && !declared.contains(name)) {
                unknown.add(name);
            }
        }

        if (!unknown.isEmpty()) {
            throw new BusinessRuleException(CommonMessages.UNKNOWN_QUERY_PARAMETERS, String.join(", ", unknown));
        }

        return true;
    }


    private Set<String> declaredParameters(HandlerMethod handlerMethod) {
        var declared = new LinkedHashSet<String>();

        for (MethodParameter parameter : handlerMethod.getMethodParameters()) {
            var type = parameter.getParameterType();

            if (Pageable.class.isAssignableFrom(type) || Sort.class.isAssignableFrom(type)) {
                declared.addAll(PAGEABLE_PARAMETERS);
                continue;
            }

            var annotation = parameter.getParameterAnnotation(RequestParam.class);
            if (annotation == null) {
                continue;
            }

            if (Map.class.isAssignableFrom(type)) {
                return null;
            }

            var name = annotation.value().isBlank() ? annotation.name() : annotation.value();
            declared.add(name.isBlank() ? parameter.getParameterName() : name);
        }

        return declared;
    }
}
