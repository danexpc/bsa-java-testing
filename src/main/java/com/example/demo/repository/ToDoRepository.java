package com.example.demo.repository;

import com.example.demo.model.ToDoEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ToDoRepository extends JpaRepository<ToDoEntity, Long>, JpaSpecificationExecutor<ToDoEntity> {

}