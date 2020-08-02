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
@Retention(RetentionPolicy.RUNTIME) //运行时解析
@Target({ElementType.TYPE}) //class注解
@Component //被Spring加载
public @interface MyProvider {
    Class<?> serviceInterface() default Object.class; // 接口

    String serviceVersion() default "0.0.1"; // 版本号
}
