package com.example.f_space.service;

import com.example.f_space.model.Schedule;

import java.util.List;
import java.util.Optional;

public interface ScheduleService {
    Optional<Schedule> findById(Long scheduleId);
    List<Schedule> findByUserId(Long userId);
    List<Schedule> findByMedicationId(Long medicationId);
}
