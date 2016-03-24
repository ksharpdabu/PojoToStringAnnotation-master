package com.brianattwell;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

/**
 * Create a function in {@link StringUtil} for creating strings.
 */
@Target(value = ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)  //默认是RetentionPolicy.CLASS，可以修改
public @interface StaticStringUtil {}