package com.shgx.rpc.commons;

/**
 * @author: guangxush
 * @create: 2020/06/11
 */
public class ProviderUtils {
    public static String generateKey(String serviceName, String serviceVersion){
        return String.join(":", serviceName, serviceVersion);
    }
}
