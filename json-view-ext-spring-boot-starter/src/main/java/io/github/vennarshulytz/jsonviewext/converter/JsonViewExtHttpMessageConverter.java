package io.github.vennarshulytz.jsonviewext.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.vennarshulytz.jsonviewext.annotation.JsonViewExt;
import io.github.vennarshulytz.jsonviewext.core.FilterRuleRegistry;
import io.github.vennarshulytz.jsonviewext.core.JsonViewExtBeanSerializerModifier;
import io.github.vennarshulytz.jsonviewext.core.JsonViewExtContextHolder;
import io.github.vennarshulytz.jsonviewext.model.FilterContext;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.lang.Nullable;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

/**
 * 自定义 HttpMessageConverter
 *
 * @author vennarshulytz
 * @since 1.1.0
 */
public class JsonViewExtHttpMessageConverter extends AbstractHttpMessageConverter<Object>  {

    private final ObjectMapper filterObjectMapper;
    private final FilterRuleRegistry ruleRegistry;

    public JsonViewExtHttpMessageConverter(ObjectMapper filterObjectMapper, FilterRuleRegistry ruleRegistry) {
        super(StandardCharsets.UTF_8, MediaType.APPLICATION_JSON,
                new MediaType("application", "*+json", StandardCharsets.UTF_8));
        this.filterObjectMapper = filterObjectMapper;
        this.ruleRegistry = ruleRegistry;
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return true;
    }

    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        return super.canWrite(mediaType) && hasJsonViewExtAnnotation();
    }

    private boolean hasJsonViewExtAnnotation() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            return false;
        }

        HttpServletRequest request = attributes.getRequest();
        Object handler = request.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE);

        if (!(handler instanceof HandlerMethod)) {
            return false;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;

        Method method = handlerMethod.getMethod();
        boolean exist = method.isAnnotationPresent(JsonViewExt.class);
        if (exist) {
            FilterContext context = ruleRegistry.getOrCreateContext(method);
            if (!context.hasRules()) {
                return false;
            }

            JsonViewExtContextHolder.setContext(context);
            JsonViewExtBeanSerializerModifier.PathTracker.clear();

            return true;
        }

        return false;
    }

    @Override
    protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        throw new UnsupportedOperationException("JsonViewExtHttpMessageConverter does not support reading");
    }

    @Override
    public boolean canRead(Class<?> clazz, @Nullable MediaType mediaType) {
        return false;
    }

    @Override
    protected void writeInternal(Object filteredResponse, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        try {
            filterObjectMapper.writeValue(outputMessage.getBody(), filteredResponse);
        } finally {
            // 清理上下文
            JsonViewExtContextHolder.clear();
            JsonViewExtBeanSerializerModifier.PathTracker.reset();
        }
    }
}
