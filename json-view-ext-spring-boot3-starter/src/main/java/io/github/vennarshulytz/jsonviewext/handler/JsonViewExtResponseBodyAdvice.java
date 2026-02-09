package io.github.vennarshulytz.jsonviewext.handler;

import io.github.vennarshulytz.jsonviewext.converter.JsonViewExtMappingJackson2HttpMessageConverter;
import io.github.vennarshulytz.jsonviewext.core.FilterRuleRegistry;
import io.github.vennarshulytz.jsonviewext.model.FilterContext;
import io.github.vennarshulytz.jsonviewext.model.FilteredResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.lang.reflect.Method;

/**
* ResponseBodyAdvice 实现，拦截 Controller 返回值并包装为 FilteredResponse
*
* @author vennarshulytz
* @since 1.1.0
*/
@ControllerAdvice
public class JsonViewExtResponseBodyAdvice implements ResponseBodyAdvice<Object> {

   private static final Logger log = LoggerFactory.getLogger(JsonViewExtResponseBodyAdvice.class);

   private final FilterRuleRegistry ruleRegistry;

   public JsonViewExtResponseBodyAdvice(FilterRuleRegistry ruleRegistry) {
       this.ruleRegistry = ruleRegistry;
   }

   @Override
   public boolean supports(MethodParameter returnType,
                           Class<? extends HttpMessageConverter<?>> converterType) {
       // 只处理 JSON 响应且有 @JsonViewExt 注解的方法
       Method method = returnType.getMethod();
       if (method == null) {
           return false;
       }

       return JsonViewExtMappingJackson2HttpMessageConverter.class.isAssignableFrom(converterType) && ruleRegistry.hasJsonViewExtAnnotation(method);
   }

   @Override
   public Object beforeBodyWrite(Object body,
                                 MethodParameter returnType,
                                 MediaType selectedContentType,
                                 Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                 ServerHttpRequest request,
                                 ServerHttpResponse response) {
       if (body == null) {
           return null;
       }

       Method method = returnType.getMethod();
       if (method == null) {
           return body;
       }

       // 避免重复包装
       if (body instanceof FilteredResponse) {
           return body;
       }

       try {
           // 获取或创建过滤上下文
           FilterContext context = ruleRegistry.getOrCreateContext(method);

           if (!context.hasRules()) {
               return body;
           }

           log.debug("Wrapping response with FilteredResponse for method: {}", method.getName());
           return new FilteredResponse(body, context);

       } catch (Exception e) {
           log.error("Error creating FilteredResponse, returning original body", e);
           return body;
       }
   }
}
