package com.via.rpc.conf.annotation;

import java.lang.annotation.*;

/**
 * Created by liuj-ai on 2019/4/17.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
public @interface Service {
    Class<?> interfaceClass() default void.class;
}
