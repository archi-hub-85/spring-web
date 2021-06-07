package ru.akh.spring_web.dao;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import ru.akh.spring_web.dto.Author;
import ru.akh.spring_web.dto.Book;
import ru.akh.spring_web.dto.BookContent;

@Component
@Profile("inMemory")
public class InMemoryBookRepositoryInitializer {

    @Autowired
    private BookRepository repository;

    private Map<String, Author> authors = new HashMap<>();

    @PostConstruct
    public void fill() {
        addBookWithContent("The Dark Tower: The Gunslinger", 1982, "Stephen King", "dark_tower_1.txt",
                "The Dark Tower: The Gunslinger");
        addBookWithContent("The Dark Tower II: The Drawing of the Three", 1987, "Stephen King", "dark_tower_2.txt",
                "The Dark Tower II: The Drawing of the Three");
        addBookWithContent("The Dark Tower III: The Waste Lands", 1991, "Stephen King", "dark_tower_3.txt",
                "The Dark Tower III: The Waste Lands");
        addBookWithContent("The Dark Tower IV: Wizard and Glass", 1997, "Stephen King", "dark_tower_4.txt",
                "The Dark Tower IV: Wizard and Glass");
        addBookWithContent("The Dark Tower V: Wolves of the Calla", 2003, "Stephen King", "dark_tower_5.txt",
                "The Dark Tower V: Wolves of the Calla");
        addBookWithContent("The Dark Tower VI: Song of Susannah", 2004, "Stephen King", "dark_tower_6.txt",
                "The Dark Tower VI: Song of Susannah");
        addBookWithContent("The Dark Tower VII: The Dark Tower", 2004, "Stephen King", "dark_tower_7.txt",
                "The Dark Tower VII: The Dark Tower");

        addBookWithContent("Foundation", 1951, "Isaac Asimov", "foundation_1.txt", "Foundation");
        addBookWithContent("Foundation and Empire", 1952, "Isaac Asimov", "foundation_2.txt", "Foundation and Empire");
        addBookWithContent("Second Foundation", 1953, "Isaac Asimov", "foundation_3.txt", "Second Foundation");
        addBookWithContent("Foundation's Edge", 1982, "Isaac Asimov", "foundation_4.txt", "Foundation's Edge");
        addBookWithContent("Foundation and Earth", 1986, "Isaac Asimov", "foundation_5.txt", "Foundation and Earth");
        addBookWithContent("Prelude to Foundation", 1988, "Isaac Asimov", "foundation_6.txt", "Prelude to Foundation");
        addBookWithContent("Forward the Foundation", 1993, "Isaac Asimov", "foundation_7.txt",
                "Forward the Foundation");

        addBookWithContent("A Study in Scarlet", 1887, "Arthur Conan Doyle", "sherlock_holmes_1.txt",
                "A Study in Scarlet");
        addBookWithContent("The Sign of the Four", 1890, "Arthur Conan Doyle", "sherlock_holmes_2.txt",
                "The Sign of the Four");
        addBookWithContent("The Hound of the Baskervilles", 1902, "Arthur Conan Doyle", "sherlock_holmes_3.txt",
                "The Hound of the Baskervilles");
        addBookWithContent("The Valley of Fear", 1915, "Arthur Conan Doyle", "sherlock_holmes_4.txt",
                "The Valley of Fear");
    }

    private void addBookWithContent(String title, int year, String authorName, String fileName, String content) {
        Author author = authors.get(authorName);
        if (author == null) {
            author = new Author();
            author.setName(authorName);
            authors.put(authorName, author);
        }

        Book book = new Book();
        book.setTitle(title);
        book.setYear(year);
        book.setAuthor(author);
        long id = repository.put(book);

        BookContent bookContent = new BookContent();
        byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
        bookContent.setId(id);
        bookContent.setFileName(fileName);
        bookContent.setMimeType(MediaType.TEXT_PLAIN_VALUE);
        bookContent.setContent(contentBytes);
        bookContent.setSize(contentBytes.length);
        repository.putContent(bookContent);
    }

}
