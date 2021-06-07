package ru.akh.spring_web.dao.exception;

@SuppressWarnings("serial")
public class BookNotFoundException extends BookException {

    public BookNotFoundException(String msg) {
        super(msg);
    }

    public BookNotFoundException(long id) {
        this("Book[id=" + id + "] not found");
    }

}
