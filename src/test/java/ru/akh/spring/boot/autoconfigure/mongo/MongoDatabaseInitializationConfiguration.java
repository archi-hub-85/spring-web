package ru.akh.spring.boot.autoconfigure.mongo;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import com.mongodb.client.MongoDatabase;

/**
 * Configures {@link MongoDatabase} initialization.
 */
@Configuration(proxyBeanMethods = false)
@Import({ MongoDatabaseInitializer.class, MongoDatabaseInitializationConfiguration.Registrar.class })
public class MongoDatabaseInitializationConfiguration {

    /**
     * {@link ImportBeanDefinitionRegistrar} to register the
     * {@link MongoDatabaseInitializerPostProcessor} without causing early bean
     * instantiation issues.
     */
    static class Registrar implements ImportBeanDefinitionRegistrar {

        private static final String BEAN_NAME = "mongoDatabaseInitializerPostProcessor";

        @Override
        public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
                BeanDefinitionRegistry registry) {
            if (!registry.containsBeanDefinition(BEAN_NAME)) {
                AbstractBeanDefinition beanDefinition = BeanDefinitionBuilder
                        .genericBeanDefinition(MongoDatabaseInitializerPostProcessor.class,
                                MongoDatabaseInitializerPostProcessor::new)
                        .getBeanDefinition();
                beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
                // We don't need this one to be post processed otherwise it can cause a
                // cascade of bean instantiation that we would rather avoid.
                beanDefinition.setSynthetic(true);
                registry.registerBeanDefinition(BEAN_NAME, beanDefinition);
            }
        }

    }

}
