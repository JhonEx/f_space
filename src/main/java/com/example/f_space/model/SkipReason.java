package com.example.f_space.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "skip_reasons")
@Data
public class SkipReason {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "intake_id", nullable = false)
    private Intake intake;

    private String reasonType;

    @Column(columnDefinition = "TEXT")
    private String specificReason;

    private String authorizedBy;
}
