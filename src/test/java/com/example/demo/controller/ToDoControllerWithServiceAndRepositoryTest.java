package com.example.demo.controller;

import com.example.demo.config.DemoApplicationTestConfig;
import com.example.demo.model.ToDoEntity;
import com.example.demo.repository.ToDoRepository;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = DemoApplicationTestConfig.class)
@AutoConfigureMockMvc
@ActiveProfiles(profiles = "test")
class ToDoControllerWithServiceAndRepositoryTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ToDoRepository toDoRepository;

    private final long testId = 1L;
    private final String testText = "My to do text";;

    @BeforeEach
    void init() {
        toDoRepository.save(new ToDoEntity(testId, testText));
    }

    @Test
    void whenGetAll_thenReturnValidResponse() throws Exception {
        this.mockMvc
                .perform(get("/todos"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].text").value(testText))
                .andExpect(jsonPath("$[0].id").value(testId))
                .andExpect(jsonPath("$[0].completedAt").doesNotExist());
    }
}
