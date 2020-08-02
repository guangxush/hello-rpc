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

    private Object handle(RpcRequest rpcRequest) throws Throwable {
        String providerKey = ProviderUtils.generateKey(rpcRequest.getClassName(), rpcRequest.getServiceVersion());
        Object providerBean = handlerMap.get(providerKey);

        if (null == providerBean) {
            throw new RuntimeException(String.format("Provider not exist: %s:%s", rpcRequest.getClassName(), rpcRequest.getMethodName()));
        }

        Class<?> providerClass = providerBean.getClass();
        String methodName = rpcRequest.getMethodName();
        Class<?>[] parameterTypes = rpcRequest.getParameterTypes();
        Object[] parameters = rpcRequest.getParameters();

        log.debug(providerClass.getName());
        log.debug(methodName);

        for (int i = 0; i < parameterTypes.length; i++) {
            log.debug(parameterTypes[i].getName());
        }

        for (int i = 0; i < parameters.length; i++) {
            log.debug(parameters[i].toString());
        }

        FastClass providerFastClass = FastClass.create(providerClass);
        int methodIndex = providerFastClass.getIndex(methodName, parameterTypes);
        return providerFastClass.invoke(methodIndex, providerBean, parameters);
    }
}
