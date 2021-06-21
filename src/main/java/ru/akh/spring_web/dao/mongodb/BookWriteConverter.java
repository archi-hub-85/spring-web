package ru.akh.spring_web.dao.mongodb;

import org.bson.Document;
import org.springframework.core.convert.converter.Converter;

import com.mongodb.DBRef;

import ru.akh.spring_web.dto.Author;
import ru.akh.spring_web.dto.Book;

public class BookWriteConverter implements Converter<Book, Document> {

    public static final BookWriteConverter INSTANCE = new BookWriteConverter();

    private BookWriteConverter() {
    }

    @Override
    public Document convert(Book source) {
        Document document = new Document();
        document.put("_id", source.getId());
        document.put("title", source.getTitle());
        document.put("year", source.getYear());

        Author author = source.getAuthor();
        document.put("author", new DBRef(Constants.CollectionNames.AUTHORS, author.getId()));

        return document;
    }

}
