package com.example.demo.entity;

import com.example.demo.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "user_certificates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCertificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    private String organization;

    @Column(name = "acquisition_date")
    private LocalDate acquisitionDate;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Column(name = "certificate_number")
    private String certificateNumber;

    @Column(name = "display_order")
    private Integer displayOrder;
}
