package ru.akh.spring_web.controller;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.ContentDisposition;
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

        BookContent content = new BookContent();
        content.setId(id);
        content.setFileName("test.txt");
        content.setMimeType(MediaType.TEXT_PLAIN_VALUE);
        content.setContent("test content".getBytes(StandardCharsets.UTF_8));
        Mockito.when(repository.getContent(id)).thenReturn(content);

        performDownload(id)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.header().string(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(content.getFileName()).build().toString()))
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(MockMvcResultMatchers.content().bytes(content.getContent()));
    }

    @Test
    @WithWriter
    public void testUpload() throws Exception {
        performUpload(2, "/c:\\test.txt", "test content")
                .andExpect(MockMvcResultMatchers.status().isOk());

        ArgumentCaptor<BookContent> captor = ArgumentCaptor.forClass(BookContent.class);
        Mockito.verify(repository).putContent(captor.capture());
        BookContent content = captor.getValue();
        Assertions.assertEquals(2, content.getId(), "content.id");
        Assertions.assertEquals("test.txt", content.getFileName(), "content.fileName");
        Assertions.assertEquals(MediaType.TEXT_PLAIN_VALUE, content.getMimeType(), "content.mimeType");
        Assertions.assertArrayEquals("test content".getBytes(StandardCharsets.UTF_8), content.getContent(),
                "content.content");
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
