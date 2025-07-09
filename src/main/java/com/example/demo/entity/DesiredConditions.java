package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Entity
@Table(name = "desired_conditions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DesiredConditions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ElementCollection
    @CollectionTable(name = "desired_jobs", joinColumns = @JoinColumn(name = "condition_id"))
    @Column(name = "job")
    private List<String> jobs;
    
    @ElementCollection
    @CollectionTable(name = "desired_locations", joinColumns = @JoinColumn(name = "condition_id"))
    @Column(name = "location")
    private List<String> locations;
    
    @Column(nullable = false)
    private String salary;
    
    @ElementCollection
    @CollectionTable(name = "desired_others", joinColumns = @JoinColumn(name = "condition_id"))
    @Column(name = "other_condition")
    private List<String> others;
}
