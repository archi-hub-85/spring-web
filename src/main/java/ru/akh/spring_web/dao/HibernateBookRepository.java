package ru.akh.spring_web.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import ru.akh.spring_web.dao.exception.BookContentNotFoundException;
import ru.akh.spring_web.dao.exception.BookNotFoundException;
import ru.akh.spring_web.dto.Book;
import ru.akh.spring_web.dto.BookContent;

@Repository("bookRepository")
@Profile("hibernate")
@Transactional(readOnly = true)
public class HibernateBookRepository implements BookRepository {

    @Autowired
    private SessionFactory sessionFactory;

    private Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

    @Override
    public Book get(long id) {
        Book book = getCurrentSession().get(Book.class, id);
        if (book == null) {
            throw new BookNotFoundException(id);
        }

        getCurrentSession().detach(book);

        return book;
    }

    @Override
    @Transactional
    public long put(Book book) {
        Long id = book.getId();
        if (id == null) {
            id = (Long) getCurrentSession().save(book);
        } else {
            Book existingBook = getCurrentSession().find(Book.class, id);
            if (existingBook == null) {
                throw new BookNotFoundException(id);
            }

            getCurrentSession().update(book);
        }

        getCurrentSession().detach(book);

        return id;
    }

    @Override
    public List<Book> getTopBooks(Book.Field field, int limit) {
        String fieldName;
        switch (field) {
        case ID:
        case TITLE:
        case YEAR:
            fieldName = field.toString().toLowerCase();
            break;
        case AUTHOR:
            fieldName = "author.name";
            break;
        default:
            throw new IllegalArgumentException("Unknown field value: " + field);
        }

        return getCurrentSession().createQuery("from Book order by " + fieldName, Book.class).setMaxResults(limit)
                .getResultList();
    }

    @Override
    public List<Book> getBooksByAuthor(String author) {
        return getCurrentSession().createQuery("from Book where author.name = ?1", Book.class).setParameter(1, author)
                .getResultList();
    }

    @Override
    public BookContent getContent(long id) {
        BookContent content = getCurrentSession().get(BookContent.class, id);
        if (content == null) {
            throw new BookContentNotFoundException(id);
        }

        getCurrentSession().detach(content);

        return content;
    }

    @Override
    @Transactional
    public void putContent(BookContent content) {
        getCurrentSession().update(content);
    }

}
