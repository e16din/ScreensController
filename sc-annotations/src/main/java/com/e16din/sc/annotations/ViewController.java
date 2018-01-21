package com.e16din.sc.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
@Repeatable(value = ViewControllers.class)
public @interface ViewController {
    Class screen();

    Class viewState() default Object.class;

    boolean startOnce() default false;

    Class after() default Object.class; // require any view controller class
}
