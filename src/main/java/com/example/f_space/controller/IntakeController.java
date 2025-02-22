package com.example.f_space.controller;


import com.example.f_space.controller.exceptionhandler.GlobalExceptionHandler;
import com.example.f_space.model.Intake;
import com.example.f_space.model.Schedule;
import com.example.f_space.repository.ScheduleRepository;
import com.example.f_space.service.IntakeService;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/intakes")
public class IntakeController {

    @Autowired
    private IntakeService intakeService;
    @Autowired
    private ScheduleRepository scheduleRepository;

    @GetMapping("/test")
    public ResponseEntity<String> testController() {
        return ResponseEntity.ok("Controller is working!");
    }

    @PostMapping
    public ResponseEntity<String> recordIntake(@RequestBody IntakeRequest intakeRequest) {
        Schedule schedule = scheduleRepository.findById(intakeRequest.getScheduleId())
                .orElseThrow(() -> new GlobalExceptionHandler.ResourceNotFoundException("Schedule not found with ID: " + intakeRequest.getScheduleId()));

        if ("TAKEN".equalsIgnoreCase(intakeRequest.getStatus()) && intakeRequest.getTakenAt() == null) {
            throw new GlobalExceptionHandler.ResourceNotFoundException("'taken_at' timestamp is required when status is TAKEN");
        }

        Intake intake = new Intake();
        intake.setSchedule(schedule);
        intake.setStatus(intakeRequest.getStatus());
        if (intakeRequest.getTakenAt() != null) {
            intake.setTakenAt(Timestamp.from(intakeRequest.getTakenAt().toInstant()));  // Convert ZonedDateTime to UTC Timestamp
        }
        intake.setScheduledFor(Timestamp.from(ZonedDateTime.now(ZoneOffset.UTC).toInstant()));  // Store current time in UTC
        intakeService.recordIntake(intake);

        return ResponseEntity.status(HttpStatus.CREATED).body("Intake recorded successfully");
    }


    @GetMapping("/schedule/{scheduleId}")
    public ResponseEntity<List<Intake>> getIntakesBySchedule(@PathVariable Long scheduleId) {
        List<Intake> intakes = intakeService.getIntakesBySchedule(scheduleId);
        if (intakes.isEmpty()) {
            throw new GlobalExceptionHandler.ResourceNotFoundException("No intakes found for schedule ID: " + scheduleId);
        }
        return ResponseEntity.ok(intakes);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Intake>> getIntakesByUser(@PathVariable Long userId) {
        List<Intake> intakes = intakeService.getIntakesByUser(userId);
        if (intakes.isEmpty()) {
            throw new GlobalExceptionHandler.ResourceNotFoundException("No intakes found for user ID: " + userId);
        }
        return ResponseEntity.ok(intakes);
    }


    @Data
    public static class IntakeRequest {
        private Long scheduleId;
        private String status;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
        private ZonedDateTime takenAt;  // Supports timezone-aware timestamps
    }

}
