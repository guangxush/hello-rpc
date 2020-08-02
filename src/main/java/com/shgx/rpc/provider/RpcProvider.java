package com.shgx.rpc.provider;

import com.shgx.rpc.annotation.MyProvider;
import com.shgx.rpc.commons.ProviderUtils;
import com.shgx.rpc.ptotocol.RpcDecoder;
import com.shgx.rpc.ptotocol.RpcEncoder;
import com.shgx.rpc.register.ServiceModel;
import com.shgx.rpc.register.ServiceRegistry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.shgx.rpc.constants.Constants.PROVIDER_THREAD_POOL_NUM;
import static com.shgx.rpc.constants.Constants.PROVIDER_THREAD_POOL_QUEUE_LEN;

/**
 * @author: guangxush
 * @create: 2020/06/11
 */
@Slf4j
public class RpcProvider implements InitializingBean, BeanPostProcessor {

    private static ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("rpc-pool-%d").build();
    private static ThreadPoolExecutor threadPoolExecutor;
    private String serverAddress;
    private ServiceRegistry serviceRegistry;
    private Map<String, Object> handlerMap = new HashMap<>(256);
    private EventLoopGroup bossGroup = null;
    private EventLoopGroup workerGroup = null;

    public RpcProvider(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public RpcProvider(String serverAddress, ServiceRegistry serviceRegistry) {
        this.serverAddress = serverAddress;
        this.serviceRegistry = serviceRegistry;
    }

    public static void submit(Runnable task) {
        if (threadPoolExecutor == null) {
            synchronized (RpcProvider.class) {
                if (threadPoolExecutor == null) {
                    threadPoolExecutor = new ThreadPoolExecutor(PROVIDER_THREAD_POOL_NUM,
                            PROVIDER_THREAD_POOL_NUM,
                            600L,
                            TimeUnit.SECONDS,
                            new ArrayBlockingQueue<>(PROVIDER_THREAD_POOL_QUEUE_LEN),
                            threadFactory);
                }
            }
        }
        threadPoolExecutor.submit(task);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        new Thread(() -> {
            try {
                start();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        ).start();
    }
//
//    @Override
//    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
//        Map<String, Object> providerMap = applicationContext.getBeansWithAnnotation(MyProvider.class);
//        if (MapUtils.isNotEmpty(providerMap)) {
//            for (Object providerBean : providerMap.values()) {
//                MyProvider myProvider = providerBean.getClass().getAnnotation(MyProvider.class);
//                String serviceName = myProvider.serviceInterface().getName();
//                String version = myProvider.version();
//                String providerKey = ProviderUtils.generateKey(serviceName, version);
//                handlerMap.put(providerKey, providerBean);
//
//                String[] address = serverAddress.split(":");
//                String host = address[0];
//                int port = Integer.parseInt(address[1]);
//                ServiceModel serviceModel = ServiceModel.builder()
//                        .address(host)
//                        .serviceName(serviceName)
//                        .servicePort(port)
//                        .serviceVersion(version);
//                try {
//                    serviceRegistry.register(serviceModel);
//                    log.debug("register service...", serviceModel.toString());
//                } catch (Exception e) {
//                    log.error("register fail", serviceModel.toString(), e);
//                }
//            }
//        }
//    }

    /**
     * netty监听服务, 进行服务注册
     * @throws InterruptedException
     */
    public void start() throws InterruptedException {
        if (bossGroup == null || workerGroup == null) {
            bossGroup = new NioEventLoopGroup();
            workerGroup = new NioEventLoopGroup();
            ServerBootstrap bootstrap = new ServerBootstrap();
            //通用平台使用NioServerSocketChannel；Linux使用EpollServerSocketChannel
            bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline()
                            .addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0))
                            .addLast(new RpcDecoder())
                            .addLast(new RpcEncoder())
                            .addLast(new ProviderHandler(handlerMap));
                }
            }).option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            String[] address = serverAddress.split(":");
            String host = address[0];
            int port = Integer.parseInt(address[1]);

            ChannelFuture future = bootstrap.bind(host, port).sync();
            log.info("Server started on port {}", port);
            future.channel().closeFuture().sync();
        }
    }

    /**
     * 手动注册服务，便于测试
     * @param providerBean
     * @param serverAddress
     */
    public void addService(Object providerBean, String serverAddress){
        MyProvider myProvider = providerBean.getClass().getAnnotation(MyProvider.class);
        String serviceName = myProvider.serviceInterface().getName();
        String version = myProvider.serviceVersion();
        String providerKey = ProviderUtils.generateKey(serviceName, version);
        String[] address = serverAddress.split(":");
        String host = address[0];
        int port = Integer.parseInt(address[1]);
        ServiceModel serviceModel = ServiceModel.builder()
                .address(host)
                .serviceName(serviceName)
                .servicePort(port)
                .serviceVersion(version);
        try {
            serviceRegistry.register(serviceModel);
            log.debug("register service...", serviceModel.toString());
        } catch (Exception e) {
            log.error("register fail...", serviceModel.toString(), e);
        }

        if(!handlerMap.containsKey(providerKey)){
            log.info("Loading service..."+ providerKey);
            handlerMap.put(providerKey, providerBean);
        }
    }

    //BeanPostProcessor
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        MyProvider myProvider = bean.getClass().getAnnotation(MyProvider.class);
        if(myProvider == null){
            return bean;
        }
        String serviceName = myProvider.serviceInterface().getName();
        String version = myProvider.serviceVersion();
        String providerKey = ProviderUtils.generateKey(serviceName, version);
        //缓存provider bean到本地缓存中
        handlerMap.put(providerKey, bean);

        String[] address = serverAddress.split(":");
        String host = address[0];
        int port = Integer.parseInt(address[1]);
        ServiceModel serviceModel = ServiceModel.builder()
                .address(host)
                .serviceName(serviceName)
                .servicePort(port)
                .serviceVersion(version);
        try {
            serviceRegistry.register(serviceModel);
            log.debug("register service... {}", serviceModel.toString());
        } catch (Exception e) {
            log.error("register fail {}", serviceModel.toString(), e);
        }
        return bean;
    }
}
