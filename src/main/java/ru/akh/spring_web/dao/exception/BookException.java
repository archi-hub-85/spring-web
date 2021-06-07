package ru.akh.spring_web.dao.exception;

import org.springframework.dao.NonTransientDataAccessException;

@SuppressWarnings("serial")
public class BookException extends NonTransientDataAccessException {

    public BookException(String msg) {
        super(msg);
    }

}
