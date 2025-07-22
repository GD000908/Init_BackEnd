package com.example.demo.repository;

import com.example.demo.entity.SpecCareer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpecCareerRepository extends JpaRepository<SpecCareer, Long> {
    
    List<SpecCareer> findByUserSpecUserIdOrderByDisplayOrderAsc(Long userId);
    
    List<SpecCareer> findByUserSpecUserIdOrderByStartDateDesc(Long userId);
    
    @Query("SELECT sc FROM SpecCareer sc WHERE sc.userSpec.user.id = :userId AND sc.isCurrent = true")
    List<SpecCareer> findCurrentCareersByUserId(@Param("userId") Long userId);
    
    void deleteByUserSpecUserIdAndIdIn(Long userId, List<Long> ids);
    
    void deleteByUserSpecUserId(Long userId);
    
    @Query("SELECT COUNT(sc) FROM SpecCareer sc WHERE sc.userSpec.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);
    
    @Query("SELECT CASE WHEN COUNT(sc) > 0 THEN true ELSE false END FROM SpecCareer sc WHERE sc.userSpec.user.id = :userId")
    boolean existsByUserId(@Param("userId") Long userId);
}
