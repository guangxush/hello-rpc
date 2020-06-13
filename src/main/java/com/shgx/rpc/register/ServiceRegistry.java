package com.shgx.rpc.register;

/**
 * @author: guangxush
 * @create: 2020/06/11
 */
public interface ServiceRegistry {

    void register(ServiceModel serviceModel) throws Exception;

    void unRegister(ServiceModel serviceModel) throws Exception;

    ServiceModel discovery(String serviceName) throws Exception;

    void close() throws Exception;
}
