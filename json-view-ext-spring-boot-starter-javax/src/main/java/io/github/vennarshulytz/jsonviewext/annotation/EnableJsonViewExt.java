package io.github.vennarshulytz.jsonviewext.annotation;

import io.github.vennarshulytz.jsonviewext.autoconfigure.JsonViewExtAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用 JsonViewExt 功能注解
 *
 * @author vennarshulytz
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({JacksonAutoConfiguration.class, JsonViewExtAutoConfiguration.class})
public @interface EnableJsonViewExt {
}
