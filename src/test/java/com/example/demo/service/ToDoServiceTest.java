package com.example.demo.service;

import com.example.demo.dto.ToDoSaveRequest;
import com.example.demo.dto.mapper.ToDoEntityToResponseMapper;
import com.example.demo.exception.ToDoNotFoundException;
import com.example.demo.model.ToDoEntity;
import com.example.demo.repository.ToDoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.data.jpa.domain.Specification.where;

class ToDoServiceTest {

    private ToDoRepository toDoRepository;

    private ToDoService toDoService;

    //executes before each test defined below
    @BeforeEach
    void setUp() {
        this.toDoRepository = mock(ToDoRepository.class);
        toDoService = new ToDoService(toDoRepository);
    }

    @Test
    void whenGetAll_thenReturnAll() {
        //mock
        var testToDos = new ArrayList<ToDoEntity>();
        testToDos.add(new ToDoEntity(0L, "Test 1"));
        var toDo = new ToDoEntity(1L, "Test 2");
        toDo.completeNow();
        testToDos.add(toDo);
        when(toDoRepository.findAll()).thenReturn(testToDos);

        //call
        var todos = toDoService.getAll();

        //validate
        assertEquals(todos.size(), testToDos.size());
        for (int i = 0; i < todos.size(); i++) {
            assertThat(todos.get(i), samePropertyValuesAs(
                    ToDoEntityToResponseMapper.map(testToDos.get(i))
            ));
        }
    }

    @Test
    void whenGetAllCompleted_thenReturnAllWhereCompletedAtNotNull() {
        //mock
        var testToDos = new ArrayList<ToDoEntity>();
        testToDos.add(new ToDoEntity(0L, "Test 1"));
        var toDo1 = new ToDoEntity(1L, "Test 2");
        var toDo2 = new ToDoEntity(2L, "Test 3");
        toDo1.completeNow();
        toDo2.completeNow();
        var completedTodos = List.of(toDo1, toDo2);
        testToDos = new ArrayList<>(completedTodos);
        when(toDoRepository.findAll(where(any())))
                .thenReturn(testToDos
                        .stream()
                        .filter(todo -> todo.getCompletedAt() != null)
                        .collect(Collectors.toList()));

        //call
        var todos = toDoService.getAllCompleted();

        //validate
        assertEquals(todos.size(), completedTodos.size());
        for (int i = 0; i < todos.size(); i++) {
            assertThat(todos.get(i), samePropertyValuesAs(
                    ToDoEntityToResponseMapper.map(completedTodos.get(i))
            ));
        }
    }

    @Test
    void whenGetAllInProgress_thenReturnAllWhereCompletedAtNull() {
        //mock
        var testToDos = new ArrayList<ToDoEntity>();
        testToDos.add(new ToDoEntity(0L, "Test 1", ZonedDateTime.now(ZoneOffset.UTC)));
        var toDo1 = new ToDoEntity(1L, "Test 2");
        var toDo2 = new ToDoEntity(2L, "Test 3");
        var inProgressTodos = List.of(toDo1, toDo2);
        testToDos = new ArrayList<>(inProgressTodos);
        when(toDoRepository.findAll(where(any())))
                .thenReturn(testToDos
                        .stream()
                        .filter(todo -> todo.getCompletedAt() == null)
                        .collect(Collectors.toList()));

        //call
        var todos = toDoService.getAllInProgress();

        //validate
        assertEquals(todos.size(), inProgressTodos.size());
        for (int i = 0; i < todos.size(); i++) {
            assertThat(todos.get(i), samePropertyValuesAs(
                    ToDoEntityToResponseMapper.map(inProgressTodos.get(i))
            ));
        }
    }

    @Test
    void whenUpsertWithId_thenReturnUpdated() throws ToDoNotFoundException {
        //mock
        var expectedToDo = new ToDoEntity(0L, "New Item");
        when(toDoRepository.findById(anyLong())).thenAnswer(i -> {
            Long id = i.getArgument(0, Long.class);
            if (id.equals(expectedToDo.getId())) {
                return Optional.of(expectedToDo);
            } else {
                return Optional.empty();
            }
        });
        when(toDoRepository.save(ArgumentMatchers.any(ToDoEntity.class))).thenAnswer(i -> {
            ToDoEntity arg = i.getArgument(0, ToDoEntity.class);
            Long id = arg.getId();
            if (id != null) {
                if (!id.equals(expectedToDo.getId()))
                    return new ToDoEntity(id, arg.getText());
                expectedToDo.setText(arg.getText());
                return expectedToDo; //return valid result only if we get valid id
            } else {
                return new ToDoEntity(40158L, arg.getText());
            }
        });

        //call
        var toDoSaveRequest = new ToDoSaveRequest();
        toDoSaveRequest.id = expectedToDo.getId();
        toDoSaveRequest.text = "Updated Item";
        var todo = toDoService.upsert(toDoSaveRequest);

        //validate
        assertEquals(todo.id, toDoSaveRequest.id);
        assertEquals(todo.text, toDoSaveRequest.text);
    }

    @Test
    void whenUpsertNoId_thenReturnNew() throws ToDoNotFoundException {
        //mock
        var newId = 0L;
        when(toDoRepository.findById(anyLong())).thenAnswer(i -> {
            Long id = i.getArgument(0, Long.class);
            if (id == newId) {
                return Optional.empty();
            } else {
                return Optional.of(new ToDoEntity(newId, "Wrong ToDo"));
            }
        });
        when(toDoRepository.save(ArgumentMatchers.any(ToDoEntity.class))).thenAnswer(i -> {
            ToDoEntity arg = i.getArgument(0, ToDoEntity.class);
            Long id = arg.getId();
            if (id == null)
                return new ToDoEntity(newId, arg.getText());
            else
                return new ToDoEntity();
        });

        //call
        var toDoDto = new ToDoSaveRequest();
        toDoDto.text = "Created Item";
        var result = toDoService.upsert(toDoDto);

        //validate
        assertEquals((long) result.id, newId);
        assertEquals(result.text, toDoDto.text);
    }

    @Test
    void whenComplete_thenReturnWithCompletedAt() throws ToDoNotFoundException {
        var startTime = ZonedDateTime.now(ZoneOffset.UTC);
        //mock
        var todo = new ToDoEntity(0L, "Test 1");
        when(toDoRepository.findById(anyLong())).thenReturn(Optional.of(todo));
        when(toDoRepository.save(ArgumentMatchers.any(ToDoEntity.class))).thenAnswer(i -> {
            ToDoEntity arg = i.getArgument(0, ToDoEntity.class);
            Long id = arg.getId();
            if (id.equals(todo.getId())) {
                return todo;
            } else {
                return new ToDoEntity();
            }
        });

        //call
        var result = toDoService.completeToDo(todo.getId());

        //validate
        assertEquals(result.id, todo.getId());
        assertEquals(result.text, todo.getText());
        assertTrue(result.completedAt.isAfter(startTime));
    }

    @Test
    void whenCancel_thenReturnWithEmptyCompletedAt() throws ToDoNotFoundException {
        var startTime = ZonedDateTime.now(ZoneOffset.UTC);
        //mock
        var todo = new ToDoEntity(0L, "Test 1", ZonedDateTime.now(ZoneOffset.UTC));
        when(toDoRepository.findById(anyLong())).thenReturn(Optional.of(todo));
        when(toDoRepository.save(ArgumentMatchers.any(ToDoEntity.class))).thenAnswer(i -> {
            ToDoEntity arg = i.getArgument(0, ToDoEntity.class);
            Long id = arg.getId();
            if (id.equals(todo.getId())) {
                return todo;
            } else {
                return new ToDoEntity();
            }
        });

        //call
        var result = toDoService.cancelToDo(todo.getId());

        //validate
        assertEquals(result.id, todo.getId());
        assertEquals(result.text, todo.getText());
        assertNull(result.completedAt);
    }

    @Test
    void whenCancelByIdThatDoesntExist_thenThrowToDoNotFoundException() {
        assertThrows(ToDoNotFoundException.class, () -> toDoService.cancelToDo(1L));
    }

    @Test
    void whenGetOne_thenReturnCorrectOne() throws ToDoNotFoundException {
        //mock
        var todo = new ToDoEntity(0L, "Test 1");
        when(toDoRepository.findById(anyLong())).thenReturn(Optional.of(todo));

        //call
        var result = toDoService.getOne(0L);

        //validate
        assertThat(result, samePropertyValuesAs(
                ToDoEntityToResponseMapper.map(todo)
        ));
    }

    @Test
    void whenDeleteOne_thenRepositoryDeleteCalled() {
        //call
        var id = 0L;
        toDoService.deleteOne(id);

        //validate
        verify(toDoRepository, times(1)).deleteById(id);
    }

    @Test
    void whenIdNotFound_thenThrowNotFoundException() {
        assertThrows(ToDoNotFoundException.class, () -> toDoService.getOne(1L));
    }

    @Test
    void whenDeleteAll_thenRepositoryDeleteAllCalled() {
        //call
        toDoService.deleteAll();

        //validate
        verify(toDoRepository, times(1)).deleteAll();
    }

}
