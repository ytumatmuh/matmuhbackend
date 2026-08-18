package com.matmuh.matmuhsite.core.config;

import org.springdoc.webmvc.ui.SwaggerIndexTransformer;
import org.springdoc.webmvc.ui.SwaggerResourceResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SwaggerResourceConfig implements WebMvcConfigurer {

    private static final String WEB_JARS_LOCATION = "classpath:/META-INF/resources/webjars/";

    private final SwaggerIndexTransformer swaggerIndexTransformer;
    private final SwaggerResourceResolver swaggerResourceResolver;

    public SwaggerResourceConfig(SwaggerIndexTransformer swaggerIndexTransformer,
                                 SwaggerResourceResolver swaggerResourceResolver) {
        this.swaggerIndexTransformer = swaggerIndexTransformer;
        this.swaggerResourceResolver = swaggerResourceResolver;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/api/swagger-ui/**")
                .addResourceLocations(WEB_JARS_LOCATION)
                .resourceChain(false)
                .addResolver(swaggerResourceResolver)
                .addTransformer(swaggerIndexTransformer);
    }
}
