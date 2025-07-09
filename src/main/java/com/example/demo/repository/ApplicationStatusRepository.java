package com.example.demo.repository;

import com.example.demo.entity.ApplicationStatus;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ApplicationStatusRepository extends JpaRepository<ApplicationStatus, Long> {
    List<ApplicationStatus> findByUser(User user);
    List<ApplicationStatus> findByUserId(Long userId);
    int countByUserIdAndStatus(Long userId, ApplicationStatus.Status status);
}
