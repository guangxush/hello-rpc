package com.shgx.rpc.provider;

import com.shgx.rpc.commons.ProviderUtils;
import com.shgx.rpc.ptotocol.RpcRequest;
import com.shgx.rpc.ptotocol.RpcResponse;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.reflect.FastClass;

import java.util.Map;

/**
 * @author: guangxush
 * @create: 2020/06/11
 */
@Slf4j
public class ProviderHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private final Map<String, Object> handlerMap;

    public ProviderHandler(Map<String, Object> handlerMap) {
        this.handlerMap = handlerMap;
    }

    /**
     * rpc请求处理器
     * @param channelHandlerContext
     * @param rpcRequest
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcRequest rpcRequest) throws Exception {
        RpcProvider.submit(() -> {
            log.debug("Receive request {}", rpcRequest.getRequestId());
            RpcResponse rpcResponse = new RpcResponse();
            rpcResponse.setRequestId(rpcRequest.getRequestId());
            try {
                Object result = handle(rpcRequest);
                rpcResponse.setResult(result);
            } catch (Throwable throwable) {
                rpcResponse.setException(throwable.toString());
                log.error("Rpc server handle request error", throwable);
            }
            channelHandlerContext.writeAndFlush(rpcResponse)
                    .addListener((ChannelFutureListener) channelFuture -> log.debug("send response for request:" + rpcRequest.getRequestId()));
        });
    }

    /**
     * 消息处理器核心实现
     * @param rpcRequest
     * @return
     * @throws Throwable
     */
    private Object handle(RpcRequest rpcRequest) throws Throwable {
        // 生成服务注册key
        String providerKey = ProviderUtils.generateKey(rpcRequest.getClassName(), rpcRequest.getServiceVersion());
        // 从缓存中获取相关的bean，缓存map的注册在RpcProvider中实现
        Object providerBean = handlerMap.get(providerKey);
        if (null == providerBean) {
            // 没有获取到当前bean服务
            throw new RuntimeException(String.format("Provider not exist: %s:%s", rpcRequest.getClassName(), rpcRequest.getMethodName()));
        }

        // 使用反射完成消息处理
        Class<?> providerClass = providerBean.getClass();
        String methodName = rpcRequest.getMethodName();
        Class<?>[] parameterTypes = rpcRequest.getParameterTypes();
        Object[] parameters = rpcRequest.getParameters();
        // 打印类名
        log.debug(providerClass.getName());
        // 打印方法名
        log.debug(methodName);
        // 打印参数类型
        for (Class<?> parameterType : parameterTypes) {
            log.debug(parameterType.getName());
        }
        // 打印参数
        for (Object parameter : parameters) {
            log.debug(parameter.toString());
        }
        // 使用Cglib创建服务生产者的代理对象，调用指定的方法
        FastClass providerFastClass = FastClass.create(providerClass);
        int methodIndex = providerFastClass.getIndex(methodName, parameterTypes);
        return providerFastClass.invoke(methodIndex, providerBean, parameters);
    }
}
