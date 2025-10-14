package com.ctm.annotations;
import java.lang.annotation.*;
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface FK {
 String references();                 // "table(column)"
 String onDelete() default "";        // e.g. "CASCADE", "RESTRICT", "SET NULL"
 String onUpdate() default "";
}

