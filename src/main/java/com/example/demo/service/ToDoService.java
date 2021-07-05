package com.example.demo.service;

import com.example.demo.dto.ToDoResponse;
import com.example.demo.dto.ToDoSaveRequest;
import com.example.demo.dto.mapper.ToDoEntityToResponseMapper;
import com.example.demo.exception.ToDoNotFoundException;
import com.example.demo.model.ToDoEntity;
import com.example.demo.repository.ToDoRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.example.demo.repository.specification.ToDoSpecifications.isCompleted;
import static com.example.demo.repository.specification.ToDoSpecifications.isInProgress;
import static org.springframework.data.jpa.domain.Specification.where;

@Service
public class ToDoService {

    private final ToDoRepository toDoRepository;

    public ToDoService(ToDoRepository toDoRepository) {
        this.toDoRepository = toDoRepository;
    }

    public List<ToDoResponse> getAll() {
        return toDoRepository.findAll().stream()
                .map(ToDoEntityToResponseMapper::map)
                .collect(Collectors.toList());
    }

    public List<ToDoResponse> getAllCompleted() {
        return getAllByCriteria(where(isCompleted())).stream()
                .map(ToDoEntityToResponseMapper::map)
                .collect(Collectors.toList());
    }

    public List<ToDoResponse> getAllInProgress() {
        return getAllByCriteria(where(isInProgress())).stream()
                .map(ToDoEntityToResponseMapper::map)
                .collect(Collectors.toList());
    }

    private List<ToDoEntity> getAllByCriteria(Specification<ToDoEntity> specification) {
        return toDoRepository.findAll(specification);
    }

    public ToDoResponse upsert(ToDoSaveRequest toDoDTO) throws ToDoNotFoundException {
        ToDoEntity todo;
        //update if it has id or create if it hasn't
        if (toDoDTO.id == null) {
            todo = new ToDoEntity(toDoDTO.text);
        } else {
            todo = toDoRepository.findById(toDoDTO.id).orElseThrow(() -> new ToDoNotFoundException(toDoDTO.id));
            todo.setText(toDoDTO.text);
        }
        return ToDoEntityToResponseMapper.map(toDoRepository.save(todo));
    }

    public ToDoResponse completeToDo(Long id) throws ToDoNotFoundException {
        ToDoEntity todo = toDoRepository.findById(id).orElseThrow(() -> new ToDoNotFoundException(id));
        todo.completeNow();
        return ToDoEntityToResponseMapper.map(toDoRepository.save(todo));
    }

    public ToDoResponse cancelToDo(Long id) throws ToDoNotFoundException {
        ToDoEntity todo = toDoRepository.findById(id).orElseThrow(() -> new ToDoNotFoundException(id));
        todo.cancelNow();
        return ToDoEntityToResponseMapper.map(toDoRepository.save(todo));
    }

    public ToDoResponse getOne(Long id) throws ToDoNotFoundException {
        return ToDoEntityToResponseMapper.map(
                toDoRepository.findById(id).orElseThrow(() -> new ToDoNotFoundException(id))
        );
    }

    public void deleteOne(Long id) {
        toDoRepository.deleteById(id);
    }

    public void deleteAll() {
        toDoRepository.deleteAll();
    }
}
