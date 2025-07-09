package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "spec_certificates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpecCertificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_spec_id", nullable = false)
    private UserSpec userSpec;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 200) // nullable = false 제거
    private String organization1;

    @Column(name = "acquisition_date") // nullable = false 제거 (날짜 입력이 선택사항이 될 수도 있으므로)
    private LocalDate acquisitionDate;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Column(name = "certificate_number", length = 100)
    private String certificateNumber;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}