package com.example.f_space.service;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AnalyticsServiceImplTest {

    @InjectMocks
    private AnalyticsServiceImpl analyticsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCalculateSMA() {
        List<Double> values = Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0);
        List<Double> result = analyticsService.calculateMovingAverage(values, 3, "SMA");

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(2.0, result.get(0));
        assertEquals(3.0, result.get(1));
        assertEquals(4.0, result.get(2));
    }

    @Test
    void testCalculateEMA() {
        List<Double> values = Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0);
        List<Double> result = analyticsService.calculateMovingAverage(values, 3, "EMA");

        assertNotNull(result);
        assertEquals(5, result.size());
        assertEquals(1.0, result.get(0)); // First EMA value should match the first data point
        assertTrue(result.get(1) > 1.0); // Check progression
        assertTrue(result.get(2) > result.get(1));
    }

    @Test
    void testCalculateWMA() {
        List<Double> values = Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0);
        List<Double> result = analyticsService.calculateMovingAverage(values, 3, "WMA");

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(2.3333333333333335, result.get(0));
        assertEquals(3.3333333333333335, result.get(1));
        assertEquals(4.333333333333333, result.get(2));
    }
    @Test
    void testInvalidType() {
        List<Double> values = Arrays.asList(1.0, 2.0, 3.0);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            analyticsService.calculateMovingAverage(values, 3, "INVALID");
        });

        assertEquals("Invalid moving average type", exception.getMessage());
    }
}
