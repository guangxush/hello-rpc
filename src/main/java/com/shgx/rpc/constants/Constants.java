package com.shgx.rpc.constants;

/**
 * @author: guangxush
 * @create: 2020/06/11
 */
public class Constants {

    public static final String DEFAULT_HOST = "127.0.0.1";

    public static final int DEFAULT_PORT = 8080;

    /**
     * 注册中心节点根路径
     */
    public static final String BASE_URL = "/rpc";

    /**
     * 生产者线程池线程数目
     */
    public static final int PROVIDER_THREAD_POOL_NUM = 256;

    /**
     * 生产者线程池工作队列长度
     */
    public static final int PROVIDER_THREAD_POOL_QUEUE_LEN = 1024;

    public static String INIT_METHOD = "init";

}
