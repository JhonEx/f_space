package com.example.f_space.service;

import com.example.f_space.model.Intake;
import com.example.f_space.model.Medication;
import com.example.f_space.model.Schedule;
import com.example.f_space.repository.IntakeRepository;
import com.example.f_space.repository.ScheduleRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;


@SpringBootTest
public class ComplianceServiceImplTest {

    @Mock
    private IntakeRepository intakeRepository;

    @Mock
    private ScheduleRepository scheduleRepository;

    @InjectMocks
    private ComplianceServiceImpl complianceService;

    private Schedule schedule;
    private Intake intake;
    private Medication medication;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Initialize test data
        medication = new Medication();
        medication.setId(1L);
        medication.setName("Paracetamol");
        medication.setDosageForm("Tablet");
        medication.setStrength("500mg");

        schedule = new Schedule();
        schedule.setId(1L);
        schedule.setMedication(medication);

        // Use a fixed UTC time for consistency
        ZonedDateTime fixedUtcTime = ZonedDateTime.of(2025, 2, 21, 12, 0, 0, 0, ZoneOffset.UTC);
        intake = new Intake();
        intake.setId(1L);
        intake.setSchedule(schedule);
        intake.setStatus("TAKEN");
        intake.setScheduledFor(Timestamp.from(fixedUtcTime.toInstant()));
    }

    @Test
    void calculateCompliance() {
        when(scheduleRepository.findByUserId(1L)).thenReturn(Arrays.asList(schedule));
        when(intakeRepository.findByScheduleId(1L)).thenReturn(Arrays.asList(intake));

        LocalDate startDate = LocalDate.of(2025, 2, 14); // 7 days before
        LocalDate endDate = LocalDate.of(2025, 2, 21);

        ComplianceMetrics metrics = complianceService.calculateCompliance(1L, 1L, startDate, endDate);

        assertNotNull(metrics);
        assertEquals(1.0, metrics.getComplianceRate(), 0.001);
        assertEquals(1.0, metrics.getAdjustedRate(), 0.001);
        assertEquals(0, metrics.getMissedCount());
        assertEquals(0, metrics.getSkippedCount());
        assertEquals(1, metrics.getTakenCount());
        assertEquals(0, metrics.getConsecutiveMisses());
        assertEquals("TAKEN", metrics.getCompliancePattern());
    }

    @Test
    void calculateComplianceWithDifferentTimeZones() {
        ZonedDateTime pstDateTime = ZonedDateTime.of(2025, 2, 21, 4, 0, 0, 0, ZoneId.of("America/Los_Angeles"));
        ZonedDateTime utcConvertedTime = pstDateTime.withZoneSameInstant(ZoneOffset.UTC); // 2025-02-21 12:00:00Z
        intake.setScheduledFor(Timestamp.from(utcConvertedTime.toInstant()));

        when(scheduleRepository.findByUserId(1L)).thenReturn(Arrays.asList(schedule));
        when(intakeRepository.findByScheduleId(1L)).thenReturn(Arrays.asList(intake));

        LocalDate startDate = LocalDate.of(2025, 2, 14); // 7 days before
        LocalDate endDate = LocalDate.of(2025, 2, 21);

        System.out.println("intake scheduledFor: " + intake.getScheduledFor());
        System.out.println("startDateTime: " + startDate.atStartOfDay(ZoneOffset.UTC));
        System.out.println("endDateTime: " + endDate.atTime(23, 59, 59).atZone(ZoneOffset.UTC));

        ComplianceMetrics metrics = complianceService.calculateCompliance(1L, 1L, startDate, endDate);

        assertNotNull(metrics);
        assertEquals(1.0, metrics.getComplianceRate(), 0.001);
        assertEquals(1.0, metrics.getAdjustedRate(), 0.001);
        assertEquals(0, metrics.getMissedCount());
        assertEquals(0, metrics.getSkippedCount());
        assertEquals(1, metrics.getTakenCount());
        assertEquals(0, metrics.getConsecutiveMisses());
        assertEquals("TAKEN", metrics.getCompliancePattern());
    }

    @Test
    void calculateComplianceWithMissedIntake() {
        Intake missedIntake = new Intake();
        missedIntake.setId(2L);
        missedIntake.setSchedule(schedule);
        missedIntake.setStatus("MISSED");
        missedIntake.setScheduledFor(Timestamp.from(ZonedDateTime.of(2025, 2, 20, 12, 0, 0, 0, ZoneOffset.UTC).toInstant()));

        when(scheduleRepository.findByUserId(1L)).thenReturn(Arrays.asList(schedule));
        when(intakeRepository.findByScheduleId(1L)).thenReturn(Arrays.asList(intake, missedIntake));

        LocalDate startDate = LocalDate.of(2025, 2, 14);
        LocalDate endDate = LocalDate.of(2025, 2, 21);

        ComplianceMetrics metrics = complianceService.calculateCompliance(1L, 1L, startDate, endDate);

        assertNotNull(metrics);
        assertEquals(0.5, metrics.getComplianceRate(), 0.001);
        assertEquals(0.5, metrics.getAdjustedRate(), 0.001);
        assertEquals(1, metrics.getMissedCount());
        assertEquals(0, metrics.getSkippedCount());
        assertEquals(1, metrics.getTakenCount());
        assertEquals(1, metrics.getConsecutiveMisses());
        assertEquals("TAKEN", metrics.getCompliancePattern());
    }
}