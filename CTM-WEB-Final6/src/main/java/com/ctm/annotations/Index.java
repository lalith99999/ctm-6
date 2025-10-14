package com.ctm.annotations;


import java.lang.annotation.*;
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Index {
 String name() default "";
}

