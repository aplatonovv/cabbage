package ru.amontag.cabbage.core.annotation;

import scala.collection.immutable.Map;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Objects;

/**
 * Created by montag on 23.03.15.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Handler {
    public String name();
    public AccessType accessType();
}
