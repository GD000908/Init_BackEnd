package com.example.demo.repository;

import com.example.demo.entity.SpecMilitary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpecMilitaryRepository extends JpaRepository<SpecMilitary, Long> {
    
    List<SpecMilitary> findByUserSpecUserIdOrderByStartDateDesc(Long userId);
    
    void deleteByUserSpecUserIdAndIdIn(Long userId, List<Long> ids);
    
    void deleteByUserSpecUserId(Long userId);
    
    @Query("SELECT COUNT(sm) FROM SpecMilitary sm WHERE sm.userSpec.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);
    
    @Query("SELECT sm FROM SpecMilitary sm WHERE sm.userSpec.user.id = :userId AND sm.serviceType = :serviceType")
    List<SpecMilitary> findByUserIdAndServiceType(@Param("userId") Long userId, @Param("serviceType") String serviceType);
}
