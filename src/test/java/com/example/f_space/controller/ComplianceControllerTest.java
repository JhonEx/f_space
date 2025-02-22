package com.example.f_space.controller;

import static org.junit.jupiter.api.Assertions.*;

import com.example.f_space.service.ComplianceMetrics;
import com.example.f_space.service.ComplianceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

import static org.mockito.Mockito.*;

public class ComplianceControllerTest {

    @Mock
    private ComplianceService complianceService;

    @InjectMocks
    private ComplianceController complianceController;

    private ComplianceMetrics mockMetrics;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mockMetrics = new ComplianceMetrics(1.0, 0.9, 1, 1, 10, 2, "TAKEN");
    }

    @Test
    void testGetComplianceRate_Success() {
        when(complianceService.calculateCompliance(1L, 1L, LocalDate.now().minusDays(7), LocalDate.now())).thenReturn(mockMetrics);

        ResponseEntity<ComplianceMetrics> response = complianceController.getComplianceRate(1L, 1L, LocalDate.now().minusDays(7), LocalDate.now());

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1.0, response.getBody().getComplianceRate());
        verify(complianceService, times(1)).calculateCompliance(1L, 1L, LocalDate.now().minusDays(7), LocalDate.now());
    }

    @Test
    void testGetComplianceRate_InvalidDateRange() {
        Exception exception = assertThrows(RuntimeException.class, () ->
                complianceController.getComplianceRate(1L, 1L, LocalDate.now(), LocalDate.now().minusDays(1)));

        assertEquals("Start date must be before end date.", exception.getMessage());
    }

    @Test
    void testGetComplianceRate_NoDataFound() {
        ComplianceMetrics emptyMetrics = new ComplianceMetrics(0.0, 0.0, 0, 0, 0, 0, "No Data");

        when(complianceService.calculateCompliance(1L, 1L, LocalDate.now().minusDays(7), LocalDate.now())).thenReturn(emptyMetrics);

        Exception exception = assertThrows(RuntimeException.class, () ->
                complianceController.getComplianceRate(1L, 1L, LocalDate.now().minusDays(7), LocalDate.now()));

        assertEquals("No compliance data found for user ID: 1 and medication ID: 1 in the specified date range.", exception.getMessage());
    }
}
