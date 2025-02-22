package com.example.f_space.service;

import com.example.f_space.model.Intake;
import com.example.f_space.model.Schedule;
import com.example.f_space.repository.IntakeRepository;
import com.example.f_space.repository.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ComplianceServiceImpl implements ComplianceService{

    @Autowired
    private IntakeRepository intakeRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @Cacheable("complianceRate")
    public ComplianceMetrics calculateCompliance(Long userId, Long medicationId, LocalDate startDate, LocalDate endDate) {
        ZonedDateTime startDateTime = startDate.atStartOfDay(ZoneOffset.UTC);
        ZonedDateTime endDateTime = endDate.atTime(23, 59, 59).atZone(ZoneOffset.UTC);

        List<Schedule> schedules = scheduleRepository.findByUserId(userId);
        List<Intake> intakes = schedules.stream()
                .filter(schedule -> schedule.getMedication().getId().equals(medicationId))
                .flatMap(schedule -> intakeRepository.findByScheduleId(schedule.getId()).stream())
                .filter(intake -> {
                    ZonedDateTime intakeDate = intake.getScheduledFor().toInstant().atZone(ZoneOffset.UTC);
                    return !intakeDate.isBefore(startDateTime) && !intakeDate.isAfter(endDateTime);
                })
                .collect(Collectors.toList());

        long taken = intakes.stream().filter(i -> "TAKEN".equalsIgnoreCase(i.getStatus())).count();
        long missed = intakes.stream().filter(i -> "MISSED".equalsIgnoreCase(i.getStatus())).count();
        long skipped = intakes.stream().filter(i -> "SKIPPED".equalsIgnoreCase(i.getStatus())).count();
        long total = taken + missed + skipped;
        long validTotal = taken + missed;

        double complianceRate = total > 0 ? (double) taken / total : 0.0;
        double adjustedRate = validTotal > 0 ? (double) taken / validTotal : 0.0;
        int consecutiveMisses = countConsecutiveMisses(intakes);
        String pattern = identifyPatterns(intakes);

        return new ComplianceMetrics(complianceRate, adjustedRate, (int) missed, (int) skipped, (int) taken, consecutiveMisses, pattern);
    }


    private int countConsecutiveMisses(List<Intake> intakes) {
        int maxConsecutive = 0, currentConsecutive = 0;
        for (Intake intake : intakes) {
            if ("MISSED".equalsIgnoreCase(intake.getStatus())) {
                currentConsecutive++;
                maxConsecutive = Math.max(maxConsecutive, currentConsecutive);
            } else {
                currentConsecutive = 0;
            }
        }
        return maxConsecutive;
    }

    private String identifyPatterns(List<Intake> intakes) {
        Map<String, Long> patternMap = intakes.stream()
                .collect(Collectors.groupingBy(Intake::getStatus, Collectors.counting()));
        return patternMap.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("No clear pattern");
    }
}