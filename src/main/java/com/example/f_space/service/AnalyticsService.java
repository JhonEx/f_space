package com.example.f_space.service;

import java.util.List;

public interface AnalyticsService {
    List<Double> calculateMovingAverage(List<Double> values, int days, String type);
}