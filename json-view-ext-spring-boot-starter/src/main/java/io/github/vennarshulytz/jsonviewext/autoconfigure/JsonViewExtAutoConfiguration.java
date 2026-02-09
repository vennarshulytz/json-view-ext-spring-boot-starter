package io.github.vennarshulytz.jsonviewext.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.vennarshulytz.jsonviewext.converter.JsonViewExtMappingJackson2HttpMessageConverter;
import io.github.vennarshulytz.jsonviewext.core.FilterRuleRegistry;
import io.github.vennarshulytz.jsonviewext.core.JsonViewExtModule;
import io.github.vennarshulytz.jsonviewext.handler.JsonViewExtResponseBodyAdvice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * JsonViewExt 自动配置类
 *
 * @author vennarshulytz
 * @since 1.0.0
 */
@Configuration
public class JsonViewExtAutoConfiguration {


    private static final Logger log = LoggerFactory.getLogger(JsonViewExtAutoConfiguration.class);

    @PostConstruct
    public void init() {
        log.info("JsonViewExt Spring Boot Starter initialized");
    }

    @Bean
    @ConditionalOnMissingBean
    public FilterRuleRegistry filterRuleRegistry() {
        return new FilterRuleRegistry();
    }

    @Bean
    @ConditionalOnMissingBean
    public JsonViewExtResponseBodyAdvice jsonViewExtResponseBodyAdvice(
            FilterRuleRegistry ruleRegistry) {
        return new JsonViewExtResponseBodyAdvice(ruleRegistry);
    }

    @Configuration
    @ConditionalOnWebApplication
    static class JsonViewExtWebMvcConfiguration implements WebMvcConfigurer {
        // 可以在此添加额外的 WebMvc 配置

        @Autowired
        private ObjectMapper objectMapper;

        /**
         * 注册自定义 HttpMessageConverter，优先级最高
         */
        @Override
        public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
            ObjectMapper filterMapper = objectMapper.copy();
            filterMapper.registerModule(new JsonViewExtModule());

            converters.add(0, new JsonViewExtMappingJackson2HttpMessageConverter(objectMapper, filterMapper));
        }
    }


}
