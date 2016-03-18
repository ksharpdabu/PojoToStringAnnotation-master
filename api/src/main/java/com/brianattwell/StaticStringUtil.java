package com.brianattwell;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

/**
 * Create a function in {@link StringUtil} for creating strings.
 */
@Target(value = ElementType.TYPE)
public @interface StaticStringUtil {}