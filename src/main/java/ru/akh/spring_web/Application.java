package ru.akh.spring_web;

import java.util.Arrays;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

import ru.akh.spring_web.dao.mongodb.BookWriteConverter;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Configuration
    @EnableGlobalMethodSecurity(securedEnabled = true)
    public static class MethodSecurityConfig extends GlobalMethodSecurityConfiguration {

    }

    @Configuration
    @Profile("jdbc")
    public static class JdbcConfig {

        @Bean
        public LobHandler getLobHandler() {
            return new DefaultLobHandler();
        }

    }

    @Configuration
    @Profile("mongodb")
    public static class MongoConfig {

        @Bean
        MongoTransactionManager transactionManager(MongoDatabaseFactory dbFactory) {
            return new MongoTransactionManager(dbFactory);
        }

        @Bean
        MongoCustomConversions mongoCustomConversions() {
            return new MongoCustomConversions(Arrays.asList(BookWriteConverter.INSTANCE));
        }

    }

}
