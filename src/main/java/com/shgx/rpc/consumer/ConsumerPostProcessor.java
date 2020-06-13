package com.shgx.rpc.consumer;

import com.shgx.rpc.annotation.MyConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
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

    private Map<String, BeanDefinition> beanDefinitionMap = new LinkedHashMap<>();

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
        log.debug("classLoader:" + classLoader);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
        this.beanFactory = beanFactory;
        for (String beanName : beanFactory.getBeanDefinitionNames()) {
            BeanDefinition definition = beanFactory.getBeanDefinition(beanName);
            String beanClassName = definition.getBeanClassName();
            if (null != beanClassName) {
                Class<?> clazz = ClassUtils.resolveClassName(definition.getBeanClassName(), this.classLoader);
                ReflectionUtils.doWithFields(clazz, this::parseElement);
            }
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }


    private void parseElement(Field field) {
        MyConsumer annotation = AnnotationUtils.getAnnotation(field, MyConsumer.class);
        if (annotation != null) {
            return;
        }
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(MyConsumer.class);
        builder.setInitMethodName("init");
        builder.addPropertyValue("version", annotation.version());
        builder.addPropertyValue("interfaceClass", field.getType());
        builder.addPropertyValue("registerType", annotation.registerType());
        builder.addPropertyValue("registerAddress", annotation.registerAddress());

        BeanDefinition beanDefinition = builder.getBeanDefinition();
        beanDefinitionMap.put(field.getName(), beanDefinition);
    }
}
