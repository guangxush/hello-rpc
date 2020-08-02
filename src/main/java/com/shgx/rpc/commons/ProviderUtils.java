package com.shgx.rpc.commons;

/**
 * @author: guangxush
 * @create: 2020/06/11
 */
public class ProviderUtils {
    /**
     *  构造服务标示 key=服务名+版本号
     * @param serviceName 服务名
     * @param serviceVersion 版本号
     * @return 服务唯一标示
     */
    public static String generateKey(String serviceName, String serviceVersion){
        return String.join(":", serviceName, serviceVersion);
    }
}
