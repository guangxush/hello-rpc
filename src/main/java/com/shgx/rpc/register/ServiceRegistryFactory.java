package com.shgx.rpc.register;

import com.shgx.rpc.commons.ServiceRegistryType;

/**
 * @author: guangxush
 * @create: 2020/06/11
 */
public class ServiceRegistryFactory {
    private static volatile ServiceRegistry serviceRegister;

    public static ServiceRegistry getInstance(ServiceRegistryType type, String registerAddress) throws Exception{
        if(null == serviceRegister){
            synchronized (ServiceRegistryFactory.class){
                if(null == serviceRegister){
                    serviceRegister = type == ServiceRegistryType.zookeeper ? new ZookeeperServiceRegistry(registerAddress) :
                            type == ServiceRegistryType.eureka ? new EurekaServiceRegistry() : null;
                }
            }
        }
        return serviceRegister;
    }
}
