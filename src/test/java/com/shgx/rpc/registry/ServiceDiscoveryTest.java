package com.shgx.rpc.registry;

import com.shgx.rpc.commons.ServiceRegistryType;
import com.shgx.rpc.register.ServiceModel;
import com.shgx.rpc.register.ServiceRegistry;
import com.shgx.rpc.register.ServiceRegistryFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author: guangxush
 * @create: 2020/06/13
 */
@Slf4j
public class ServiceDiscoveryTest {
    ServiceRegistry serviceRegistry;

    private static final String ADDRESS = "127.0.0.1:2181";

    @Before
    public void init() throws Exception {
        serviceRegistry = ServiceRegistryFactory.getInstance(ServiceRegistryType.zookeeper, ADDRESS);
    }

    @After
    public void close() throws Exception {
        serviceRegistry.close();
    }

    @Test
    public void testAll() throws Exception {
        ServiceModel test11 = ServiceModel
                .builder()
                .serviceName("test1")
                .servicePort(8088)
                .serviceVersion("1.0.0")
                .address("127.0.0.1");
        ServiceModel test22 = ServiceModel
                .builder()
                .serviceName("test2")
                .servicePort(8088)
                .serviceVersion("1.0.0")
                .address("127.0.0.2");
        ServiceModel test33 = ServiceModel
                .builder()
                .serviceName("test3")
                .servicePort(8088)
                .serviceVersion("1.0.0")
                .address("127.0.0.3");

        serviceRegistry.register(test11);
        serviceRegistry.register(test22);
        serviceRegistry.register(test33);


        ServiceModel test1 = serviceRegistry.discovery("test1:1.0.0");
        ServiceModel test2 = serviceRegistry.discovery("test2:1.0.0");
        ServiceModel test3 = serviceRegistry.discovery("test3");

        assert test1 != null;
        assert test2 != null;
        assert test3 == null;

        serviceRegistry.unRegister(test11);
        serviceRegistry.unRegister(test22);
        serviceRegistry.unRegister(test33);
    }

    @Test
    public void testReRegister() throws Exception {
        serviceRegistry.register(ServiceModel
                .builder()
                .serviceName("test")
                .servicePort(8088)
                .serviceVersion("1.0.0")
                .address("127.0.0.1"));
        serviceRegistry.register(ServiceModel
                .builder()
                .serviceName("test")
                .servicePort(8088)
                .serviceVersion("1.0.0")
                .address("127.0.0.1"));
    }

    @Test
    public void testLoadBalance() throws Exception {
        serviceRegistry.register(ServiceModel
                .builder()
                .serviceName("test")
                .servicePort(8088)
                .serviceVersion("1.0.0")
                .address("127.0.0.1"));

        serviceRegistry.register(ServiceModel
                .builder()
                .serviceName("test")
                .servicePort(8088)
                .serviceVersion("1.0.0")
                .address("127.0.0.2"));

        serviceRegistry.register(ServiceModel
                .builder()
                .serviceName("test")
                .servicePort(8088)
                .serviceVersion("1.0.0")
                .address("127.0.0.3"));


        ServiceModel test1 = serviceRegistry.discovery("test:1.0.0");
        ServiceModel test2 = serviceRegistry.discovery("test:1.0.0");
        ServiceModel test3 = serviceRegistry.discovery("test:1.0.0");

        assert test1 != null;
        assert test2 != null;
        assert test3 != null;

        assert !test1.getAddress().equals(test2.getAddress());
        assert !test1.getAddress().equals(test3.getAddress());
        assert !test2.getAddress().equals(test3.getAddress());

        log.info("test1: {}", test1.toString());
        log.info("test2: {}", test2.toString());
        log.info("test3: {}", test3.toString());
    }

}
