package com.matmuh.matmuhsite.core.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractJacksonHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class MultipartJsonConverterConfig implements WebMvcConfigurer {

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        for (HttpMessageConverter<?> converter : converters) {
            if (converter instanceof AbstractJacksonHttpMessageConverter jacksonConverter) {
                var supported = new ArrayList<>(jacksonConverter.getSupportedMediaTypes());
                if (!supported.contains(MediaType.TEXT_PLAIN)) {
                    supported.add(MediaType.TEXT_PLAIN);
                }
                if (!supported.contains(MediaType.APPLICATION_OCTET_STREAM)) {
                    supported.add(MediaType.APPLICATION_OCTET_STREAM);
                }
                jacksonConverter.setSupportedMediaTypes(supported);
            }
        }
    }
}
