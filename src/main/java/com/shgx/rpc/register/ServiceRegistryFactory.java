package com.shgx.rpc.register;

import com.shgx.rpc.commons.ServiceRegistryEnum;

/**
 * @author: guangxush
 * @create: 2020/06/11
 */
public class ServiceRegistryFactory {
    private static volatile ServiceRegistry serviceRegister;

    public static ServiceRegistry getInstance(ServiceRegistryEnum type, String registerAddress) throws Exception{
        if(null == serviceRegister){
            synchronized (ServiceRegistryFactory.class){
                if(null == serviceRegister){
                    serviceRegister = type == ServiceRegistryEnum.zookeeper ? new ZookeeperServiceRegistry(registerAddress) :
                            type == ServiceRegistryEnum.zookeeper ? new EurekaServiceRegistry() : null;
                }
            }
        }
        return serviceRegister;
    }
}
