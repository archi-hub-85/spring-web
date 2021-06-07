package ru.akh.spring_web.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;

@SpringBootTest
@Import(TestConfig.class)
@TestPropertySource("classpath:/application-test.properties")
abstract class AbstractControllerTest {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private WebApplicationContext wac;

    protected WebTestClient client;

    private long testStartTime;

    @BeforeEach
    void setUp() {
        client = MockMvcWebTestClient.bindToApplicationContext(this.wac)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .defaultRequest(MockMvcRequestBuilders.get("/").with(SecurityMockMvcRequestPostProcessors.csrf()))
                .configureClient()
                // .apply(SecurityMockServerConfigurers.csrf())
                .build();
    }

    @BeforeEach
    void beforeTest(TestInfo testInfo) {
        logger.debug("Starting test {}...", testInfo.getDisplayName());
        testStartTime = System.currentTimeMillis();

        WithUser withUser = AnnotationUtils.findAnnotation(testInfo.getTestMethod().get(), WithUser.class);
        if (withUser == null) {
            withUser = AnnotationUtils.findAnnotation(testInfo.getTestClass().get(), WithUser.class);
        }
        if (withUser != null) {
            String username = withUser.username();
            String password = withUser.password();
            logger.debug("with user: username = {}, password = {}", username, password);
            client = client.mutate()
                    .filter(ExchangeFilterFunctions.basicAuthentication(username, password))
                    .build();
        }
    }

    @AfterEach
    void afterTest(TestInfo testInfo) {
        long duration = System.currentTimeMillis() - testStartTime;
        logger.debug("Test {} took {} ms.", testInfo.getDisplayName(), duration);
    }

}
