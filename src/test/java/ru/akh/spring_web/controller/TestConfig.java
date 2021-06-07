package ru.akh.spring_web.controller;

import org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import ru.akh.spring_web.interceptor.DebugInterceptor;

@TestConfiguration
public class TestConfig {

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
    public class TestSecurityConfig extends WebSecurityConfigurerAdapter {

        @Override
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            auth.inMemoryAuthentication()
                    .withUser(UsersConstants.READER_USERNAME).password("{noop}" + UsersConstants.READER_PASSWORD)
                    .roles("READER")
                    .and().withUser(UsersConstants.WRITER_USERNAME).password("{noop}" + UsersConstants.WRITER_PASSWORD)
                    .roles("WRITER")
                    .and().withUser(UsersConstants.ADMIN_USERNAME).password("{noop}" + UsersConstants.ADMIN_PASSWORD)
                    .roles("READER", "WRITER");
        }

    }

}
