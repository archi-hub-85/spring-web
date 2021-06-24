package ru.akh.spring_web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import ru.akh.spring_web.AbstractTest;
import ru.akh.spring_web.dao.BookRepository;

abstract class AbstractControllerTest extends AbstractTest {

    @Autowired
    protected MockMvc mockMvc;

    @MockBean
    protected BookRepository repository;

}
