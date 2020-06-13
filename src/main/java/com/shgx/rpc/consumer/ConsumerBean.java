package com.shgx.rpc.consumer;

import com.shgx.rpc.commons.ServiceRegistryType;
import com.shgx.rpc.register.ServiceRegistryFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.FactoryBean;

/**
 * @author: guangxush
 * @create: 2020/06/13
 */
@Slf4j
public class ConsumerBean implements FactoryBean {

    private Class<?> interfaceClass;
    private String serviceVersion;
    private String registryType;
    private String registryAddress;
    private Object object;

    public void setInterfaceClass(Class<?> interfaceClass) {
        this.interfaceClass = interfaceClass;
    }

    public void setServiceVersion(String serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

    public void setRegistryType(String registryType) {
        this.registryType = registryType;
    }

    public void setRegistryAddress(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    @Override
    public Object getObject() throws Exception {
        return this.object;
    }

    @Override
    public Class<?> getObjectType() {
        return interfaceClass;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public void init() throws Exception {
        this.object = Consumer.create(interfaceClass, serviceVersion, ServiceRegistryFactory.getInstance(
                ServiceRegistryType.valueOf(registryType), registryAddress
        ));
        log.info("ConsumerBean {} init....", interfaceClass.getName());
    }
}
