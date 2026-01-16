package io.github.vennarshulytz.jsonviewext.autoconfigure;

import io.github.vennarshulytz.jsonviewext.core.FilterRuleRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

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
}
