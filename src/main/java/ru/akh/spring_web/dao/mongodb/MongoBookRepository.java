package ru.akh.spring_web.dao.mongodb;

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
        String fieldName;
        switch (field) {
        case ID:
        case TITLE:
        case YEAR:
            fieldName = field.toString().toLowerCase();
            break;
        case AUTHOR:
            // TODO add implementation
            throw new UnsupportedOperationException("Sorting by author not supported yet");
        default:
            throw new IllegalArgumentException("Unknown field value: " + field);
        }

        Query query = new Query().with(Sort.by(fieldName)).limit(limit);
        return template.find(query, Book.class, Constants.CollectionNames.BOOKS);
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    @Transactional
    public void putContent(BookContent content) {
        // TODO Auto-generated method stub
    }

}
