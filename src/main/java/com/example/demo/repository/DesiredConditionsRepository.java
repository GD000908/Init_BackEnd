package com.example.demo.repository;

import com.example.demo.entity.DesiredConditions;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface DesiredConditionsRepository extends JpaRepository<DesiredConditions, Long> {
    Optional<DesiredConditions> findByUser(User user);
    Optional<DesiredConditions> findByUserId(Long userId);
}
