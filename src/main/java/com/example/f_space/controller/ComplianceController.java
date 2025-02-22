package com.example.f_space.controller;

import com.example.f_space.controller.exceptionhandler.GlobalExceptionHandler;
import com.example.f_space.model.Intake;
import com.example.f_space.model.Schedule;
import com.example.f_space.repository.IntakeRepository;
import com.example.f_space.repository.ScheduleRepository;
import com.example.f_space.service.ComplianceMetrics;
import com.example.f_space.service.ComplianceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
@RestController
@RequestMapping("/api/v1/compliance")
public class ComplianceController {

    @Autowired
    private ComplianceService complianceService;

    @GetMapping("/test")
    public ResponseEntity<String> testController() {
        return ResponseEntity.ok("Controller is working!");
    }

    @GetMapping("/rate")
    public ResponseEntity<ComplianceMetrics> getComplianceRate(
            @RequestParam Long userId,
            @RequestParam Long medicationId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (startDate.isAfter(endDate)) {
            throw new GlobalExceptionHandler.ResourceNotFoundException("Start date must be before end date.");
        }

        ComplianceMetrics metrics = complianceService.calculateCompliance(userId, medicationId, startDate, endDate);

        if (metrics.getTakenCount() == 0 && metrics.getMissedCount() == 0 && metrics.getSkippedCount() == 0) {
            throw new GlobalExceptionHandler.ResourceNotFoundException(
                    "No compliance data found for user ID: " + userId + " and medication ID: " + medicationId + " in the specified date range.");
        }

        return ResponseEntity.ok(metrics);
    }
}