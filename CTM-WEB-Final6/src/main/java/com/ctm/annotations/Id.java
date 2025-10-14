package com.ctm.annotations;

import java.lang.annotation.*;
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Id {
    boolean auto() default true; // true = DB auto-increment; false = app-supplied
}