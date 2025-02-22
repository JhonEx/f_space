package com.example.f_space.service;

import com.example.f_space.model.Schedule;
import com.example.f_space.repository.ScheduleRepository;
import lombok.AllArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {


    private ScheduleRepository scheduleRepository;

    @Override
    public Optional<Schedule> findById(Long scheduleId) {
        return scheduleRepository.findById(scheduleId);
    }

    @Override
    public List<Schedule> findByUserId(Long userId) {
        return scheduleRepository.findByUserId(userId);
    }

    @Override
    public List<Schedule> findByMedicationId(Long medicationId) {
        return scheduleRepository.findByMedicationId(medicationId);
    }
}