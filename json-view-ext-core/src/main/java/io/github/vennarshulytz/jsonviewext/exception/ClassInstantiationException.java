package io.github.vennarshulytz.jsonviewext.exception;

/**
 * 类实例化异常
 *
 * @author vennarshulytz
 * @since 1.0.0
 */
public class ClassInstantiationException extends RuntimeException {

    public ClassInstantiationException(Class<?> targetClass, Throwable cause) {
        super("Failed to instantiate class: " + targetClass.getName(), cause);
    }

    public ClassInstantiationException(String message, Throwable cause) {
        super(message, cause);
    }
}
