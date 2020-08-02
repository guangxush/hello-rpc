package com.shgx.rpc.consumer;

import com.shgx.rpc.ptotocol.Request;
import com.shgx.rpc.ptotocol.Response;
import com.shgx.rpc.register.ServiceRegistry;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * @author: guangxush
 * @create: 2020/06/13
 */
@Slf4j
public class RpcInvokeHandler<T> implements InvocationHandler {

    private static final String EQUALS = "equals";
    private static final String HASH_CODE = "hashCode";
    private static final String TO_STRING = "toString";

    private String serviceVersion;
    private ServiceRegistry serviceRegistry;

    public RpcInvokeHandler() {
    }

    public RpcInvokeHandler(String serviceVersion, ServiceRegistry serviceRegistry) {
        this.serviceVersion = serviceVersion;
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (Object.class == method.getDeclaringClass()) {
            String name = method.getName();
            if (EQUALS.equals(name)) {
                return proxy = args[0];
            } else if (HASH_CODE.equals(name)) {
                System.identityHashCode(proxy);
            } else if (TO_STRING.equals(name)) {
                return proxy.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(proxy)) + ", with InvocationHandler" + this;
            }else{
                throw new IllegalStateException(String.valueOf(method));
            }
        }
        Request request = new Request();
        request.setRequestId(UUID.randomUUID().toString());
        request.setClassName(method.getDeclaringClass().getName());
        request.setServiceVersion(this.serviceVersion);
        request.setMethodName(method.getName());
        request.setParameterTypes(method.getParameterTypes());
        request.setParameters(args);

        log.debug(method.getDeclaringClass().getName());
        log.debug(method.getName());
        for(int i=0;i<method.getParameterTypes().length;i++){
            log.debug(method.getParameterTypes()[i].getName());
        }
        for(int i=0;i<args.length;i++){
            log.debug(args[i].toString());
        }

        Consumer consumer = new Consumer(this.serviceRegistry);
        Response response = consumer.sendRequest(request);
        if(null != response){
            log.debug("consumer receive provider rpc response:" + response.toString());
            return response.getResult();
        }else{
            throw new RuntimeException("consumer rpc fail, response is null!");
        }
    }
}
