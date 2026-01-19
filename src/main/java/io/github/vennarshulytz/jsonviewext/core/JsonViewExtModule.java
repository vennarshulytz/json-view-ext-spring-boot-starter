package io.github.vennarshulytz.jsonviewext.core;

import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Jackson Module，注册自定义序列化修改器
 *
 * @author vennarshulytz
 * @since 1.0.0
 */
public class JsonViewExtModule extends SimpleModule {

    private static final String MODULE_NAME = "JsonViewExtModule";

    private static final long serialVersionUID = 1L;

    public JsonViewExtModule() {
        super(MODULE_NAME);
    }

    @Override
    public void setupModule(SetupContext context) {
        super.setupModule(context);
        context.addBeanSerializerModifier(new JsonViewExtBeanSerializerModifier());
    }
}

