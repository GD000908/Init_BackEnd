package com.example.demo.repository;

import com.example.demo.entity.SpecSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpecSkillRepository extends JpaRepository<SpecSkill, Long> {
    
    List<SpecSkill> findByUserSpecUserIdOrderByDisplayOrderAsc(Long userId);
    
    List<SpecSkill> findByUserSpecUserIdAndCategoryOrderByDisplayOrderAsc(Long userId, String category);
    
    @Query("SELECT DISTINCT ss.category FROM SpecSkill ss WHERE ss.userSpec.user.id = :userId")
    List<String> findDistinctCategoriesByUserId(@Param("userId") Long userId);
    
    void deleteByUserSpecUserIdAndIdIn(Long userId, List<Long> ids);
    
    void deleteByUserSpecUserId(Long userId);
    
    @Query("SELECT ss.name FROM SpecSkill ss WHERE ss.userSpec.user.id = :userId ORDER BY ss.displayOrder ASC")
    List<String> findSkillNamesByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(ss) FROM SpecSkill ss WHERE ss.userSpec.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);
    
    @Query("SELECT CASE WHEN COUNT(ss) > 0 THEN true ELSE false END FROM SpecSkill ss WHERE ss.userSpec.user.id = :userId")
    boolean existsByUserId(@Param("userId") Long userId);
}
