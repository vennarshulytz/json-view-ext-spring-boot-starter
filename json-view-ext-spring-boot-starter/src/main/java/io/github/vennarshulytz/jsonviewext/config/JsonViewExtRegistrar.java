package io.github.vennarshulytz.jsonviewext.config;

import io.github.vennarshulytz.jsonviewext.annotation.EnableJsonViewExt;
import io.github.vennarshulytz.jsonviewext.autoconfigure.JsonViewExtAutoConfiguration;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

/**
 * JsonViewExt 配置注册器
 *
 * @author vennarshulytz
 * @since 1.2.0
 */
public class JsonViewExtRegistrar implements ImportBeanDefinitionRegistrar {

    private static final String PROPS_BEAN_NAME = "jsonViewExtProperties";
    private static final String JACKSON_CONFIG_BEAN_NAME = "jacksonAutoConfiguration";
    private static final String JSON_VIEW_CONFIG_BEAN_NAME = "jsonViewExtAutoConfiguration";

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(
                importingClassMetadata.getAnnotationAttributes(EnableJsonViewExt.class.getName()));

        if (attributes == null) {
            return;
        }

        Number cacheMaximumSize = attributes.getNumber("cacheMaximumSize");
        if (!registry.containsBeanDefinition(PROPS_BEAN_NAME)) {
            RootBeanDefinition propsDef = new RootBeanDefinition(JsonViewExtProperties.class);
            propsDef.getConstructorArgumentValues()
                    .addIndexedArgumentValue(0, cacheMaximumSize.longValue());
            registry.registerBeanDefinition(PROPS_BEAN_NAME, propsDef);
        }

        if (!registry.containsBeanDefinition(JACKSON_CONFIG_BEAN_NAME)) {
            registry.registerBeanDefinition(JACKSON_CONFIG_BEAN_NAME,
                    new RootBeanDefinition(JacksonAutoConfiguration.class));
        }

        if (!registry.containsBeanDefinition(JSON_VIEW_CONFIG_BEAN_NAME)) {
            registry.registerBeanDefinition(JSON_VIEW_CONFIG_BEAN_NAME,
                    new RootBeanDefinition(JsonViewExtAutoConfiguration.class));
        }
    }
}
