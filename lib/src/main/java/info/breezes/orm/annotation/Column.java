package info.breezes.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by jianxingqiao on 5/4/2014.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
    String name() default "";

    String type() default "";

    String defaultValue() default "";

    boolean notNull() default false;

    boolean autoincrement() default false;

    boolean primaryKey() default false;

    boolean uniqueIndex() default false;

    int length() default 1024;

    int order() default 0;

    String comment() default "";

}
