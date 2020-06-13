package com.shgx.rpc.consumer;

import com.shgx.rpc.commons.ServiceRegistryEnum;
import com.shgx.rpc.register.ServiceRegistryFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * @author: guangxush
 * @create: 2020/06/13
 */
@Slf4j
public class TestConsumer {
    public static void main(String[] args) throws Exception {
        String address = "127.0.0.1:2181";
        HelloService helloService = Consumer.create(HelloService.class, "0.0.1",
                ServiceRegistryFactory.getInstance(ServiceRegistryEnum.zookeeper, address));
        String response = helloService.hello("shgx");
        log.info("response: "+ response);
    }
}
