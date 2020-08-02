package com.shgx.rpc.register;

/**
 * @author: guangxush
 * @create: 2020/06/11
 */
public interface ServiceRegistry {

    /**
     * 服务注册
     * @param serviceModel
     * @throws Exception
     */
    void register(ServiceModel serviceModel) throws Exception;

    /**
     * 服务注销
     * @param serviceModel
     * @throws Exception
     */
    void unRegister(ServiceModel serviceModel) throws Exception;

    /**
     * 服务发现
     * @param serviceName
     * @return
     * @throws Exception
     */
    ServiceModel discovery(String serviceName) throws Exception;

    /**
     * 服务关闭
     * @throws Exception
     */
    void close() throws Exception;
}
