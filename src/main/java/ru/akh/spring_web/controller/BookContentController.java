package ru.akh.spring_web.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import ru.akh.spring_web.access.SecuredWriter;
import ru.akh.spring_web.access.SecuredReader;
import ru.akh.spring_web.dao.BookRepository;
import ru.akh.spring_web.dao.exception.BookException;
import ru.akh.spring_web.dto.BookContent;

@Controller
@RequestMapping("/books")
public class BookContentController {

    @Autowired
    private BookRepository repository;

    @PostMapping(path = "/upload")
    @SecuredWriter
    public void upload(@RequestParam("id") long id, @RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new BookException("Empty file!");
        }

        try (InputStream content = file.getInputStream()) {
            BookContent bookContent = new BookContent();
            bookContent.setId(id);
            bookContent.setFileName(getFileName(file.getOriginalFilename()));
            bookContent.setMimeType(file.getContentType());
            bookContent.setContent(content.readAllBytes());
            bookContent.setSize(file.getSize());

            repository.putContent(bookContent);
        }
    }

    @GetMapping("/download/{id}")
    @SecuredReader
    public ResponseEntity<Resource> download(@PathVariable long id) throws IOException {
        BookContent bookContent = repository.getContent(id);

        try (InputStream content = new ByteArrayInputStream(bookContent.getContent())) {
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.CONTENT_TYPE, bookContent.getMimeType());
            headers.setContentDisposition(ContentDisposition.attachment().filename(bookContent.getFileName()).build());
            headers.setContentLength(bookContent.getSize());

            return new ResponseEntity<Resource>(new InputStreamResource(content), headers, HttpStatus.OK);
        }
    }

    // org.springframework.web.multipart.commons.CommonsMultipartFile#getOriginalFilename()
    private static String getFileName(String path) {
        int unixSep = path.lastIndexOf('/');
        int winSep = path.lastIndexOf('\\');
        int pos = Math.max(winSep, unixSep);

        return (pos >= 0) ? path.substring(pos + 1) : path;
    }

}
