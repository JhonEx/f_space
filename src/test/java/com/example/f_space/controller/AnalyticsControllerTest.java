package com.example.f_space.controller;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

import com.example.f_space.controller.AnalyticsController;
import com.example.f_space.controller.exceptionhandler.GlobalExceptionHandler;
import com.example.f_space.model.Intake;
import com.example.f_space.model.Medication;
import com.example.f_space.model.Schedule;
import com.example.f_space.service.AnalyticsService;
import com.example.f_space.service.IntakeService;
import com.example.f_space.repository.ScheduleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import java.sql.Time;

import java.util.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@WebMvcTest(AnalyticsController.class)
public class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnalyticsService analyticsService;

    @MockBean
    private IntakeService intakeService;

    @MockBean
    private ScheduleRepository scheduleRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Schedule sampleSchedule;
    private Intake sampleIntake;

    private ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);

    @BeforeEach
    void setUp() {
        // Create Medications
        Medication paracetamol = new Medication();
        paracetamol.setId(1L);
        paracetamol.setName("Paracetamol");
        paracetamol.setDosageForm("Tablet");
        paracetamol.setStrength("500mg");

        // Mock sample schedule (userId=1, medicationId=1, Paracetamol)
        sampleSchedule = new Schedule();
        sampleSchedule.setId(1L);
        sampleSchedule.setUserId(1L);
        sampleSchedule.setMedication(paracetamol);
        sampleSchedule.setScheduledTime(Time.valueOf("08:00:00"));
        sampleSchedule.setDaysOfWeek(Arrays.asList(1, 3, 5));

        // Mock sample intake (TAKEN status)
        sampleIntake = new Intake();
        sampleIntake.setId(1L);
        sampleIntake.setSchedule(sampleSchedule);
        sampleIntake.setStatus("TAKEN");
        sampleIntake.setScheduledFor(Timestamp.from(now.toInstant()));
        sampleIntake.setTakenAt(Timestamp.from(now.toInstant()));
    }

    @Test
    void testTestController() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/test"))
                .andExpect(status().isOk())
                .andExpect(content().string("Controller is working!"));
    }

    @Test
    void testGetMovingAverages_SMA_Success() throws Exception {
        // Mock data for SMA (Simple Moving Average) based on the GET request (7 days)
        List<Schedule> schedules = Arrays.asList(sampleSchedule);
        List<Intake> intakes = createIntakesFor7Days(sampleSchedule, "TAKEN");

        when(scheduleRepository.findByUserId(1L)).thenReturn(schedules);
        when(intakeService.getIntakesBySchedule(1L)).thenReturn(intakes);

        List<ZonedDateTime> dates = intakes.stream()
                .map(i -> i.getScheduledFor().toInstant().atZone(ZoneOffset.UTC))
                .distinct()
                .collect(Collectors.toList());
        List<Double> dailyCounts = dates.stream()
                .map(date -> (double) intakes.stream()
                        .filter(i -> i.getScheduledFor().toInstant().atZone(ZoneOffset.UTC).toLocalDate().equals(date.toLocalDate()))
                        .count())
                .collect(Collectors.toList());
        List<Double> smaValues = Arrays.asList(1.0); // SMA for period=7, assuming one intake per day

        when(analyticsService.calculateMovingAverage(dailyCounts, 7, "SMA")).thenReturn(smaValues);

        mockMvc.perform(get("/api/v1/analytics/moving-averages")
                        .param("userId", "1")
                        .param("medicationId", "1")
                        .param("type", "SMA")
                        .param("days", "7")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("SMA"))
                .andExpect(jsonPath("$.period").value(7))
                .andExpect(jsonPath("$.values[0]").value(1.0))
                .andExpect(jsonPath("$.dates").isArray());

        verify(scheduleRepository, times(1)).findByUserId(1L);
        verify(intakeService, times(1)).getIntakesBySchedule(1L);
        verify(analyticsService, times(1)).calculateMovingAverage(dailyCounts, 7, "SMA");
    }


    @Test
    void testGetMovingAverages_NoSchedulesFound() throws Exception {
        when(scheduleRepository.findByUserId(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/analytics/moving-averages")
                        .param("userId", "1")
                        .param("medicationId", "1")
                        .param("type", "SMA")
                        .param("days", "7")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof GlobalExceptionHandler.ResourceNotFoundException))
                .andExpect(result -> assertEquals("No schedules found for user ID: 1",
                        result.getResolvedException().getMessage()));

        verify(scheduleRepository, times(1)).findByUserId(1L);
        verify(intakeService, never()).getIntakesBySchedule(anyLong());
        verify(analyticsService, never()).calculateMovingAverage(anyList(), anyInt(), anyString());
    }

    @Test
    void testGetMovingAverages_NoIntakesFound() throws Exception {
        List<Schedule> schedules = Arrays.asList(sampleSchedule);
        when(scheduleRepository.findByUserId(1L)).thenReturn(schedules);
        when(intakeService.getIntakesBySchedule(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/analytics/moving-averages")
                        .param("userId", "1")
                        .param("medicationId", "1")
                        .param("type", "SMA")
                        .param("days", "7")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof GlobalExceptionHandler.ResourceNotFoundException))
                .andExpect(result -> assertEquals("No intakes found for medication ID: 1 and user ID: 1",
                        result.getResolvedException().getMessage()));

        verify(scheduleRepository, times(1)).findByUserId(1L);
        verify(intakeService, times(1)).getIntakesBySchedule(1L);
        verify(analyticsService, never()).calculateMovingAverage(anyList(), anyInt(), anyString());
    }

    @Test
    void testGetMovingAverages_InsufficientData() throws Exception {
        List<Schedule> schedules = Arrays.asList(sampleSchedule);
        List<Intake> intakes = Collections.singletonList(sampleIntake); // Only one intake

        when(scheduleRepository.findByUserId(1L)).thenReturn(schedules);
        when(intakeService.getIntakesBySchedule(1L)).thenReturn(intakes);

        List<ZonedDateTime> dates = Collections.singletonList(sampleIntake.getScheduledFor().toInstant().atZone(ZoneOffset.UTC));
        List<Double> dailyCounts = Collections.singletonList(1.0); // Only one data point

        mockMvc.perform(get("/api/v1/analytics/moving-averages")
                        .param("userId", "1")
                        .param("medicationId", "1")
                        .param("type", "SMA")
                        .param("days", "7")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof GlobalExceptionHandler.ResourceNotFoundException))
                .andExpect(result -> assertEquals("Not enough data points to calculate SMA for period: 7",
                        result.getResolvedException().getMessage()));

        verify(scheduleRepository, times(1)).findByUserId(1L);
        verify(intakeService, times(1)).getIntakesBySchedule(1L);
        verify(analyticsService, never()).calculateMovingAverage(anyList(), anyInt(), anyString());
    }

    @Test
    private List<Intake> createIntakesFor7Days(Schedule schedule, String status) {
        List<Intake> intakes = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            Intake intake = new Intake();
            intake.setId((long) (i + 1));
            intake.setSchedule(schedule);
            intake.setStatus(status);
            intake.setScheduledFor(Timestamp.from(now.minusDays(i).toInstant()));
            intake.setTakenAt(Timestamp.from(now.minusDays(i).toInstant()));
            intakes.add(intake);
        }
        return intakes;
    }
}