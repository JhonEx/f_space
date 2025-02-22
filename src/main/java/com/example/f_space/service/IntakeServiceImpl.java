package com.example.f_space.service;

import com.example.f_space.controller.exceptionhandler.GlobalExceptionHandler;
import com.example.f_space.model.Intake;
import com.example.f_space.model.Schedule;
import com.example.f_space.repository.IntakeRepository;
import com.example.f_space.repository.ScheduleRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class IntakeServiceImpl implements IntakeService{


    private IntakeRepository intakeRepository;


    private ScheduleRepository scheduleRepository;

    @Override
    @Transactional
    public Intake recordIntake(Intake intake) {
        // Fetch schedule to ensure it exists
        Schedule schedule = scheduleRepository.findById(intake.getSchedule().getId())
                .orElseThrow(() -> new GlobalExceptionHandler.ResourceNotFoundException("Schedule not found"));

        intake.setSchedule(schedule);
        return intakeRepository.save(intake);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Intake> getIntakesBySchedule(Long scheduleId) {
        return intakeRepository.findByScheduleId(scheduleId);
    }

    @Override
    @Transactional(readOnly = true)
    //@Async
    public List<Intake> getIntakesByUser(Long userId) {
        List<Schedule> schedules = scheduleRepository.findByUserId(userId);
        return schedules.stream()
                .flatMap(schedule -> intakeRepository.findByScheduleId(schedule.getId()).stream())
                .collect(Collectors.toList());
    }
}
