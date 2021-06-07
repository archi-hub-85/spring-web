package ru.akh.spring_web.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationEvents {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationEvents.class);

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent success) {
        logger.debug("Auth successed: username = {}, authorities = {}", success.getAuthentication().getName(),
                success.getAuthentication().getAuthorities());
    }

    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent failures) {
        logger.debug("Auth failed: username = {}, exception = {}", failures.getAuthentication().getName(),
                failures.getException().getMessage());
    }

}
