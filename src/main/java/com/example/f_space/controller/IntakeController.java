package com.example.f_space.controller;


import com.example.f_space.controller.exceptionhandler.GlobalExceptionHandler;
import com.example.f_space.model.Intake;
import com.example.f_space.model.Schedule;
import com.example.f_space.model.SkipReason;
import com.example.f_space.repository.ScheduleRepository;
import com.example.f_space.repository.SkipReasonRepository;
import com.example.f_space.service.IntakeService;
import com.example.f_space.service.ScheduleService;
import com.example.f_space.service.SkipReasonService;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/intakes")
@AllArgsConstructor
@Tag(name = "Intakes", description = "Manage medication intake records")
public class IntakeController {

    private IntakeService intakeService;

    private ScheduleService scheduleService;

    private SkipReasonService skipReasonService;

    @Operation(summary = "Record a new intake")
    @GetMapping("/test")
    public ResponseEntity<String> testController() {
        return ResponseEntity.ok("Controller is working!");
    }

    @Operation(summary = "Get intakes by schedule ID")
    @PostMapping
    public ResponseEntity<String> recordIntake(@RequestBody IntakeRequest intakeRequest) {
        Schedule schedule = scheduleService.findById(intakeRequest.getScheduleId())
                .orElseThrow(() -> new RuntimeException("Schedule not found with ID: " + intakeRequest.getScheduleId()));

        Intake intake = new Intake();
        intake.setSchedule(schedule);
        intake.setStatus(intakeRequest.getStatus());

        if ("TAKEN".equalsIgnoreCase(intakeRequest.getStatus())) {
            if (intakeRequest.getTakenAt() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Taken time is required for TAKEN status");
            }
            intake.setTakenAt(Timestamp.from(intakeRequest.getTakenAt().toInstant()));
        }
        intake.setScheduledFor(Timestamp.from(ZonedDateTime.now(ZoneOffset.UTC).toInstant()));  // Store current time in UTC

            Intake savedIntake = intakeService.recordIntake(intake);

        if ("SKIPPED".equalsIgnoreCase(intakeRequest.getStatus())) {
            if (intakeRequest.getSkipReason() == null || intakeRequest.getSkipReason().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Skip reason is required for SKIPPED status");
            }
            SkipReason reason = new SkipReason();
            reason.setIntake(savedIntake); // Associate saved intake
            reason.setReasonType("Patient Request");
            reason.setSpecificReason(intakeRequest.getSkipReason());
            reason.setAuthorizedBy("Doctor Approval");
            skipReasonService.save(reason);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body("Intake recorded successfully");
    }


    @Operation(summary = "Get schedule by ID")
    @GetMapping("/schedule/{scheduleId}")
    public ResponseEntity<List<Intake>> getIntakesBySchedule(@PathVariable Long scheduleId) {
        List<Intake> intakes = intakeService.getIntakesBySchedule(scheduleId);
        if (intakes.isEmpty()) {
            throw new GlobalExceptionHandler.ResourceNotFoundException("No intakes found for schedule ID: " + scheduleId);
        }
        return ResponseEntity.ok(intakes);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get intakes by user", description = "Retrieves a list of intakes for a specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation"),
            @ApiResponse(responseCode = "404", description = "No intakes found for the user")
    })
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
        private ZonedDateTime takenAt;
        private String skipReason;
    }

}
