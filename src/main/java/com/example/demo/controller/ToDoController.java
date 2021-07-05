package com.example.demo.controller;

import com.example.demo.dto.ToDoResponse;
import com.example.demo.dto.ToDoSaveRequest;
import com.example.demo.exception.ToDoNotFoundException;
import com.example.demo.service.ToDoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
public class ToDoController {

    @Autowired
    ToDoService toDoService;

    @ExceptionHandler({ToDoNotFoundException.class})
    public ResponseEntity<Object> handleException(Exception ex) {
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/todos")
    @Valid
    public List<ToDoResponse> getAll(@RequestParam(required = false) Boolean isCompleted) {
        if (isCompleted != null) {
            if (isCompleted) {
                return toDoService.getAllCompleted();
            }
            return toDoService.getAllInProgress();
        }
        return toDoService.getAll();
    }

    @PostMapping("/todos")
    @Valid
    public ToDoResponse save(@Valid @RequestBody ToDoSaveRequest todoSaveRequest) throws ToDoNotFoundException {
        return toDoService.upsert(todoSaveRequest);
    }

    @PutMapping("/todos/{id}/complete")
    @Valid
    public ToDoResponse save(@PathVariable Long id) throws ToDoNotFoundException {
        return toDoService.completeToDo(id);
    }

    @PutMapping("/todos/{id}/cancel")
    @Valid
    public ToDoResponse cancel(@PathVariable Long id) throws ToDoNotFoundException {
        return toDoService.cancelToDo(id);
    }

    @GetMapping("/todos/{id}")
    @Valid
    public ToDoResponse getOne(@PathVariable Long id) throws ToDoNotFoundException {
        return toDoService.getOne(id);
    }

    @DeleteMapping("/todos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        toDoService.deleteOne(id);
    }

    @DeleteMapping("/todos")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAll() {
        toDoService.deleteAll();
    }

}