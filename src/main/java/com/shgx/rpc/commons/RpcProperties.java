package com.shgx.rpc.commons;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author: guangxush
 * @create: 2020/06/13
 */
@Data
@ConfigurationProperties(prefix = "rpc")
public class RpcProperties {
    private String serviceAddress;
    private String serviceRegistryAddress;
    private String serviceRegistryType;
}
