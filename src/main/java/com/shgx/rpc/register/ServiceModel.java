package com.shgx.rpc.register;

import lombok.Builder;

/**
 * @author: guangxush
 * @create: 2020/06/11
 */
@Builder
public class ServiceModel {
    private String serviceName;
    private String serviceVersion;
    private String address;
    private int port;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceVersion() {
        return serviceVersion;
    }

    public void setServiceVersion(String serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
