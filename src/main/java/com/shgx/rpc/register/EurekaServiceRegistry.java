package com.shgx.rpc.register;

/**
 * @author: guangxush
 * @create: 2020/06/11
 */
public class EurekaServiceRegistry implements ServiceRegistry {
    @Override
    public void register(ServiceModel serviceModel) throws Exception {

    }

    @Override
    public void unRegister(ServiceModel serviceModel) throws Exception {

    }

    @Override
    public ServiceModel discovery(String serviceName) throws Exception {
        return null;
    }

    @Override
    public void close() throws Exception {

    }
}
