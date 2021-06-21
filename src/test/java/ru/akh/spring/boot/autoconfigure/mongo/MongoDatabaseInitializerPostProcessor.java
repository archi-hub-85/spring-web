package ru.akh.spring.boot.autoconfigure.mongo;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * {@link BeanPostProcessor} used to ensure that
 * {@link MongoDatabaseInitializer} is initialized as soon as a
 * {@link MongoTemplate} is.
 */
public class MongoDatabaseInitializerPostProcessor implements BeanPostProcessor, Ordered, BeanFactoryAware {

    private BeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof MongoTemplate) {
            // force initialization of this bean as soon as we see a MongoTemplate
            beanFactory.getBean(MongoDatabaseInitializer.class);
        }
        return bean;
    }

}
