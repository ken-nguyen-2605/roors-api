package com.josephken.roors.admin.controller;

import com.josephken.roors.admin.dto.DashboardStats;
import com.josephken.roors.admin.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/statistics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStats> getDashboardStatistics(
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(statisticsService.getDashboardStatistics(days));
    }
}
