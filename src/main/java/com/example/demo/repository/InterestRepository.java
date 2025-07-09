package com.example.demo.repository;

import com.example.demo.entity.Interest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InterestRepository extends JpaRepository<Interest, Long> {
    List<Interest> findByNameIn(List<String> names);
}