package com.shgx.rpc.register;

import com.google.common.collect.Lists;
import com.shgx.rpc.commons.ProviderUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.x.discovery.*;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.apache.curator.x.discovery.strategies.RoundRobinStrategy;

import java.io.Closeable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.shgx.rpc.constants.Constants.BASE_URL;

/**
 * @author: guangxush
 * @create: 2020/06/11
 */
public class ZookeeperServiceRegistry implements ServiceRegistry {
    private final CuratorFramework client;
    private final Object lock = new Object();
    private ServiceDiscovery<ServiceModel> serviceDiscovery;

    /**
     * 本地缓存服务，避免过多创建请求
     */
    private Map<String, ServiceProvider<ServiceModel>> serviceProviderCache;
    private List<Closeable> closeableProvider = Lists.newArrayList();

    public ZookeeperServiceRegistry(String address) throws Exception {
        serviceProviderCache = new ConcurrentHashMap<>(256);
        this.client = CuratorFrameworkFactory.newClient(address, new ExponentialBackoffRetry(1000, 3));
        this.client.start();
        JsonInstanceSerializer<ServiceModel> serializer = new JsonInstanceSerializer<>(ServiceModel.class);
        serviceDiscovery = ServiceDiscoveryBuilder.
                builder(ServiceModel.class)
                .client(this.client)
                .serializer(serializer)
                .basePath(BASE_URL)
                .build();
        serviceDiscovery.start();
    }

    @Override
    public void register(ServiceModel serviceModel) throws Exception {
        ServiceInstance<ServiceModel> serviceInstance = ServiceInstance
                .<ServiceModel>builder()
                //使用{服务名}:{服务版本}唯一标识一个服务
                .name(ProviderUtils.generateKey(serviceModel.getServiceName(), serviceModel.getServiceVersion()))
                .address(serviceModel.getAddress())
                .port(serviceModel.getPort())
                .payload(serviceModel)
                .uriSpec(new UriSpec("{scheme}://{address}:{port}"))
                .build();
        serviceDiscovery.registerService(serviceInstance);
    }

    @Override
    public void unRegister(ServiceModel serviceModel) throws Exception {
        ServiceInstance<ServiceModel> serviceInstance =
                ServiceInstance.<ServiceModel>builder()
                .name(ProviderUtils.generateKey(serviceModel.getServiceName(), serviceModel.getServiceVersion()))
                .address(serviceModel.getAddress())
                .port(serviceModel.getPort())
                .payload(serviceModel)
                .uriSpec(new UriSpec("{scheme}://{address}:{port}"))
                .build();
        serviceDiscovery.unregisterService(serviceInstance);
    }

    @Override
    public ServiceModel discovery(String serviceName) throws Exception{
        // 读取缓存
        ServiceProvider<ServiceModel> serviceProvider = serviceProviderCache.get(serviceName);
        if (null == serviceProvider) {
            synchronized (lock) {
                serviceProvider = serviceDiscovery
                        .serviceProviderBuilder()
                        .serviceName(serviceName)
                        //设置负载均衡策略，这里使用轮询
                        .providerStrategy(new RoundRobinStrategy<>())
                        .build();
                serviceProvider.start();
                closeableProvider.add(serviceProvider);
                serviceProviderCache.put(serviceName, serviceProvider);
            }
        }
        ServiceInstance<ServiceModel> serviceInstance = serviceProvider.getInstance();
        return null != serviceInstance ? serviceInstance.getPayload() : null;
    }

    @Override
    public void close() throws Exception{
        for(Closeable closeable: closeableProvider) {
            CloseableUtils.closeQuietly(closeable);
        }
        serviceDiscovery.close();
    }
}
