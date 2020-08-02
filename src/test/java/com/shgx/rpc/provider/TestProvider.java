package com.shgx.rpc.provider;

import com.shgx.rpc.commons.ServiceRegistryType;
import com.shgx.rpc.consumer.HelloService;
import com.shgx.rpc.register.ServiceRegistryFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * @author: guangxush
 * @create: 2020/06/13
 */
@Slf4j
public class TestProvider {
    public static void main(String[] args) throws Exception {
        String serverAddress = "127.0.0.1:6688";
        String registryAddress = "127.0.0.1:2181";

        RpcProvider rpcProvider = new RpcProvider(serverAddress, ServiceRegistryFactory.getInstance(ServiceRegistryType.zookeeper, registryAddress));
        HelloService helloService = new HelloServiceImpl();
        rpcProvider.addService(helloService, serverAddress);
        try {
            rpcProvider.start();
        } catch (Exception e) {
            log.error("exception:", e);
        }
    }
}
