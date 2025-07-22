package com.example.demo.repository;

import com.example.demo.entity.SpecEducation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpecEducationRepository extends JpaRepository<SpecEducation, Long> {
    
    List<SpecEducation> findByUserSpecUserIdOrderByDisplayOrderAsc(Long userId);
    
    List<SpecEducation> findByUserSpecUserIdOrderByStartDateDesc(Long userId);
    
    @Query("SELECT se FROM SpecEducation se WHERE se.userSpec.user.id = :userId AND se.isCurrent = true")
    List<SpecEducation> findCurrentEducationsByUserId(@Param("userId") Long userId);
    
    void deleteByUserSpecUserIdAndIdIn(Long userId, List<Long> ids);
    
    void deleteByUserSpecUserId(Long userId);
    
    @Query("SELECT COUNT(se) FROM SpecEducation se WHERE se.userSpec.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);
    
    @Query("SELECT CASE WHEN COUNT(se) > 0 THEN true ELSE false END FROM SpecEducation se WHERE se.userSpec.user.id = :userId")
    boolean existsByUserId(@Param("userId") Long userId);
}
