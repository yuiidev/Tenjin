package net.demozo.tenjin.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
public @interface Column {
    String type() default "";

    /**
     * Name of the column.
     * @return
     */
    String value() default "";
    int length() default Integer.MAX_VALUE;
    boolean nullable() default false;
    boolean autoIncrements() default false;
}
