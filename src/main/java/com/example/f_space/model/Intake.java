package com.example.f_space.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;

@Entity
@Table(name = "intakes")
@Data
public class Intake {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    private Schedule schedule;

    private String status;

    @Version
    private int version;

    @Column(name = "scheduled_for")
    private Timestamp scheduledFor;

    @Column(name = "taken_at")
    private Timestamp takenAt;

}
