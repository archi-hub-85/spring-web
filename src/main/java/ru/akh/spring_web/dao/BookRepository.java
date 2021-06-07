package ru.akh.spring_web.dao;

import java.util.List;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import ru.akh.spring_web.dto.Book;
import ru.akh.spring_web.dto.BookContent;

public interface BookRepository {

    @NotNull
    Book get(long id);

    long put(@NotNull Book book);

    List<Book> getTopBooks(@NotNull Book.Field field, @Min(1) int limit);

    List<Book> getBooksByAuthor(@NotNull String author);

    @NotNull
    BookContent getContent(long id);

    @NotNull
    void putContent(@NotNull BookContent content);

}
