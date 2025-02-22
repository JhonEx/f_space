package com.example.f_space.service;

import java.time.LocalDate;

public interface ComplianceService {
    ComplianceMetrics calculateCompliance(Long userId, Long medicationId, LocalDate startDate, LocalDate endDate);
}