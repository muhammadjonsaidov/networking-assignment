package org.example.appsmallcrm.controller;

import lombok.RequiredArgsConstructor;
import org.example.appsmallcrm.dto.ApiResponse;
import org.example.appsmallcrm.entity.Activity;
import org.example.appsmallcrm.entity.Customer;
import org.example.appsmallcrm.entity.DashboardStats;
import org.example.appsmallcrm.service.DashboardStatsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // Secure dashboard for ADMINs
public class DashboardController {

    private final DashboardStatsService dashboardStatsService;

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStats>> getDashboardStats() {
        return ResponseEntity.ok(ApiResponse.success(dashboardStatsService.getStats()));
    }

    @GetMapping("/sales/bar-chart")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getBarChartData() {
        return ResponseEntity.ok(ApiResponse.success(dashboardStatsService.getBarData()));
    }

    @GetMapping("/sales/line-chart")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getLineChartData() {
        return ResponseEntity.ok(ApiResponse.success(dashboardStatsService.getLineData()));
    }

    @GetMapping("/sales/pie-chart")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getPieChartData() {
        return ResponseEntity.ok(ApiResponse.success(dashboardStatsService.getPieData()));
    }

    @GetMapping("/recent-customers")
    public ResponseEntity<ApiResponse<List<Customer>>> getRecentCustomers(
            @RequestParam Integer count) {
        return ResponseEntity.ok(ApiResponse.success(dashboardStatsService.getRecentCustomers(count)));
    }

    @GetMapping("/recent-activities")
    public ResponseEntity<ApiResponse<List<Activity>>> getRecentActivities(
            @RequestParam(defaultValue = "5") int count) {
        return ResponseEntity.ok(ApiResponse.success(dashboardStatsService.getRecentActivities(count)));
    }
}