package ru.akh.spring_web.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.AbstractLobCreatingPreparedStatementCallback;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import ru.akh.spring_web.dao.exception.AuthorNotFoundException;
import ru.akh.spring_web.dao.exception.BookNotFoundException;
import ru.akh.spring_web.dto.Author;
import ru.akh.spring_web.dto.Book;
import ru.akh.spring_web.dto.BookContent;

@Repository("bookRepository")
@Profile("jdbc")
@Transactional(readOnly = true)
public class JdbcBookRepository implements BookRepository {

    private static class TableNames {

        public static final String AUTHORS = "AUTHORS";
        public static final String BOOKS = "BOOKS";

    }

    private static class ColumnNames {

        public static final String ID = "ID";
        public static final String NAME = "NAME";
        public static final String TITLE = "TITLE";
        public static final String YEAR = "YEAR";
        public static final String AUTHOR_ID = "AUTHOR_ID";
        public static final String FILENAME = "FILENAME";
        public static final String MIMETYPE = "MIMETYPE";
        public static final String CONTENT = "CONTENT";
        public static final String SIZE = "SIZE";

    }

    private static final String GET_QUERY = MessageFormat.format(
            "select b.{0}, b.{1}, b.{2}, b.{3}, a.{4} from {5} b inner join {6} a on b.{3} = a.{0}",
            ColumnNames.ID, ColumnNames.TITLE, ColumnNames.YEAR, ColumnNames.AUTHOR_ID, ColumnNames.NAME,
            TableNames.BOOKS, TableNames.AUTHORS);

    private static final String GET_BY_ID_QUERY = GET_QUERY + MessageFormat.format(" where b.{0} = ?", ColumnNames.ID);

    private static final String INSERT_AUTHOR_QUERY = MessageFormat.format("insert into {0} ({1}) values (?)",
            TableNames.AUTHORS, ColumnNames.NAME);

    private static final String UPDATE_AUTHOR_QUERY = MessageFormat.format(
            "update {0} set {1} = ? where {2} = ?",
            TableNames.AUTHORS, ColumnNames.NAME, ColumnNames.ID);

    private static final String INSERT_QUERY = MessageFormat.format("insert into {0} ({1}, {2}, {3}) values (?, ?, ?)",
            TableNames.BOOKS, ColumnNames.TITLE, ColumnNames.YEAR, ColumnNames.AUTHOR_ID);

    private static final String UPDATE_QUERY = MessageFormat.format(
            "update {0} set {1} = ?, {2} = ?, {3} = ? where {4} = ?",
            TableNames.BOOKS, ColumnNames.TITLE, ColumnNames.YEAR, ColumnNames.AUTHOR_ID, ColumnNames.ID);

    private static final String GET_BY_AUTHOR_QUERY = GET_QUERY
            + MessageFormat.format(" where a.{0} = ?", ColumnNames.NAME);

    private static final String GET_CONTENT_QUERY = MessageFormat.format(
            "select {0}, {1}, {2}, LENGTH({2}) as \"{3}\" from {4} where {5} = ? and {2} is not null",
            ColumnNames.FILENAME, ColumnNames.MIMETYPE, ColumnNames.CONTENT, ColumnNames.SIZE, TableNames.BOOKS,
            ColumnNames.ID);

    private static final String UPDATE_CONTENT_QUERY = MessageFormat.format(
            "update {0} set {1} = ?, {2} = ?, {3} = ? where {4} = ?",
            TableNames.BOOKS, ColumnNames.FILENAME, ColumnNames.MIMETYPE, ColumnNames.CONTENT, ColumnNames.ID);

    private static final RowMapper<Book> BOOK_MAPPER = (resultSet, rowNum) -> {
        Book book = new Book();
        book.setId(resultSet.getLong(ColumnNames.ID));
        book.setTitle(resultSet.getString(ColumnNames.TITLE));
        book.setYear(resultSet.getInt(ColumnNames.YEAR));

        Author author = new Author();
        author.setId(resultSet.getLong(ColumnNames.AUTHOR_ID));
        author.setName(resultSet.getString(ColumnNames.NAME));
        book.setAuthor(author);

        return book;
    };

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    private LobHandler lobHandler;

    @Autowired
    public JdbcBookRepository(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public Book get(long id) {
        try {
            return jdbcTemplate.queryForObject(GET_BY_ID_QUERY, BOOK_MAPPER, id);
        } catch (EmptyResultDataAccessException e) {
            throw new BookNotFoundException(id);
        }
    }

    @Override
    @Transactional
    public long put(Book book) {
        Author author = book.getAuthor();
        Long authorId = author.getId();
        if (authorId == null) {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(INSERT_AUTHOR_QUERY,
                        new String[] { ColumnNames.ID });
                ps.setString(1, author.getName());
                return ps;
            }, keyHolder);
            authorId = keyHolder.getKey().longValue();
        } else {
            int count = jdbcTemplate.update(UPDATE_AUTHOR_QUERY, author.getName(), authorId);
            if (count == 0) {
                throw new AuthorNotFoundException(authorId);
            }
        }

        Long id = book.getId();

        if (id == null) {
            long finalAuthorId = authorId;
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(INSERT_QUERY, new String[] { ColumnNames.ID });
                ps.setString(1, book.getTitle());
                ps.setInt(2, book.getYear());
                ps.setLong(3, finalAuthorId);
                return ps;
            }, keyHolder);
            id = keyHolder.getKey().longValue();
        } else {
            int count = jdbcTemplate.update(UPDATE_QUERY, book.getTitle(), book.getYear(), authorId, id);
            if (count == 0) {
                throw new BookNotFoundException(id);
            }
        }

        return id;
    }

    @Override
    public List<Book> getTopBooks(Book.Field field, int limit) {
        String tableAlias = "b";
        String fieldName;
        switch (field) {
        case ID:
            fieldName = ColumnNames.ID;
            break;
        case TITLE:
            fieldName = ColumnNames.TITLE;
            break;
        case YEAR:
            fieldName = ColumnNames.YEAR;
            break;
        case AUTHOR:
            tableAlias = "a";
            fieldName = ColumnNames.NAME;
            break;
        default:
            throw new IllegalArgumentException("Unknown field value: " + field);
        }

        jdbcTemplate.setMaxRows(limit);
        try {
            return jdbcTemplate.query(MessageFormat.format(GET_QUERY + " order by {0}.{1}", tableAlias, fieldName),
                    BOOK_MAPPER);
        } finally {
            jdbcTemplate.setMaxRows(-1);
        }
    }

    @Override
    public List<Book> getBooksByAuthor(String author) {
        return jdbcTemplate.query(GET_BY_AUTHOR_QUERY, BOOK_MAPPER, author);
    }

    @Override
    public BookContent getContent(long id) {
        return jdbcTemplate.queryForObject(
                GET_CONTENT_QUERY,
                (resultSet, rowNum) -> {
                    BookContent bookContent = new BookContent();
                    bookContent.setId(id);
                    bookContent.setFileName(resultSet.getString(ColumnNames.FILENAME));
                    bookContent.setMimeType(resultSet.getString(ColumnNames.MIMETYPE));
                    bookContent.setContent(lobHandler.getBlobAsBytes(resultSet, ColumnNames.CONTENT));
                    bookContent.setSize(resultSet.getLong(ColumnNames.SIZE));
                    return bookContent;
                },
                id);
    }

    @Override
    @Transactional
    public void putContent(BookContent content) {
        int count = jdbcTemplate.execute(UPDATE_CONTENT_QUERY,
                new AbstractLobCreatingPreparedStatementCallback(lobHandler) {

                    @Override
                    protected void setValues(PreparedStatement ps, LobCreator lobCreator)
                            throws SQLException, DataAccessException {
                        ps.setString(1, content.getFileName());
                        ps.setString(2, content.getMimeType());
                        lobCreator.setBlobAsBytes(ps, 3, content.getContent());
                        ps.setLong(4, content.getId());
                    }

                });

        if (count == 0) {
            throw new BookNotFoundException(content.getId());
        }
    }

}
