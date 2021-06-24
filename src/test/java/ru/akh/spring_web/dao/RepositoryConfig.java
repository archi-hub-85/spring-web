package ru.akh.spring_web.dao;

import org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;

import ru.akh.spring_web.interceptor.DebugInterceptor;

@TestConfiguration
public class RepositoryConfig {

    @Bean
    @Profile({ "inMemory", "mongodb" })
    public BookRepositoryInitializer bookRepositoryInitializer(BookRepository repository) {
        return new BookRepositoryInitializer(repository);
    }

    @Bean("debugInterceptor")
    public DebugInterceptor getDebugInterceptor() {
        return new DebugInterceptor();
    }

    @Bean
    public BeanNameAutoProxyCreator getBeanNameAutoProxyCreator() {
        BeanNameAutoProxyCreator bean = new BeanNameAutoProxyCreator();
        bean.setBeanNames("bookRepository");
        bean.setInterceptorNames("debugInterceptor");
        return bean;
    }

    @TestConfiguration
    @Profile("mongodb")
    @ComponentScan("ru.akh.spring.boot.autoconfigure.mongo")
    public static class MongoConfig {

    }

}
