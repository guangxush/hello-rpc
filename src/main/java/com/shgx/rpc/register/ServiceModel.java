package com.shgx.rpc.register;

import lombok.Data;

/**
 * 服务元数据信息
 * @author: guangxush
 * @create: 2020/06/11
 */

@Data
public class ServiceModel {
    /**
     * 服务名
     */
    private String serviceName;
    /**
     * 服务版本
     */
    private String serviceVersion;
    /**
     * 服务地址
     */
    private String address;
    /**
     * 服务端口
     */
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
