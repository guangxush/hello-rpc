package com.shgx.test.controller;

import com.shgx.rpc.annotation.MyConsumer;
import com.shgx.test.service.HelloService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author: guangxush
 * @create: 2020/06/13
 */
@RestController
public class HelloController {

    @MyConsumer(serviceVersion = "0.0.2")
    private HelloService helloService;

    @GetMapping("/hello")
    public String testHello(@RequestParam String param){
        // http://localhost:8081/hello?param=rpc
        return helloService.hello(param);
    }

}
