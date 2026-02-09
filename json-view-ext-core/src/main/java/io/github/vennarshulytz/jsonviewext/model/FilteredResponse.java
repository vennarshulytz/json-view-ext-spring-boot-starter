package io.github.vennarshulytz.jsonviewext.model;

/**
 * 过滤响应包装类，标记需要进行字段过滤的响应对象
 *
 * @author vennarshulytz
 * @since 1.1.0
 */
public class FilteredResponse {

    private final Object data;
    private final FilterContext context;

    public FilteredResponse(Object data, FilterContext context) {
        this.data = data;
        this.context = context;
    }

    public Object getData() {
        return data;
    }

    public FilterContext getContext() {
        return context;
    }
}
