package ru.amontag.cabbage.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by montag on 23.03.15.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Query {
    public String value();
    public String preparationScript() default "";
    public String postprocessingScript() default "";
    public String target();
    public String method();
    public String description() default "";
}
