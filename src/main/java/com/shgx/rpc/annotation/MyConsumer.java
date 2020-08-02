package com.shgx.rpc.annotation;

import org.springframework.beans.factory.annotation.Autowired;
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
@Target({ElementType.FIELD}) //注解目标为属性
@Autowired //被Spring加载
public @interface MyConsumer {
    /**
     * com.shgx.rpc.consumer.ConsumerBean#setVersion(java.lang.String)
     * @return
     */
    String serviceVersion() default "0.0.1";

    /**
     * com.shgx.rpc.consumer.ConsumerBean#setRegistryAddress(java.lang.String)
     * @return
     */
    String registryAddress() default "127.0.0.1:2181";

    /**
     * com.shgx.rpc.consumer.ConsumerBean#setRegistryType(java.lang.String)
     * @return
     */
    String registryType() default "zookeeper";
}
