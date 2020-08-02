package com.shgx.rpc.register;

import lombok.Data;

/**
 * @author: guangxush
 * @create: 2020/06/11
 */

@Data
public class ServiceModel {
    private String serviceName;
    private String serviceVersion;
    private String address;
    private int port;

    public static ServiceModel builder() {
        return new ServiceModel();
    }

    public ServiceModel serviceName(String serviceName) {
        this.setServiceName(serviceName);
        return this;
    }


    public ServiceModel servicePort(int servicePort) {
        this.port = servicePort;
        return this;
    }


    public ServiceModel address(String address) {
        this.address = address;
        return this;
    }

    public ServiceModel serviceVersion(String serviceVersion) {
        this.serviceVersion = serviceVersion;
        return this;
    }

    @Override
    public String toString() {
        return "ServiceModel{" +
                "serviceName='" + serviceName + '\'' +
                ", serviceVersion='" + serviceVersion + '\'' +
                ", address='" + address + '\'' +
                ", port=" + port +
                '}';
    }
}
