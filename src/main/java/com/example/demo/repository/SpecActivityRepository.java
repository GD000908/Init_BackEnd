package com.example.demo.repository;

import com.example.demo.entity.SpecActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpecActivityRepository extends JpaRepository<SpecActivity, Long> {
    
    List<SpecActivity> findByUserSpecUserIdOrderByDisplayOrderAsc(Long userId);
    
    List<SpecActivity> findByUserSpecUserIdOrderByStartDateDesc(Long userId);
    
    void deleteByUserSpecUserIdAndIdIn(Long userId, List<Long> ids);
    
    void deleteByUserSpecUserId(Long userId);
    
    @Query("SELECT COUNT(sa) FROM SpecActivity sa WHERE sa.userSpec.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);
    
    @Query("SELECT sa FROM SpecActivity sa WHERE sa.userSpec.user.id = :userId AND sa.organization = :organization")
    List<SpecActivity> findByUserIdAndOrganization(@Param("userId") Long userId, @Param("organization") String organization);
}
