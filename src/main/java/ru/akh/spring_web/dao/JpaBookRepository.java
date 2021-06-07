package ru.akh.spring_web.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import ru.akh.spring_web.dao.exception.AuthorNotFoundException;
import ru.akh.spring_web.dao.exception.BookContentNotFoundException;
import ru.akh.spring_web.dao.exception.BookNotFoundException;
import ru.akh.spring_web.dto.Author;
import ru.akh.spring_web.dto.Book;
import ru.akh.spring_web.dto.BookContent;

@Repository("bookRepository")
@Profile("jpa")
@Transactional(readOnly = true)
public class JpaBookRepository implements BookRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Book get(long id) {
        Book book = em.find(Book.class, id);
        if (book == null) {
            throw new BookNotFoundException(id);
        }

        em.detach(book);

        return book;
    }

    @Override
    @Transactional
    public long put(Book book) {
        Long authorId = book.getAuthor().getId();
        if ((authorId != null) && (em.find(Author.class, authorId) == null)) {
            throw new AuthorNotFoundException(authorId);
        }

        Long id = book.getId();
        if (id == null) {
            em.persist(book);
            id = book.getId();
        } else {
            Book existingBook = em.find(Book.class, id);
            if (existingBook == null) {
                throw new BookNotFoundException(id);
            }

            em.merge(book);
        }

        em.detach(book);

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

        return em.createQuery("from Book order by " + fieldName, Book.class).setMaxResults(limit).getResultList();
    }

    @Override
    public List<Book> getBooksByAuthor(String author) {
        return em.createQuery("from Book where author.name = ?1", Book.class).setParameter(1, author).getResultList();
    }

    @Override
    public BookContent getContent(long id) {
        BookContent content = em.find(BookContent.class, id);
        if (content == null) {
            throw new BookContentNotFoundException(id);
        }

        em.detach(content);

        return content;
    }

    @Override
    @Transactional
    public void putContent(BookContent content) {
        long id = content.getId();
        Book existingBook = em.find(Book.class, id);
        if (existingBook == null) {
            throw new BookNotFoundException(id);
        }

        em.merge(content);
        em.detach(content);
    }

}
