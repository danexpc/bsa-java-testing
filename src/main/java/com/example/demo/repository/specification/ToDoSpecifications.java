package com.example.demo.repository.specification;

import com.example.demo.model.ToDoEntity;
import org.springframework.data.jpa.domain.Specification;

public class ToDoSpecifications {
    public static Specification<ToDoEntity> isCompleted() {
        return (root, query, cb) -> root.get("completedAt").isNotNull();
    }

    public static Specification<ToDoEntity> isInProgress() {
        return (root, query, cb) -> root.get("completedAt").isNull();
    }
}
