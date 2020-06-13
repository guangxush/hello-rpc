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
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author: guangxush
 * @create: 2020/06/11
 */
@Slf4j
public class Provider implements ApplicationContextAware, InitializingBean {

    private String serverAddress;

    private static ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("rpc-pool-%d").build();

    private static ThreadPoolExecutor threadPoolexecutor;

    private ServiceRegistry serviceRegistry;

    private Map<String, Object> handlerMap = new HashMap<>(256);
    private EventLoopGroup baseGroup = null;
    private EventLoopGroup workerGroup = null;

    public Provider(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public Provider(String serverAddress, ServiceRegistry serviceRegistry) {
        this.serverAddress = serverAddress;
        this.serviceRegistry = serviceRegistry;
    }

    public static void submit(Runnable task) {
        if (threadPoolexecutor == null) {
            synchronized (Provider.class) {
                if (threadPoolexecutor == null) {
                    threadPoolexecutor = new ThreadPoolExecutor(256,
                            256,
                            600L,
                            TimeUnit.SECONDS,
                            new ArrayBlockingQueue<>(1024),
                            threadFactory);
                }
            }
        }
        threadPoolexecutor.submit(task);
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

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, Object> providerMap = applicationContext.getBeansWithAnnotation(MyProvider.class);
        if (MapUtils.isNotEmpty(providerMap)) {
            for (Object providerBean : providerMap.values()) {
                MyProvider myProvider = providerBean.getClass().getAnnotation(MyProvider.class);
                String serviceName = myProvider.serviceInterface().getName();
                String version = myProvider.version();
                String providerKey = ProviderUtils.generateKey(serviceName, version);
                handlerMap.put(providerKey, providerBean);

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
                    log.error("register fail", serviceModel.toString(), e);
                }
            }
        }
    }

    public void start() throws InterruptedException {
        if (baseGroup == null || workerGroup == null) {
            baseGroup = new NioEventLoopGroup();
            workerGroup = new NioEventLoopGroup();
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(baseGroup, workerGroup).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
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
            log.info("Server started on port{}", port);
            future.channel().closeFuture().sync();
        }
    }

    public void addService(Object providerBean, String serverAddress){
        MyProvider myProvider = providerBean.getClass().getAnnotation(MyProvider.class);
        String serviceName = myProvider.serviceInterface().getName();
        String version = myProvider.version();
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

//    BeanPostProcessor
//    @Override
//    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
//        return bean;
//    }
//
//    @Override
//    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
//        MyProvider myProvider = bean.getClass().getAnnotation(MyProvider.class);
//        if(myProvider == null){
//            return bean;
//        }
//        String serviceName = myProvider.serviceInterface().getName();
//        String version = myProvider.version();
//        String providerKey = ProviderUtils.generateKey(serviceName, version);
//        handlerMap.put(providerKey, bean);
//
//        String[] address = serverAddress.split(":");
//        String host = address[0];
//        int port = Integer.parseInt(address[1]);
//        ServiceModel serviceModel = ServiceModel.builder()
//                .address(host)
//                .serviceName(serviceName)
//                .servicePort(port)
//                .serviceVersion(version);
//        try {
//            serviceRegistry.register(serviceModel);
//            log.debug("register service...", serviceModel.toString());
//        } catch (Exception e) {
//            log.error("register fail", serviceModel.toString(), e);
//        }
//        return bean;
//    }
}
