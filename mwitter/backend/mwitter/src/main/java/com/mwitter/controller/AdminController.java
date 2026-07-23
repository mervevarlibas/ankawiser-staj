package com.mwitter.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mwitter.dto.AdminMetricsResponse;
import com.mwitter.service.AdminMetricsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final AdminMetricsService adminMetricsService;

    @GetMapping("/metrics")
    public AdminMetricsResponse metrics() {
        return adminMetricsService.currentMetrics();
    }
}
