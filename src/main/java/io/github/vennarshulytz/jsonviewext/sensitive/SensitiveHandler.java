package io.github.vennarshulytz.jsonviewext.sensitive;

import io.github.vennarshulytz.jsonviewext.exception.ClassInstantiationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 脱敏处理器，负责管理和执行脱敏操作
 *
 * @author vennarshulytz
 * @since 1.0.0
 */
public class SensitiveHandler {

    private static final Logger log = LoggerFactory.getLogger(SensitiveHandler.class);

    /**
     * 脱敏处理器缓存
     */
    private static final Map<Class<? extends SensitiveType>, SensitiveType> HANDLER_CACHE = new ConcurrentHashMap<>();

    private SensitiveHandler() {
    }

    /**
     * 获取脱敏处理器实例
     */
    public static SensitiveType getHandler(Class<? extends SensitiveType> handlerClass) {
        return HANDLER_CACHE.computeIfAbsent(handlerClass, clazz -> {
            try {
                return clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                String name = clazz.getName();
                log.error("Failed to create SensitiveType instance for class: {}", name);
                throw new ClassInstantiationException("Cannot instantiate SensitiveType: " + name, e);
            }
        });
    }

    /**
     * 执行脱敏操作
     */
    public static String desensitize(Class<? extends SensitiveType> handlerClass, String value) {
        if (value == null || handlerClass == null) {
            return value;
        }
        try {
            SensitiveType handler = getHandler(handlerClass);
            return handler.desensitize(value);
        } catch (Exception e) {
            log.warn("Desensitization failed for value, returning original value", e);
            return value;
        }
    }

    /**
     * 注册自定义脱敏处理器
     */
    public static void registerHandler(Class<? extends SensitiveType> handlerClass,
                                       SensitiveType handler) {
        HANDLER_CACHE.put(handlerClass, handler);
    }

    /**
     * 清除缓存（主要用于测试）
     */
    public static void clearCache() {
        HANDLER_CACHE.clear();
    }
}
