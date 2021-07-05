package com.example.demo.controller;

import com.example.demo.config.DemoApplicationTestConfig;
import com.example.demo.model.ToDoEntity;
import com.example.demo.repository.ToDoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = DemoApplicationTestConfig.class)
@AutoConfigureMockMvc
@ActiveProfiles(profiles = "test")
class ToDoControllerWithServiceAndRepositoryTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ToDoRepository toDoRepository;

    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        toDoRepository.deleteAll();
    }

    @Test
    void whenGetAll_thenReturnValidResponse() throws Exception {
        String testText = "My to do text 1";
        toDoRepository.save(new ToDoEntity(1L, testText));

        this.mockMvc
                .perform(get("/todos"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].text").value(testText))
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[0].completedAt").doesNotExist());
    }

    @Test
    void whenGetAllCompleted_thenReturnValidResponse() throws Exception {
        String testTextForCompleted = "My to do text for completed";
        String testTextForInProgress = "My to do text for in progress";
        ZonedDateTime completeTime = ZonedDateTime.now(ZoneOffset.UTC);
        toDoRepository.save(new ToDoEntity(1L, testTextForCompleted, completeTime));
        toDoRepository.save(new ToDoEntity(2L, testTextForInProgress));

        this.mockMvc
                .perform(get("/todos?isCompleted=true"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].text").value(testTextForCompleted))
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[0].completedAt").exists());
    }

    @Test
    void whenGetAllInProgress_thenReturnValidResponse() throws Exception {
        String testTextForCompleted = "My to do text for completed";
        String testTextForInProgress = "My to do text for in progress";
        ZonedDateTime completeTime = ZonedDateTime.now(ZoneOffset.UTC);
        toDoRepository.save(new ToDoEntity(1L, testTextForCompleted, completeTime));
        toDoRepository.save(new ToDoEntity(2L, testTextForInProgress));

        this.mockMvc
                .perform(get("/todos?isCompleted=false"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].text").value(testTextForInProgress))
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[0].completedAt").doesNotExist());
    }

    @Test
    void whenIdDoesntExist_thenReturnNotFoundStatus() throws Exception {
        long id = 1L;
        String testText = "My to do text";

        this.mockMvc
                .perform(get("/todos" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void whenSaveToDo_thenFindToDoByItsId() throws Exception {
        long id = 1L;
        String testText = "My to do text for saving request";

        ToDoEntity todo = new ToDoEntity(id, testText);

        this.mockMvc
                .perform(post("/todos")
                        .content(mapper.writeValueAsString(todo))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value(testText))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.completedAt").doesNotExist());

        assertThat(toDoRepository.findById(id).orElseThrow()).isEqualToComparingFieldByField(todo);
    }
}
