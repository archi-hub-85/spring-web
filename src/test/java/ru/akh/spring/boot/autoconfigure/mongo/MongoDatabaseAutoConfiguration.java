package ru.akh.spring.boot.autoconfigure.mongo;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.mongodb.client.MongoDatabase;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(MongoDatabase.class)
@EnableConfigurationProperties(MongoDatabaseProperties.class)
@Import(MongoDatabaseInitializationConfiguration.class)
public class MongoDatabaseAutoConfiguration {

}
