package com.example.demo.repository;

import com.example.demo.entity.TodoItem;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TodoItemRepository extends JpaRepository<TodoItem, Long> {
    List<TodoItem> findByUser(User user);
    List<TodoItem> findByUserId(Long userId);
    List<TodoItem> findByUserIdOrderByIdDesc(Long userId);
}
