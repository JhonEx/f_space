package com.example.f_space.controller;

import com.example.f_space.controller.exceptionhandler.GlobalExceptionHandler;
import com.example.f_space.model.Intake;
import com.example.f_space.model.Schedule;

import com.example.f_space.repository.ScheduleRepository;
import com.example.f_space.service.AnalyticsService;
import com.example.f_space.service.IntakeService;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/analytics")
@AllArgsConstructor
@Tag(name = "Analytics", description = "Endpoints for compliance and moving averages analytics")
public class AnalyticsController {

    private AnalyticsService analyticsService;

    private IntakeService intakeService;

    private ScheduleRepository scheduleRepository;


    @GetMapping("/test")
    @Operation(summary = "Test the API endpoint", description = "Returns a simple confirmation that the controller is working")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<String> testController() {
        return ResponseEntity.ok("Controller is working!");
    }

    @GetMapping("/moving-averages")
    @Operation(summary = "Get moving averages for medication intakes", description = "Calculates SMA, EMA, or WMA for medication intake counts over a specified period")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation"),
            @ApiResponse(responseCode = "404", description = "No schedules or intakes found, or insufficient data"),
            @ApiResponse(responseCode = "400", description = "Invalid moving average type")
    })
    public ResponseEntity<MovingAverageResponse> getMovingAverages(
            @RequestParam Long userId,
            @RequestParam Long medicationId,
            @RequestParam String type,
            @RequestParam int days) {

        List<Schedule> schedules = scheduleRepository.findByUserId(userId);
        if (schedules.isEmpty()) {
            throw new GlobalExceptionHandler.ResourceNotFoundException("No schedules found for user ID: " + userId);
        }

        List<Intake> intakes = schedules.stream()
                .filter(schedule -> schedule.getMedication().getId().equals(medicationId))
                .flatMap(schedule -> intakeService.getIntakesBySchedule(schedule.getId()).stream())
                .filter(intake -> "TAKEN".equalsIgnoreCase(intake.getStatus()))
                .sorted(Comparator.comparing(Intake::getScheduledFor))
                .collect(Collectors.toList());

        if (intakes.isEmpty()) {
            throw new GlobalExceptionHandler.ResourceNotFoundException(
                    "No intakes found for medication ID: " + medicationId + " and user ID: " + userId);
        }

        List<ZonedDateTime> dates = intakes.stream()
                .map(i -> i.getScheduledFor().toInstant().atZone(ZoneOffset.UTC))
                .distinct()
                .collect(Collectors.toList());

        List<Double> dailyCounts = dates.stream()
                .map(date -> (double) intakes.stream()
                        .filter(i -> i.getScheduledFor().toInstant().atZone(ZoneOffset.UTC).toLocalDate().equals(date.toLocalDate()))
                        .count())
                .collect(Collectors.toList());

        if (dailyCounts.size() < days) {
            throw new GlobalExceptionHandler.ResourceNotFoundException("Not enough data points to calculate " + type + " for period: " + days);
        }

        List<Double> movingAverages = analyticsService.calculateMovingAverage(dailyCounts, days, type);
        MovingAverageResponse response = new MovingAverageResponse(dates, movingAverages, type, days);
        return ResponseEntity.ok(response);
    }
    @Data
    public static class MovingAverageResponse {
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
        private List<ZonedDateTime> dates;
        private List<Double> values;
        private String type;
        private int period;
        public MovingAverageResponse(List<ZonedDateTime> dates, List<Double> values, String type, int period) {
            this.dates = dates;
            this.values = values;
            this.type = type;
            this.period = period;
        }
    }
}
