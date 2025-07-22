package com.example.demo.repository;

import com.example.demo.entity.SpecLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpecLinkRepository extends JpaRepository<SpecLink, Long> {
    
    List<SpecLink> findByUserSpecUserIdOrderByDisplayOrderAsc(Long userId);
    
    List<SpecLink> findByUserSpecUserIdAndTypeOrderByDisplayOrderAsc(Long userId, String type);
    
    @Query("SELECT DISTINCT sl.type FROM SpecLink sl WHERE sl.userSpec.user.id = :userId")
    List<String> findDistinctTypesByUserId(@Param("userId") Long userId);
    
    void deleteByUserSpecUserIdAndIdIn(Long userId, List<Long> ids);
    
    void deleteByUserSpecUserId(Long userId);
    
    @Query("SELECT COUNT(sl) FROM SpecLink sl WHERE sl.userSpec.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);
    
    @Query("SELECT CASE WHEN COUNT(sl) > 0 THEN true ELSE false END FROM SpecLink sl WHERE sl.userSpec.user.id = :userId")
    boolean existsByUserId(@Param("userId") Long userId);
}
