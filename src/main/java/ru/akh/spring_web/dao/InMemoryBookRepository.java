package ru.akh.spring_web.dao;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import ru.akh.spring_web.dao.exception.AuthorNotFoundException;
import ru.akh.spring_web.dao.exception.BookContentNotFoundException;
import ru.akh.spring_web.dao.exception.BookNotFoundException;
import ru.akh.spring_web.dto.Author;
import ru.akh.spring_web.dto.Book;
import ru.akh.spring_web.dto.BookContent;

@Repository("bookRepository")
@Profile("inMemory")
public class InMemoryBookRepository implements BookRepository {

    private static final Map<Book.Field, Comparator<Book>> FIELD_COMPARATORS = new HashMap<>(
            Book.Field.values().length);

    static {
        FIELD_COMPARATORS.put(Book.Field.ID, Comparator.comparing(Book::getId));
        FIELD_COMPARATORS.put(Book.Field.TITLE, Comparator.comparing(Book::getTitle));
        FIELD_COMPARATORS.put(Book.Field.YEAR, Comparator.comparing(Book::getYear));
        FIELD_COMPARATORS.put(Book.Field.AUTHOR,
                Comparator.comparing(((Function<Book, Author>) Book::getAuthor).andThen(Author::getName)));
    }

    private TreeMap<Long, Author> authors = new TreeMap<>();
    private TreeMap<Long, Book> books = new TreeMap<>();
    private Map<Long, BookContent> contents = new HashMap<>();

    @Override
    public Book get(long id) {
        Book book = books.get(id);
        if (book == null) {
            throw new BookNotFoundException(id);
        }

        return book;
    }

    @Override
    public long put(Book book) {
        synchronized (authors) {
            Author author = book.getAuthor();
            Long authorId = author.getId();
            if (authorId == null) {
                authorId = (authors.isEmpty() ? 0 : authors.lastKey()) + 1;
                author.setId(authorId);
            } else if (!authors.containsKey(authorId)) {
                throw new AuthorNotFoundException(authorId);
            }

            authors.put(authorId, author);
        }

        synchronized (books) {
            Long id = book.getId();
            if (id == null) {
                id = (books.isEmpty() ? 0 : books.lastKey()) + 1;
                book.setId(id);
            } else if (!books.containsKey(id)) {
                throw new BookNotFoundException(id);
            }

            books.put(id, book);
            return id;
        }
    }

    @Override
    public List<Book> getTopBooks(Book.Field field, int limit) {
        return books.values().stream().sorted(FIELD_COMPARATORS.get(field)).limit(limit).collect(Collectors.toList());
    }

    @Override
    public List<Book> getBooksByAuthor(String author) {
        return books.values().stream().filter(book -> book.getAuthor().getName().equalsIgnoreCase(author))
                .collect(Collectors.toList());
    }

    @Override
    public BookContent getContent(long id) {
        BookContent content = contents.get(id);
        if (content == null) {
            throw new BookContentNotFoundException(id);
        }

        return content;
    }

    @Override
    public void putContent(BookContent content) {
        long id = content.getId();
        if (!books.containsKey(id)) {
            throw new BookNotFoundException(id);
        }

        contents.put(id, content);
    }

}
