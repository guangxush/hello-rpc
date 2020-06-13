package com.shgx.rpc.provider;

import com.shgx.rpc.commons.ProviderUtils;
import com.shgx.rpc.ptotocol.Request;
import com.shgx.rpc.ptotocol.Response;
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
public class ProviderHandler extends SimpleChannelInboundHandler<Request> {

    private final Map<String, Object> handlerMap;

    public ProviderHandler(Map<String, Object> handlerMap) {
        this.handlerMap = handlerMap;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Request request) throws Exception {
        Provider.submit(() -> {
            log.debug("Receive request:", request.getRequestId());
            Response response = new Response();
            response.setRequestId(request.getRequestId());
            try {
                Object result = handle(request);
                response.setResult(result);
            } catch (Throwable throwable) {
                response.setException(throwable.toString());
                log.error("Rpc server handle request error", throwable);
            }
            channelHandlerContext.writeAndFlush(response)
                    .addListener((ChannelFutureListener) channelFuture -> log.debug("send response for request:" + request.getRequestId()));
        });
    }

    private Object handle(Request request) throws Throwable {
        String providerKey = ProviderUtils.generateKey(request.getClassName(), request.getVersion());
        Object providerBean = handlerMap.get(providerKey);

        if (null == providerBean) {
            throw new RuntimeException(String.format("Provider not exist: %s:%s", request.getClassName(), request.getMethodName()));
        }

        Class<?> providerClass = providerBean.getClass();
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();

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
