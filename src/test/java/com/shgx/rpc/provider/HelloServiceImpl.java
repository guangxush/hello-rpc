package com.shgx.rpc.provider;

import com.shgx.rpc.annotation.MyProvider;
import com.shgx.rpc.consumer.HelloService;

/**
 * @author: guangxush
 * @create: 2020/06/13
 */
@MyProvider(serviceInterface = HelloService.class, version = "0.0.1")
public class HelloServiceImpl implements HelloService {
    @Override
    public String hello(String name) {
        return "hello " + name;
    }
}
