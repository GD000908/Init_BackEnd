package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "spec_militaries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpecMilitary {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_spec_id", nullable = false)
    private UserSpec userSpec;
    
    @Column(name = "service_type", nullable = false, length = 50)
    private String serviceType;
    
    @Column(name = "military_branch", length = 50)
    private String militaryBranch;
    
    @Column(name = "military_rank", length = 50)
    private String rank;
    
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;
    
    @Column(name = "exemption_reason", length = 200)
    private String exemptionReason;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
