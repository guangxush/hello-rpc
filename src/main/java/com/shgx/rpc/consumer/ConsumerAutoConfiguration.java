package com.shgx.rpc.consumer;

import com.shgx.rpc.commons.RpcProperties;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author: guangxush
 * @create: 2020/06/13
 */
@Configuration
@EnableConfigurationProperties(RpcProperties.class)
public class ConsumerAutoConfiguration {

    @Bean
    public static BeanFactoryPostProcessor consumerPostProcess() {
        return new ConsumerPostProcessor();
    }
}
