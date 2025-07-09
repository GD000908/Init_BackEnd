package com.example.demo.repository;

import com.example.demo.entity.UserSpec;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserSpecRepository extends JpaRepository<UserSpec, Long> {
    
    Optional<UserSpec> findByUserId(Long userId);
    
    @Query("SELECT us FROM UserSpec us JOIN FETCH us.user WHERE us.user.id = :userId")
    Optional<UserSpec> findByUserIdWithUser(@Param("userId") Long userId);
    
    boolean existsByUserId(Long userId);
    
    void deleteByUserId(Long userId);
}
