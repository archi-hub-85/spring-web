package ru.akh.spring_web.controller;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@WithUser(username = UsersConstants.READER_USERNAME, password = UsersConstants.READER_PASSWORD)
public @interface WithReader {

}
