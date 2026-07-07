package io.github.vennarshulytz.jsonviewext.config;

/**
 * JsonViewExt 配置属性
 *
 * @author vennarshulytz
 * @since 1.2.0
 */
public class JsonViewExtProperties {

    public static final long DEFAULT_CACHE_MAXIMUM_SIZE = 1024L;
    public static final boolean DEFAULT_FAIL_FAST = false;
    public static final boolean DEFAULT_RETURN_ORIGINAL_ON_DESENSITIZATION_ERROR = true;
    public static final boolean DEFAULT_DEBUG_TRACE_ENABLED = false;

    private final long cacheMaximumSize;
    private final boolean failFast;
    private final boolean returnOriginalOnDesensitizationError;
    private final boolean debugTraceEnabled;

    public JsonViewExtProperties(long cacheMaximumSize) {
        this(cacheMaximumSize, DEFAULT_FAIL_FAST,
                DEFAULT_RETURN_ORIGINAL_ON_DESENSITIZATION_ERROR,
                DEFAULT_DEBUG_TRACE_ENABLED);
    }

    public JsonViewExtProperties(long cacheMaximumSize,
                                 boolean failFast,
                                 boolean returnOriginalOnDesensitizationError,
                                 boolean debugTraceEnabled) {
        if (cacheMaximumSize <= 0) {
            throw new IllegalArgumentException(
                    "cacheMaximumSize must be positive, got: " + cacheMaximumSize);
        }
        this.cacheMaximumSize = cacheMaximumSize;
        this.failFast = failFast;
        this.returnOriginalOnDesensitizationError = returnOriginalOnDesensitizationError;
        this.debugTraceEnabled = debugTraceEnabled;
    }

    public static JsonViewExtProperties defaultProperties() {
        return new JsonViewExtProperties(DEFAULT_CACHE_MAXIMUM_SIZE);
    }

    public long getCacheMaximumSize() {
        return cacheMaximumSize;
    }

    public boolean isFailFast() {
        return failFast;
    }

    public boolean isReturnOriginalOnDesensitizationError() {
        return returnOriginalOnDesensitizationError;
    }

    public boolean isDebugTraceEnabled() {
        return debugTraceEnabled;
    }

    @Override
    public String toString() {
        return "JsonViewExtProperties{" +
                "cacheMaximumSize=" + cacheMaximumSize +
                ", failFast=" + failFast +
                ", returnOriginalOnDesensitizationError=" + returnOriginalOnDesensitizationError +
                ", debugTraceEnabled=" + debugTraceEnabled +
                '}';
    }
}
