package ru.akh.spring_web.dao.mongodb;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mongodb.DBRef;
import com.mongodb.client.result.UpdateResult;

import ru.akh.spring_web.dao.BookRepository;
import ru.akh.spring_web.dao.exception.AuthorNotFoundException;
import ru.akh.spring_web.dao.exception.BookContentNotFoundException;
import ru.akh.spring_web.dao.exception.BookNotFoundException;
import ru.akh.spring_web.dto.Author;
import ru.akh.spring_web.dto.Book;
import ru.akh.spring_web.dto.BookContent;

@Repository("bookRepository")
@Profile("mongodb")
@Transactional(readOnly = true)
public class MongoBookRepository implements BookRepository {

    @Autowired
    private MongoTemplate template;

    @Autowired
    private SequenceGenerator sequenceGenerator;

    @Override
    public Book get(long id) {
        Book book = template.findById(id, Book.class, Constants.CollectionNames.BOOKS);
        if (book == null) {
            throw new BookNotFoundException(id);
        }

        return book;
    }

    @Override
    @Transactional
    public long put(Book book) {
        Author author = book.getAuthor();
        Long authorId = author.getId();
        if (authorId == null) {
            authorId = sequenceGenerator.generateSequence(Constants.SequenceNames.AUTHORS);
            author.setId(authorId);
            template.insert(author, Constants.CollectionNames.AUTHORS);
        } else {
            UpdateResult updateResult = template.updateFirst(Query.query(Criteria.where("_id").is(authorId)),
                    Update.update("name", author.getName()), Author.class, Constants.CollectionNames.AUTHORS);
            if (updateResult.getMatchedCount() == 0) {
                throw new AuthorNotFoundException(authorId);
            }
        }

        Long id = book.getId();
        if (id == null) {
            id = sequenceGenerator.generateSequence(Constants.SequenceNames.BOOKS);
            book.setId(id);
            template.insert(book, Constants.CollectionNames.BOOKS);
        } else {
            UpdateResult updateResult = template.updateFirst(Query.query(Criteria.where("_id").is(id)),
                    new Update().set("title", book.getTitle()).set("year", book.getYear()).set("author",
                            new DBRef(Constants.CollectionNames.AUTHORS, author.getId())),
                    Book.class, Constants.CollectionNames.BOOKS);
            if (updateResult.getMatchedCount() == 0) {
                throw new BookNotFoundException(id);
            }
        }

        return id;
    }

    @Override
    public List<Book> getTopBooks(Book.Field field, int limit) {
        switch (field) {
        case ID:
        case TITLE:
        case YEAR: {
            String fieldName = field.toString().toLowerCase();
            Query query = new Query().with(Sort.by(fieldName)).limit(limit);
            return template.find(query, Book.class, Constants.CollectionNames.BOOKS);
        }
        case AUTHOR: {
            Query authorsQuery = new Query().with(Sort.by("name"));
            List<Author> authors = template.find(authorsQuery, Author.class, Constants.CollectionNames.AUTHORS);
            List<Long> authorIds = authors.stream().map(Author::getId).collect(Collectors.toList());

            List<Book> books = new ArrayList<>(limit);
            for (long authorId : authorIds) {
                Query booksQuery = Query.query(Criteria.where("author.$id").is(authorId)).limit(limit - books.size());
                List<Book> booksByAuthor = template.find(booksQuery, Book.class, Constants.CollectionNames.BOOKS);
                books.addAll(booksByAuthor);
                if (books.size() >= limit) {
                    break;
                }
            }

            return books;
        }
        default:
            throw new IllegalArgumentException("Unknown field value: " + field);
        }
    }

    @Override
    public List<Book> getBooksByAuthor(String author) {
        Query authorQuery = Query.query(Criteria.where("name").is(author));
        List<Author> authors = template.find(authorQuery, Author.class, Constants.CollectionNames.AUTHORS);
        Set<Long> authorIds = authors.stream().map(Author::getId).collect(Collectors.toSet());

        Query query = Query.query(Criteria.where("author.$id").in(authorIds));
        return template.find(query, Book.class, Constants.CollectionNames.BOOKS);
    }

    @Override
    public BookContent getContent(long id) {
        BookContent content = template.findById(id, BookContent.class, Constants.CollectionNames.BOOKS);
        if (content == null) {
            throw new BookContentNotFoundException(id);
        }

        if (content.getSize() == 0) {
            content.setSize(content.getContent().length);
        }

        return content;
    }

    @Override
    @Transactional
    public void putContent(BookContent content) {
        long id = content.getId();
        UpdateResult updateResult = template.updateFirst(Query.query(Criteria.where("_id").is(id)),
                new Update().set("fileName", content.getFileName()).set("mimeType", content.getMimeType())
                        .set("content", content.getContent()),
                BookContent.class, Constants.CollectionNames.BOOKS);
        if (updateResult.getMatchedCount() == 0) {
            throw new BookNotFoundException(id);
        }
    }

}
