package net.demozo.tenjin.annotation;

import net.demozo.tenjin.RelationshipType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Relationship {
    /**
     * The name of the column.
     * @return
     */
    String value() default "id";
    RelationshipType type();
}
