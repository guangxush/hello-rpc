package com.shgx.rpc.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author: guangxush
 * @create: 2020/06/10
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Component
public @interface MyConsumer {
    String version() default "0.0.1";

    String registerAddress() default "127.0.0.1:2181";

    String registerType() default "zookeeper";
}
