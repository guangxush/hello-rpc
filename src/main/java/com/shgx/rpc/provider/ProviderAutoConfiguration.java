package com.shgx.rpc.provider;

import com.shgx.rpc.commons.RpcProperties;
import com.shgx.rpc.commons.ServiceRegistryType;
import com.shgx.rpc.ptotocol.Response;
import com.shgx.rpc.register.ServiceRegistry;
import com.shgx.rpc.register.ServiceRegistryFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * @author: guangxush
 * @create: 2020/06/13
 */
@Configuration
@EnableConfigurationProperties(Response.class)
public class ProviderAutoConfiguration {

    @Resource
    private RpcProperties properties;

    @Bean
    public Provider init() throws Exception {
        ServiceRegistryType type = ServiceRegistryType.valueOf(properties.getServiceRegisterType());
        ServiceRegistry serviceRegistry = ServiceRegistryFactory.getInstance(type, properties.getServiceRegisterAddress());
        return new Provider(properties.getServiceAddress(), serviceRegistry);
    }
}
