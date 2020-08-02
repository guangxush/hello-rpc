## 自定义实现RPC


### RPC使用方式
0. 打开zookeeper
```text

```

1. 打包```mvn clean install -Dmaven.test.skip=true```

2. 其他项目里面引入jar包和Maven依赖，注意版本号

```text
<dependency>
    <groupId>com.shgx</groupId>
    <artifactId>rpc</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

3. 设置配置文件：application.properties
```text
# rpc
rpc.service-address=127.0.0.1:6689
rpc.service-registry-type=zookeeper
rpc.service-registry-address=127.0.0.1:2181
```

4. 服务发布

```java
@MyProvider(serviceInterface = HelloService.class, version = "0.0.1")
public class HelloServiceImpl implements HelloService {
    @Override
    public String hello(String param) {
        return "hello, " + param;
    }
}
```

5. 服务调用

```java
@RestController
public class HelloController {

    @MyConsumer(version = "0.0.1")
    private HelloService helloService;

    @GetMapping("/hello")
    public String testHello(@RequestParam String param){
        return helloService.hello(param);
    }
}
```

6. 参考demo

[测试demo](./test-rpc)

### 参考文档

[一个轻量级RPC框架](https://www.cnblogs.com/luxiaoxun/p/5272384.html)