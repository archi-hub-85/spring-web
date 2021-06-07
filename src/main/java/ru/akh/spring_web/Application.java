package ru.akh.spring_web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Configuration
    @EnableGlobalMethodSecurity(securedEnabled = true)
    public class MethodSecurityConfig extends GlobalMethodSecurityConfiguration {

    }

    @Configuration
    @Profile("jdbc")
    public class JdbcConfig {

        @Bean
        public LobHandler getLobHandler() {
            return new DefaultLobHandler();
        }

    }

}
