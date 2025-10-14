package com.ctm.annotations;

import java.lang.annotation.*;
@Retention(RetentionPolicy.RUNTIME) 
@Target(ElementType.FIELD) 
public @interface Check { 
	String value(); // SQL check snippet (DB-agnostic enough) }
}