package com.marry.common.core.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marry.common.core.domain.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * Wrap controller return values in {@link R} for consistent client parsing.
 * Skips values that already are {@link R}, {@code String}, primitive wrappers, or marked with
 * {@link com.marry.common.core.web.NoWrapResponse}.
 */
@Slf4j
@RestControllerAdvice(basePackages = "com.marry")
public class CommonResponseAdvice implements ResponseBodyAdvice<Object> {

    private final ObjectMapper mapper;

    public CommonResponseAdvice(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        if (returnType.getParameterType().equals(R.class) || returnType.getParameterType().equals(String.class)) {
            return false;
        }
        return !returnType.hasMethodAnnotation(NoWrapResponse.class);
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        if (body instanceof R) {
            return body;
        }
        // String body needs special handling — must return String when using StringHttpMessageConverter
        if (body instanceof String s) {
            try {
                response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                return mapper.writeValueAsString(R.ok(s));
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize String response", e);
                return R.fail(500, "Response serialization failed");
            }
        }
        return R.ok(body);
    }
}