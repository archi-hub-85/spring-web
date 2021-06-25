package ru.akh.spring_web.controller;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import ru.akh.spring_web.dto.Author;
import ru.akh.spring_web.dto.Book;

@WebMvcTest(BookController.class)
public class BookControllerTest extends AbstractControllerTest {

    @Test
    @WithReader
    public void testGetBook() throws Exception {
        long id = 1;

        Book book = createBook(id, "title1", 2021, 2L, "name1");
        Author author = book.getAuthor();
        Mockito.when(repository.get(id)).thenReturn(book);

        performGetBook(id)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(id))
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value(book.getTitle()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.year").value(book.getYear()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.author.id").value(author.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.author.name").value(author.getName()));
    }

    @Test
    @WithWriter
    public void testPutBook() throws Exception {
        Mockito.when(repository.put(Mockito.any())).thenReturn(1L);

        performPutBook(1L, "title", 2021, 1L, "author")
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(MockMvcResultMatchers.content().string("1"));
    }

    @Test
    @WithReader
    public void testGetTopBooks() throws Exception {
        Book book1 = createBook(1L, "title1", 2021, 1L, "name1");
        Book book2 = createBook(2L, "title2", 2022, 2L, "name2");
        Mockito.when(repository.getTopBooks(Book.Field.ID, 2))
                .thenReturn(Arrays.asList(book1, book2));

        performGetTopBooks(Book.Field.ID, 2)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(book1.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].title").value(book1.getTitle()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].year").value(book1.getYear()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].author.id").value(book1.getAuthor().getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].author.name").value(book1.getAuthor().getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].id").value(book2.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].title").value(book2.getTitle()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].year").value(book2.getYear()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].author.id").value(book2.getAuthor().getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].author.name").value(book2.getAuthor().getName()));
    }

    @Test
    @WithReader
    public void testGetBooksByAuthor() throws Exception {
        Book book1 = createBook(1L, "title1", 2021, 1L, "name1");
        Book book2 = createBook(2L, "title2", 2022, 1L, "name1");
        Mockito.when(repository.getBooksByAuthor("author"))
                .thenReturn(Arrays.asList(book1, book2));

        performGetBooksByAuthor("author")
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(book1.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].title").value(book1.getTitle()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].year").value(book1.getYear()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].author.id").value(book1.getAuthor().getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].author.name").value(book1.getAuthor().getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].id").value(book2.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].title").value(book2.getTitle()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].year").value(book2.getYear()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].author.id").value(book2.getAuthor().getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].author.name").value(book2.getAuthor().getName()));
    }

    @Test
    @WithAnonymousUser
    public void testGetWithWrongUser() throws Exception {
        performGetBook(1)
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @WithWriter
    public void testGetWithWrongRole() throws Exception {
        performGetBook(1)
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithAnonymousUser
    public void testPutWithWrongUser() throws Exception {
        performPutBook(1L, "title", 2020, null, "author")
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @WithReader
    public void testPutWithWrongRole() throws Exception {
        performPutBook(1L, "title", 2020, null, "author")
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithAnonymousUser
    public void testGetTopBooksWithWrongUser() throws Exception {
        performGetTopBooks(Book.Field.ID, 2)
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @WithWriter
    public void testGetTopBooksWithWrongRole() throws Exception {
        performGetTopBooks(Book.Field.ID, 2)
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithAnonymousUser
    public void testGetBooksByAuthorWithWrongUser() throws Exception {
        performGetBooksByAuthor("author")
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @WithWriter
    public void testGetBooksByAuthorWithWrongRole() throws Exception {
        performGetBooksByAuthor("author")
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    private ResultActions performGetBook(long id) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.get("/books/{id}", id).accept(MediaType.APPLICATION_JSON));
    }

    private ResultActions performPutBook(Long id, String title, int year, Long authorId, String authorName)
            throws Exception {
        // jsonBook = new ObjectMapper().writeValueAsString(book);
        String jsonBook = "{ \"id\": " + id + ", \"title\": \"" + title + "\", \"year\": " + year
                + ", \"author\": { \"id\": " + authorId + ", \"name\": \"" + authorName + "\" } }";

        return mockMvc.perform(
                MockMvcRequestBuilders.put("/books").with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(jsonBook));
    }

    private ResultActions performGetTopBooks(Book.Field field, int limit) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.get("/books/").queryParam("field", String.valueOf(field))
                .queryParam("top", String.valueOf(limit)).accept(MediaType.APPLICATION_JSON));
    }

    private ResultActions performGetBooksByAuthor(String author) throws Exception {
        return mockMvc.perform(
                MockMvcRequestBuilders.get("/books/").queryParam("author", author).accept(MediaType.APPLICATION_JSON));
    }

    private static Book createBook(Long id, String title, int year, Long authorId, String authorName) {
        Book book = new Book();
        book.setId(id);
        book.setTitle(title);
        book.setYear(year);

        Author author = new Author();
        author.setId(authorId);
        author.setName(authorName);
        book.setAuthor(author);

        return book;
    }

}
