package com.example.f_space.controller;

import static org.junit.jupiter.api.Assertions.*;

import com.example.f_space.controller.AnalyticsController;
import com.example.f_space.controller.exceptionhandler.GlobalExceptionHandler;
import com.example.f_space.model.Intake;
import com.example.f_space.model.Medication;
import com.example.f_space.model.Schedule;
import com.example.f_space.model.SkipReason;
import com.example.f_space.repository.SkipReasonRepository;
import com.example.f_space.service.AnalyticsService;
import com.example.f_space.service.IntakeService;
import com.example.f_space.repository.ScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Time;
import java.util.Optional;


@WebMvcTest(IntakeController.class)
class IntakeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IntakeService intakeService;

    @MockBean
    private ScheduleRepository scheduleRepository;

    @MockBean
    private SkipReasonRepository skipReasonRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Schedule sampleSchedule;
    private Intake sampleIntake;

    @BeforeEach
    void setUp() {
        // Mock sample schedule from DataLoader (Paracetamol, userId=1)

        Medication paracetamol = new Medication();
        paracetamol.setName("Paracetamol");
        paracetamol.setDosageForm("Tablet");
        paracetamol.setStrength("500mg");

        sampleSchedule = new Schedule();
        sampleSchedule.setId(1L);
        sampleSchedule.setUserId(1L);
        sampleSchedule.setMedication(paracetamol);
        sampleSchedule.setScheduledTime(Time.valueOf("08:00:00"));
        sampleSchedule.setDaysOfWeek(Arrays.asList(1, 3, 5));

        // Mock sample intake
        sampleIntake = new Intake();
        sampleIntake.setId(1L);
        sampleIntake.setSchedule(sampleSchedule);
        sampleIntake.setStatus("TAKEN");
        sampleIntake.setScheduledFor(Timestamp.from(ZonedDateTime.now().toInstant()));
        sampleIntake.setTakenAt(Timestamp.from(ZonedDateTime.now().toInstant()));
    }

    @Test
    void testTestController() throws Exception {
        mockMvc.perform(get("/api/v1/intakes/test"))
                .andExpect(status().isOk())
                .andExpect(content().string("Controller is working!"));
    }

    @Test
    void testRecordIntake_TakenStatus_Success() throws Exception {
        IntakeController.IntakeRequest request = new IntakeController.IntakeRequest();
        request.setScheduleId(1L);
        request.setStatus("TAKEN");
        request.setTakenAt(ZonedDateTime.parse("2025-02-22T14:00:00Z"));

        when(scheduleRepository.findById(1L)).thenReturn(Optional.of(sampleSchedule));
        when(intakeService.recordIntake(any(Intake.class))).thenReturn(sampleIntake);

        mockMvc.perform(post("/api/v1/intakes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Intake recorded successfully"));

        verify(intakeService, times(1)).recordIntake(any(Intake.class));
        verify(skipReasonRepository, never()).save(any(SkipReason.class));
    }

    @Test
    void testRecordIntake_TakenStatus_MissingTakenAt() throws Exception {
        IntakeController.IntakeRequest request = new IntakeController.IntakeRequest();
        request.setScheduleId(1L);
        request.setStatus("TAKEN");
        // takenAt is null

        when(scheduleRepository.findById(1L)).thenReturn(Optional.of(sampleSchedule));

        mockMvc.perform(post("/api/v1/intakes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Taken time is required for TAKEN status"));

        verify(intakeService, never()).recordIntake(any(Intake.class));
    }

    @Test
    void testRecordIntake_SkippedStatus_Success() throws Exception {
        IntakeController.IntakeRequest request = new IntakeController.IntakeRequest();
        request.setScheduleId(1L);
        request.setStatus("SKIPPED");
        request.setSkipReason("Patient felt dizzy and chose to skip the dose.");

        when(scheduleRepository.findById(1L)).thenReturn(Optional.of(sampleSchedule));
        when(intakeService.recordIntake(any(Intake.class))).thenReturn(sampleIntake);
        when(skipReasonRepository.save(any(SkipReason.class))).thenReturn(new SkipReason());

        mockMvc.perform(post("/api/v1/intakes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Intake recorded successfully"));

        verify(intakeService, times(1)).recordIntake(any(Intake.class));
        verify(skipReasonRepository, times(1)).save(any(SkipReason.class));
    }

    @Test
    void testRecordIntake_SkippedStatus_MissingSkipReason() throws Exception {
        IntakeController.IntakeRequest request = new IntakeController.IntakeRequest();
        request.setScheduleId(1L);
        request.setStatus("SKIPPED");
        // skipReason is null

        when(scheduleRepository.findById(1L)).thenReturn(Optional.of(sampleSchedule));
        when(intakeService.recordIntake(any(Intake.class))).thenReturn(sampleIntake);

        mockMvc.perform(post("/api/v1/intakes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Skip reason is required for SKIPPED status"));

        verify(skipReasonRepository, never()).save(any(SkipReason.class));
    }

    @Test
    void testRecordIntake_ScheduleNotFound() throws Exception {
        IntakeController.IntakeRequest request = new IntakeController.IntakeRequest();
        request.setScheduleId(999L);
        request.setStatus("TAKEN");
        request.setTakenAt(ZonedDateTime.now());

        when(scheduleRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v1/intakes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is5xxServerError()) // RuntimeException triggers 500
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof RuntimeException))
                .andExpect(result -> assertEquals("Schedule not found with ID: 999",
                        result.getResolvedException().getMessage()));

        verify(intakeService, never()).recordIntake(any(Intake.class));
    }

    @Test
    void testGetIntakesBySchedule_Success() throws Exception {
        when(intakeService.getIntakesBySchedule(1L)).thenReturn(Arrays.asList(sampleIntake));

        mockMvc.perform(get("/api/v1/intakes/schedule/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("TAKEN"));

        verify(intakeService, times(1)).getIntakesBySchedule(1L);
    }

    @Test
    void testGetIntakesBySchedule_NoIntakesFound() throws Exception {
        when(intakeService.getIntakesBySchedule(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/intakes/schedule/1"))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof GlobalExceptionHandler.ResourceNotFoundException))
                .andExpect(result -> assertEquals("No intakes found for schedule ID: 1",
                        result.getResolvedException().getMessage()));
    }

    @Test
    void testGetIntakesByUser_Success() throws Exception {
        when(intakeService.getIntakesByUser(1L)).thenReturn(Arrays.asList(sampleIntake));

        mockMvc.perform(get("/api/v1/intakes/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("TAKEN"));

        verify(intakeService, times(1)).getIntakesByUser(1L);
    }

    @Test
    void testGetIntakesByUser_NoIntakesFound() throws Exception {
        when(intakeService.getIntakesByUser(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/intakes/user/1"))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof GlobalExceptionHandler.ResourceNotFoundException))
                .andExpect(result -> assertEquals("No intakes found for user ID: 1",
                        result.getResolvedException().getMessage()));
    }
}