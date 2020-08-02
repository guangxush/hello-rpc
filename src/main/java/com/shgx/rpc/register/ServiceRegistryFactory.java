package com.shgx.rpc.register;

import com.shgx.rpc.commons.ServiceRegistryType;

/**
 * @author: guangxush
 * @create: 2020/06/11
 */
public class ServiceRegistryFactory {
    private static volatile ServiceRegistry serviceRegistry;

    public static ServiceRegistry getInstance(ServiceRegistryType serviceRegistryType, String registryAddress) throws Exception {
        if (null == serviceRegistry) {
            synchronized (ServiceRegistryFactory.class) {
                if (null == serviceRegistry) {
                    serviceRegistry = serviceRegistryType == ServiceRegistryType.zookeeper ? new ZookeeperServiceRegistry(registryAddress) :
                            serviceRegistryType == ServiceRegistryType.eureka ? new EurekaServiceRegistry(registryAddress) : null;
                }
            }
        }
        return serviceRegistry;
    }
}
