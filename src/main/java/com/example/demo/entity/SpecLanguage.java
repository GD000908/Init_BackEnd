package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "spec_languages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpecLanguage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_spec_id", nullable = false)
    private UserSpec userSpec;
    
    @Column(nullable = false, length = 50)
    private String name;
    
    @Column(nullable = false, length = 20)
    private String level;
    
    @Column(name = "test_name", length = 100)
    private String testName;
    
    @Column(length = 50)
    private String score;
    
    @Column(name = "acquisition_date")
    private LocalDate acquisitionDate;
    
    @Column(name = "display_order")
    private Integer displayOrder = 0;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
