package com.example.f_space.controller;

import com.example.f_space.controller.exceptionhandler.GlobalExceptionHandler;
import com.example.f_space.model.Intake;
import com.example.f_space.model.Schedule;
import com.example.f_space.repository.IntakeRepository;
import com.example.f_space.repository.ScheduleRepository;
import com.example.f_space.service.ComplianceMetrics;
import com.example.f_space.service.ComplianceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
@Tag(name = "Compliance", description = "Endpoints for calculating medication compliance")
public class ComplianceController {

    private ComplianceService complianceService;

    @Operation(summary = "Test Compliance Controller")
    @GetMapping("/test")
    public ResponseEntity<String> testController() {
        return ResponseEntity.ok("Controller is working!");
    }

    @Operation(summary = "Get Compliance Rate", description = "Calculates and returns the compliance rate for a specific user and medication within a date range.")

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