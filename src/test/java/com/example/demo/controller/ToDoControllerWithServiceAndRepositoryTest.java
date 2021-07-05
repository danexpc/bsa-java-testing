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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = DemoApplicationTestConfig.class)
@AutoConfigureMockMvc
@ActiveProfiles(profiles = "test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
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

        // given
        String testText = "My to do text 1";
        var todo = new ToDoEntity(1L, testText);
        toDoRepository.save(todo);

        // when
        // then
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

        // given
        String testTextForCompleted = "My to do text for completed";
        String testTextForInProgress = "My to do text for in progress";
        ZonedDateTime completeTime = ZonedDateTime.now(ZoneOffset.UTC);

        toDoRepository.save(new ToDoEntity(1L, testTextForCompleted, completeTime));
        toDoRepository.save(new ToDoEntity(2L, testTextForInProgress));

        // when
        // then
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

        // given
        String testTextForCompleted = "My to do text for completed";
        String testTextForInProgress = "My to do text for in progress";
        ZonedDateTime completeTime = ZonedDateTime.now(ZoneOffset.UTC);

        toDoRepository.save(new ToDoEntity(1L, testTextForCompleted, completeTime));
        toDoRepository.save(new ToDoEntity(2L, testTextForInProgress));


        // when
        // then
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
    void whenIdExist_thenReturnToDoWithItsId() throws Exception {

        // given
        long id = 1L;
        String testText = "My to do text";
        ToDoEntity todo = new ToDoEntity(id, testText);

        toDoRepository.save(todo);

        // when
        // then
        this.mockMvc
                .perform(get("/todos/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value(testText))
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.completedAt").doesNotExist());
    }

    @Test
    void whenIdDoesntExist_thenReturnNotFoundStatus() throws Exception {

        // given
        long id = 1L;

        // when
        // then
        this.mockMvc
                .perform(get("/todos/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void whenCompleteToDo_thenSetCompleteAt() throws Exception {

        // given
        long id = 1L;
        String testText = "My to do text";
        ToDoEntity todo = new ToDoEntity(id, testText);

        toDoRepository.save(todo);

        // when
        this.mockMvc
                .perform(put("/todos/" + id + "/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.text").value(testText))
                .andExpect(jsonPath("$.completedAt").exists());

        // then
        assertThat(toDoRepository.findById(id).orElseThrow().getCompletedAt()).isNotNull();
    }

    @Test
    void whenCancelToDo_thenSetCompleteAtAsNull() throws Exception {

        // given
        long id = 1L;
        String testText = "My to do text";
        ToDoEntity todo = new ToDoEntity(id, testText, ZonedDateTime.now(ZoneOffset.UTC));

        toDoRepository.save(todo);

        // when
        this.mockMvc
                .perform(put("/todos/" + id + "/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.text").value(testText))
                .andExpect(jsonPath("$.completedAt").doesNotExist());

        // then
        assertThat(toDoRepository.findById(id).orElseThrow().getCompletedAt()).isNull();
    }

    @Test
    void whenSaveToDo_thenFindToDoByItsId() throws Exception {

        // given
        long id = 1L;
        String testText = "My to do text for saving request";
        ToDoEntity todo = new ToDoEntity(id, testText);

        // when
        this.mockMvc
                .perform(post("/todos")
                        .content(mapper.writeValueAsString(todo))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value(testText))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.completedAt").doesNotExist());

        // then
        assertThat(toDoRepository.findById(id).orElseThrow()).isEqualToComparingFieldByField(todo);
    }

    @Test
    void whenDeleteToDoById_thenFindToDoByItsIdReturnsEmptyOptional() throws Exception {

        // given
        long id = 1L;
        String testText = "My to do text for saving request";
        ToDoEntity todo = new ToDoEntity(id, testText);

        toDoRepository.save(todo);

        // when
        this.mockMvc
                .perform(delete("/todos/" + id))
                .andExpect(status().isNoContent());

        // then
        assertThat(toDoRepository.findById(id)).isNotPresent();
    }

    @Test
    void whenDeleteAllToDo_thenFindAllToDoReturnsEmptyList() throws Exception {

        // given
        String testTextForCompleted = "My to do text for completed";
        String testTextForInProgress = "My to do text for in progress";
        ZonedDateTime completeTime = ZonedDateTime.now(ZoneOffset.UTC);

        toDoRepository.save(new ToDoEntity(1L, testTextForCompleted, completeTime));
        toDoRepository.save(new ToDoEntity(2L, testTextForInProgress));

        // when
        this.mockMvc
                .perform(delete("/todos"))
                .andExpect(status().isNoContent());

        // then
        assertThat(toDoRepository.findAll().isEmpty()).isTrue();
    }
}
