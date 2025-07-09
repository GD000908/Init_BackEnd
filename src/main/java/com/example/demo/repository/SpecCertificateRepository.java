package com.example.demo.repository;

import com.example.demo.entity.SpecCertificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpecCertificateRepository extends JpaRepository<SpecCertificate, Long> {
    
    List<SpecCertificate> findByUserSpecUserIdOrderByDisplayOrderAsc(Long userId);
    
    List<SpecCertificate> findByUserSpecUserIdOrderByAcquisitionDateDesc(Long userId);
    
    void deleteByUserSpecUserIdAndIdIn(Long userId, List<Long> ids);
    
    void deleteByUserSpecUserId(Long userId);
    
    @Query("SELECT COUNT(sc) FROM SpecCertificate sc WHERE sc.userSpec.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);
    
    @Query("SELECT sc FROM SpecCertificate sc WHERE sc.userSpec.user.id = :userId AND sc.organization1 = :organization")
    List<SpecCertificate> findByUserIdAndOrganization(@Param("userId") Long userId, @Param("organization") String organization);
    
    @Query("SELECT CASE WHEN COUNT(sc) > 0 THEN true ELSE false END FROM SpecCertificate sc WHERE sc.userSpec.user.id = :userId")
    boolean existsByUserId(@Param("userId") Long userId);
}
