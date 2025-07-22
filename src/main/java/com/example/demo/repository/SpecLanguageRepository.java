package com.example.demo.repository;

import com.example.demo.entity.SpecLanguage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpecLanguageRepository extends JpaRepository<SpecLanguage, Long> {
    
    List<SpecLanguage> findByUserSpecUserIdOrderByDisplayOrderAsc(Long userId);
    
    void deleteByUserSpecUserIdAndIdIn(Long userId, List<Long> ids);
    
    void deleteByUserSpecUserId(Long userId);
    
    @Query("SELECT COUNT(sl) FROM SpecLanguage sl WHERE sl.userSpec.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);
    
    @Query("SELECT sl FROM SpecLanguage sl WHERE sl.userSpec.user.id = :userId AND sl.name = :languageName")
    List<SpecLanguage> findByUserIdAndLanguageName(@Param("userId") Long userId, @Param("languageName") String languageName);
    
    @Query("SELECT CASE WHEN COUNT(sl) > 0 THEN true ELSE false END FROM SpecLanguage sl WHERE sl.userSpec.user.id = :userId")
    boolean existsByUserId(@Param("userId") Long userId);
}
