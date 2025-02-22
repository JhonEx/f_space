package com.example.f_space.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AnalyticsServiceImpl implements AnalyticsService{

    @Cacheable("movingAverages")
    public List<Double> calculateMovingAverage(List<Double> values, int days, String type) {
        switch (type.toUpperCase()) {
            case "SMA": return calculateSMA(values, days);
            case "EMA": return calculateEMA(values, days);
            case "WMA": return calculateWMA(values, days);
            default: throw new IllegalArgumentException("Invalid moving average type");
        }
    }

    private List<Double> calculateSMA(List<Double> values, int period) {
        List<Double> sma = new ArrayList<>();
        if (values.size() < period) {
            double average = values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            sma.add(average);
        } else {
            for (int i = 0; i <= values.size() - period; i++) {
                List<Double> subList = values.subList(i, i + period);
                double average = subList.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                sma.add(average);
            }
        }
        return sma;
    }


    private List<Double> calculateEMA(List<Double> values, int period) {
        List<Double> ema = new ArrayList<>();
        double multiplier = 2.0 / (period + 1);
        double previousEMA = values.get(0);
        ema.add(previousEMA);
        for (int i = 1; i < values.size(); i++) {
            double currentEMA = ((values.get(i) - previousEMA) * multiplier) + previousEMA;
            ema.add(currentEMA);
            previousEMA = currentEMA;
        }
        return ema;
    }

    private List<Double> calculateWMA(List<Double> values, int period) {
        List<Double> wma = new ArrayList<>();
        int weightSum = period * (period + 1) / 2;
        for (int i = 0; i <= values.size() - period; i++) {
            double weightedSum = 0.0;
            for (int j = 0; j < period; j++) {
                weightedSum += values.get(i + j) * (j + 1);
            }
            wma.add(weightedSum / weightSum);
        }
        return wma;
    }
}