package com.shgx.rpc.consumer;

import com.shgx.rpc.annotation.MyConsumer;
import com.shgx.rpc.constants.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author: guangxush
 * @create: 2020/06/13
 */
@Slf4j
public class ConsumerPostProcessor implements BeanFactoryPostProcessor, BeanClassLoaderAware, ApplicationContextAware {

    private ConfigurableListableBeanFactory beanFactory;
    private ClassLoader classLoader;
    private ApplicationContext context;

    /**
     * 保证有序性
     */
    private Map<String, BeanDefinition> beanDefinitionMap = new LinkedHashMap<>();

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
        log.debug("classLoader: {}" , this.classLoader.toString());
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
        //遍历容器里的所有bean
        for (String beanName : beanFactory.getBeanDefinitionNames()) {
            BeanDefinition definition = beanFactory.getBeanDefinition(beanName);
            String beanClassName = definition.getBeanClassName();
            // 当用 @Bean 返回的类型是Object时，beanClassName是 null
            if(beanClassName != null) {
                //使用反射获取bean的class对象，注意classloader是容器加载bean的classloader
                Class<?> clazz = ClassUtils.resolveClassName(definition.getBeanClassName(), this.classLoader);
                ReflectionUtils.doWithFields(clazz, this::parseElement);
            }
        }

        //重新注入到容器中
        BeanDefinitionRegistry registry = (BeanDefinitionRegistry)beanFactory;
        this.beanDefinitionMap.forEach((beanName, beanDefinition) -> {
            if (context.containsBean(beanName)) {
                throw new IllegalArgumentException("[RPC Starter] Spring context already has a bean named " + beanName
                        + ", please change @MyConsumer field name.");
            }
            registry.registerBeanDefinition(beanName, beanDefinitionMap.get(beanName));
            log.info("registered ConsumerBean {} in spring context.", beanName);
        });
    }

    /**
     * 动态修改被MyConsumer注解的bean，改为代理类
     * @param field
     */
    private void parseElement(Field field) {
        MyConsumer annotation = AnnotationUtils.getAnnotation(field, MyConsumer.class);
        if (annotation == null) {
            return;
        }
        //构造工厂bean的参数
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(ConsumerBean.class);
        builder.setInitMethodName(Constants.INIT_METHOD);
        builder.addPropertyValue("serviceVersion", annotation.serviceVersion());
        builder.addPropertyValue("interfaceClass", field.getType());
        builder.addPropertyValue("registryType", annotation.registryType());
        builder.addPropertyValue("registryAddress", annotation.registryAddress());

        BeanDefinition beanDefinition = builder.getBeanDefinition();
        beanDefinitionMap.put(field.getName(), beanDefinition);
    }
}
