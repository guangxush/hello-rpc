package com.shgx.rpc.consumer;

import com.shgx.rpc.commons.ProviderUtils;
import com.shgx.rpc.ptotocol.Request;
import com.shgx.rpc.ptotocol.Response;
import com.shgx.rpc.ptotocol.RpcDecoder;
import com.shgx.rpc.ptotocol.RpcEncoder;
import com.shgx.rpc.register.ServiceModel;
import com.shgx.rpc.register.ServiceRegistry;
import io.netty.bootstrap.*;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Proxy;

/**
 * @author: guangxush
 * @create: 2020/06/11
 */
@Slf4j
public class Consumer extends SimpleChannelInboundHandler<Response> {

    private final Object obj = new Object();
    private ServiceRegistry serviceRegistry;
    private EventLoopGroup eventLoopGroup = new NioEventLoopGroup(4);
    private Channel channel;
    private Response response;

    public Consumer(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    @SuppressWarnings("unchecked")
    public static <T> T create(Class<T> interfaceClass, String serviceVersion, ServiceRegistry serviceRegistry) {
        return (T)Proxy.newProxyInstance(interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                new RpcInvokeHandler<>(serviceVersion, serviceRegistry));
    }

    public Response sendRequest(Request request)throws Exception{
        try{
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer() {

                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            log.debug("init the consumer request:");
                            channel.pipeline()
                                    .addLast(new RpcEncoder())
                                    .addLast(new RpcDecoder())
                                    .addLast(Consumer.this);
                        }
                    });
            String targetService = ProviderUtils.generateKey(request.getClassName(), request.getVersion());
            ServiceModel serviceModel = serviceRegistry.discovery(targetService);
            if(serviceModel == null){
                // 没有服务的提供方
                throw new RuntimeException("no service for"+ targetService);
            }
            log.debug("discovery for {}-{}", targetService, serviceModel.toString());
            final ChannelFuture future = bootstrap.connect(serviceModel.getAddress(), serviceModel.getServicePort()).sync();

            future.addListener((ChannelFutureListener) arg0 -> {
                if(future.isSuccess()){
                    log.debug("connect rpc success");
                }else{
                    log.error("connect rpc failed");
                    future.cause().printStackTrace();
                    eventLoopGroup.shutdownGracefully();
                }
            });
            this.channel = future.channel();
            this.channel.writeAndFlush(request).sync();

            synchronized (this.obj){
                this.obj.wait();
            }

            return this.response;
        }finally {
            close();
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Response response) throws Exception {
        this.response = response;

        synchronized (obj){
            // 收到响应， 唤醒线程
            obj.notifyAll();
        }
    }

    private void close(){
        if(this.channel!=null){
            this.channel.close();
        }
        if(this.eventLoopGroup!=null){
            this.eventLoopGroup.shutdownGracefully();
        }
        log.debug("shutdown consumer....");
    }
}
