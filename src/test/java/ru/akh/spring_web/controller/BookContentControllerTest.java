package ru.akh.spring_web.controller;

import java.nio.charset.StandardCharsets;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import ru.akh.spring_web.dto.BookContent;

@WebMvcTest(BookContentController.class)
public class BookContentControllerTest extends AbstractControllerTest {

    @Test
    @WithReader
    public void testDownload() throws Exception {
        long id = 1;
        String content = "The Dark Tower: The Gunslinger";

        BookContent storedBookContent = new BookContent();
        storedBookContent.setId(id);
        storedBookContent.setFileName("dark_tower_1.txt");
        storedBookContent.setMimeType(MediaType.TEXT_PLAIN_VALUE);
        storedBookContent.setContent(content.getBytes(StandardCharsets.UTF_8));
        Mockito.when(repository.getContent(id)).thenReturn(storedBookContent);

        performDownload(id)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.header().string(HttpHeaders.CONTENT_DISPOSITION,
                        Matchers.containsString("filename=\"" + storedBookContent.getFileName() + "\"")))
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(MockMvcResultMatchers.content().string(content));
    }

    @Test
    @WithWriter
    public void testUpload() throws Exception {
        performUpload(2, "test.txt", "test content")
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithAnonymousUser
    public void testDownloadWithWrongUser() throws Exception {
        performDownload(1)
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @WithWriter
    public void testDownloadWithWrongRole() throws Exception {
        performDownload(1)
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithAnonymousUser
    public void testUploadWithWrongUser() throws Exception {
        performUpload(1, "test.txt", "test content")
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @WithReader
    public void testUploadWithWrongRole() throws Exception {
        performUpload(1, "test.txt", "test content")
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    private ResultActions performDownload(long id) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.get("/books/download/{id}", id).accept(MediaType.ALL));
    }

    private ResultActions performUpload(long id, String fileName, String content) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.multipart("/books/upload")
                .part(new MockPart("id", String.valueOf(id).getBytes(StandardCharsets.UTF_8)))
                .file(new MockMultipartFile("file", fileName, MediaType.TEXT_PLAIN_VALUE,
                        content.getBytes(StandardCharsets.UTF_8)))
                .with(SecurityMockMvcRequestPostProcessors.csrf()));
    }

}
