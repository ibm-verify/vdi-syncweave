package com.ibm.di.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CVTTest {
	public static final String UNDOCUMENTED = "undocumented";
	String name() default UNDOCUMENTED;
}
