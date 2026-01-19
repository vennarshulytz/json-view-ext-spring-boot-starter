package io.github.vennarshulytz.jsonviewext.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.vennarshulytz.jsonviewext.core.FilterRuleRegistry;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.lang.reflect.Method;

/**
 * ResponseBodyAdvice 实现，拦截 Controller 返回值并应用过滤规则
 *
 * @author vennarshulytz
 * @since 1.0.0
 */
@ControllerAdvice
public class JsonViewExtResponseBodyAdvice implements ResponseBodyAdvice<Object> {


    private final FilterRuleRegistry ruleRegistry;

    private final ObjectMapper objectMapper;

    public JsonViewExtResponseBodyAdvice(FilterRuleRegistry ruleRegistry,
                                         ObjectMapper objectMapper) {
        this.ruleRegistry = ruleRegistry;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // 只处理 JSON 响应且有 @JsonViewExt 注解的方法
        Method method = returnType.getMethod();
        if (method == null) {
            return false;
        }
        return ruleRegistry.hasJsonViewExtAnnotation(method) && MappingJackson2HttpMessageConverter.class.isAssignableFrom(converterType);
    }

    @Override
    public Object beforeBodyWrite(Object o, MethodParameter methodParameter, MediaType mediaType, Class<? extends HttpMessageConverter<?>> aClass, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        return null;
    }
}
