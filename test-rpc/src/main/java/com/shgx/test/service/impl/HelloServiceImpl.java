package com.shgx.test.service.impl;

import com.shgx.rpc.annotation.MyProvider;
import com.shgx.test.service.HelloService;

/**
 * @author: guangxush
 * @create: 2020/06/13
 */
@MyProvider(serviceVersion = "0.0.2", serviceInterface = HelloService.class)
public class HelloServiceImpl implements HelloService {
    @Override
    public String hello(String param) {
        return "hello, " + param;
    }
}
