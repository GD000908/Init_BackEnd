package com.example.demo.repository;

import com.example.demo.entity.CoverLetter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CoverLetterRepository extends JpaRepository<CoverLetter, Long> {
    List<CoverLetter> findByUserId(Long userId);
    int countByUserId(Long userId);
}
