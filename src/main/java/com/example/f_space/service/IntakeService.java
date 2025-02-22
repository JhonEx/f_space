package com.example.f_space.service;

import com.example.f_space.model.Intake;

import java.util.List;

public interface IntakeService {
    Intake recordIntake(Intake intake);
    List<Intake> getIntakesBySchedule(Long scheduleId);
    List<Intake> getIntakesByUser(Long userId);
}