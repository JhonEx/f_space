package com.example.f_space.service;

import com.example.f_space.model.Intake;
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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
@SpringBootTest
public class IntakeServiceImplTest {

    @Mock
    private IntakeRepository intakeRepository;

    @Mock
    private ScheduleRepository scheduleRepository;

    @InjectMocks
    private IntakeServiceImpl intakeService;

    private Schedule schedule;
    private Intake intakeTaken;
    private Intake intakeMissed;
    private Intake intakeSkipped;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        schedule = new Schedule();
        schedule.setId(1L);

        ZonedDateTime utcDateTime = ZonedDateTime.now(ZoneId.of("UTC"));

        intakeTaken = new Intake();
        intakeTaken.setId(1L);
        intakeTaken.setSchedule(schedule);
        intakeTaken.setStatus("TAKEN");
        intakeTaken.setScheduledFor(Timestamp.from(utcDateTime.toInstant()));
        intakeTaken.setTakenAt(Timestamp.from(utcDateTime.toInstant()));

        intakeMissed = new Intake();
        intakeMissed.setId(2L);
        intakeMissed.setSchedule(schedule);
        intakeMissed.setStatus("MISSED");
        intakeMissed.setScheduledFor(Timestamp.from(utcDateTime.minusDays(1).toInstant()));
        intakeMissed.setTakenAt(null);

        intakeSkipped = new Intake();
        intakeSkipped.setId(3L);
        intakeSkipped.setSchedule(schedule);
        intakeSkipped.setStatus("SKIPPED");
        intakeSkipped.setScheduledFor(Timestamp.from(utcDateTime.minusDays(2).toInstant()));
        intakeSkipped.setTakenAt(null);
    }

    @Test
    void recordIntake() {
        when(scheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));
        when(intakeRepository.save(any(Intake.class))).thenReturn(intakeTaken);

        Intake savedIntake = intakeService.recordIntake(intakeTaken);

        assertNotNull(savedIntake);
        assertEquals("TAKEN", savedIntake.getStatus());
        verify(intakeRepository, times(1)).save(intakeTaken);
    }

    @Test
    void getIntakesBySchedule() {
        when(intakeRepository.findByScheduleId(1L)).thenReturn(Arrays.asList(intakeTaken, intakeMissed, intakeSkipped));

        List<Intake> result = intakeService.getIntakesBySchedule(1L);

        assertNotNull(result);
        assertEquals(3, result.size());
        verify(intakeRepository, times(1)).findByScheduleId(1L);
    }

    @Test
    void getIntakesByUser() {
        when(scheduleRepository.findByUserId(1L)).thenReturn(Arrays.asList(schedule));
        when(intakeRepository.findByScheduleId(1L)).thenReturn(Arrays.asList(intakeTaken, intakeMissed, intakeSkipped));

        List<Intake> result = intakeService.getIntakesByUser(1L);

        assertNotNull(result);
        assertEquals(3, result.size());
        verify(scheduleRepository, times(1)).findByUserId(1L);
        verify(intakeRepository, times(1)).findByScheduleId(1L);
    }

    @Test
    void recordIntakeWithDifferentTimeZones() {
        ZonedDateTime pstDateTime = ZonedDateTime.now(ZoneId.of("America/Los_Angeles"));
        intakeTaken.setScheduledFor(Timestamp.from(pstDateTime.toInstant()));

        when(scheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));
        when(intakeRepository.save(any(Intake.class))).thenReturn(intakeTaken);

        Intake savedIntake = intakeService.recordIntake(intakeTaken);

        assertNotNull(savedIntake);
        assertEquals("TAKEN", savedIntake.getStatus());
        assertEquals(pstDateTime.toInstant(), savedIntake.getScheduledFor().toInstant());
        verify(intakeRepository, times(1)).save(intakeTaken);
    }

    @Test
    void testTakenIntake() {
        List<Intake> takenIntakes = Arrays.asList(intakeTaken);

        assertFalse(takenIntakes.isEmpty(), "There should be TAKEN intakes");
        takenIntakes.forEach(intake -> assertNotNull(intake.getTakenAt(), "TAKEN intake should have a takenAt timestamp"));
    }

    @Test
    void testMissedIntake() {
        List<Intake> missedIntakes = Arrays.asList(intakeMissed);

        assertFalse(missedIntakes.isEmpty(), "There should be MISSED intakes");
        missedIntakes.forEach(intake -> assertNull(intake.getTakenAt(), "MISSED intake should not have a takenAt timestamp"));
    }

    @Test
    void testSkippedIntake() {
        List<Intake> skippedIntakes = Arrays.asList(intakeSkipped);

        assertFalse(skippedIntakes.isEmpty(), "There should be SKIPPED intakes");
        skippedIntakes.forEach(intake -> assertNull(intake.getTakenAt(), "SKIPPED intake should not have a takenAt timestamp"));
    }
}
