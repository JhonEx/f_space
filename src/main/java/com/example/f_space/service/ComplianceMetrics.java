package com.example.f_space.service;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ComplianceMetrics {

    private double complianceRate;
    private double adjustedRate;
    private int missedCount;
    private int skippedCount;
    private int takenCount;
    private int consecutiveMisses;
    private String compliancePattern;
}