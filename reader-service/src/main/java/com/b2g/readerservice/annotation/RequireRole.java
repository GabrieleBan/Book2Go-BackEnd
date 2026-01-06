package com.b2g.readerservice.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to require JWT validation with specific roles.
 * Can be applied to controller methods to ensure the user has the required role(s).
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {

    /**
     * The required roles. At least one of these roles must be present in the JWT token.
     *
     * @return array of required role names
     */
    String[] value();

    /**
     * Whether all specified roles are required (true) or just one of them (false).
     * Default is false (OR logic - user needs at least one of the specified roles).
     *
     * @return true if all roles are required, false if only one is needed
     */
    boolean requireAll() default false;
}
