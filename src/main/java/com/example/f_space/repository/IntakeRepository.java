package com.example.f_space.repository;

import com.example.f_space.model.Intake;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IntakeRepository extends JpaRepository<Intake, Long> {
    List<Intake> findByScheduleId(Long scheduleId);
    List<Intake> findByStatus(String status);
}
