package com.example.demo.repository;

import com.example.demo.entity.SpecProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpecProjectRepository extends JpaRepository<SpecProject, Long> {
    
    List<SpecProject> findByUserSpecUserIdOrderByDisplayOrderAsc(Long userId);
    
    List<SpecProject> findByUserSpecUserIdOrderByStartDateDesc(Long userId);
    
    void deleteByUserSpecUserIdAndIdIn(Long userId, List<Long> ids);
    
    void deleteByUserSpecUserId(Long userId);
    
    @Query("SELECT COUNT(sp) FROM SpecProject sp WHERE sp.userSpec.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);
    
    @Query("SELECT sp FROM SpecProject sp WHERE sp.userSpec.user.id = :userId AND sp.technologies LIKE %:technology%")
    List<SpecProject> findByUserIdAndTechnology(@Param("userId") Long userId, @Param("technology") String technology);
    
    @Query("SELECT CASE WHEN COUNT(sp) > 0 THEN true ELSE false END FROM SpecProject sp WHERE sp.userSpec.user.id = :userId")
    boolean existsByUserId(@Param("userId") Long userId);
}
