package io.github.vennarshulytz.jsonviewext.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.vennarshulytz.jsonviewext.core.FilterRuleRegistry;
import io.github.vennarshulytz.jsonviewext.core.JsonViewExtBeanSerializerModifier;
import io.github.vennarshulytz.jsonviewext.core.JsonViewExtContextHolder;
import io.github.vennarshulytz.jsonviewext.model.FilterContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * ResponseBodyAdvice 实现，拦截 Controller 返回值并应用过滤规则
 *
 * @author vennarshulytz
 * @since 1.0.0
 */
@ControllerAdvice
public class JsonViewExtResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    private static final Logger log = LoggerFactory.getLogger(JsonViewExtResponseBodyAdvice.class);

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
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType mediaType, Class<? extends HttpMessageConverter<?>> aClass, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        if (body == null) {
            return null;
        }

        Method method = returnType.getMethod();
        if (method == null) {
            return body;
        }

        try {
            // 获取或创建过滤上下文
            FilterContext context = ruleRegistry.getOrCreateContext(method);

            if (!context.hasRules()) {
                return body;
            }

            // 设置上下文
            JsonViewExtContextHolder.setContext(context);
            JsonViewExtBeanSerializerModifier.PathTracker.clear();

            // 序列化为 JSON 字符串
            String json = objectMapper.writeValueAsString(body);

            log.debug("Filtered JSON output: {}", json);

            // 返回 JSON 字符串，由 StringHttpMessageConverter 处理
            // 或者返回 JsonNode，由 Jackson 处理
            return objectMapper.readTree(json);

        } catch (IOException e) {
            log.error("Error processing JSON with filter", e);
            return body;
        } finally {
            // 清理上下文
            JsonViewExtContextHolder.clear();
            JsonViewExtBeanSerializerModifier.PathTracker.reset();
        }
    }
}
