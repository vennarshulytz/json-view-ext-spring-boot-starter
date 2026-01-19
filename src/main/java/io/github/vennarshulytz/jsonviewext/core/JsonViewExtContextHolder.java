package io.github.vennarshulytz.jsonviewext.core;


import io.github.vennarshulytz.jsonviewext.model.FilterContext;

/**
 * FilterContext 的线程本地存储
 *
 * @author zhen
 * @since 1.0.0
 */
public class JsonViewExtContextHolder {

    private static final ThreadLocal<FilterContext> CONTEXT_HOLDER = new ThreadLocal<>();

    private JsonViewExtContextHolder() {
    }

    /**
     * 设置当前线程的 FilterContext
     */
    public static void setContext(FilterContext context) {
        if (context == null) {
            CONTEXT_HOLDER.remove();
        } else {
            CONTEXT_HOLDER.set(context);
        }
    }

    /**
     * 获取当前线程的 FilterContext
     */
    public static FilterContext getContext() {
        return CONTEXT_HOLDER.get();
    }

    /**
     * 清除当前线程的 FilterContext
     */
    public static void clear() {
        CONTEXT_HOLDER.remove();
    }

    /**
     * 判断当前线程是否有 FilterContext
     */
    public static boolean hasContext() {
        FilterContext context = CONTEXT_HOLDER.get();
        return context != null && context.hasRules();
    }
}
