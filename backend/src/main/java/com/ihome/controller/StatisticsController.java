package com.ihome.controller;

import com.ihome.common.ApiResponse;
import com.ihome.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 数据统计和报表控制器
 * 提供各种统计数据和报表接口
 */
@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    /**
     * 获取仪表盘统计数据
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> getDashboardStatistics() {
        try {
            Map<String, Object> statistics = statisticsService.getDashboardStatistics();
            return ApiResponse.ok(statistics);
        } catch (Exception e) {
            return ApiResponse.error("获取仪表盘统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取学生分布统计
     */
    @GetMapping("/students/distribution")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> getStudentDistributionStatistics() {
        try {
            Map<String, Object> statistics = statisticsService.getStudentDistributionStatistics();
            return ApiResponse.ok(statistics);
        } catch (Exception e) {
            return ApiResponse.error("获取学生分布统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取宿舍使用情况统计
     */
    @GetMapping("/dormitories/usage")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> getDormitoryUsageStatistics() {
        try {
            Map<String, Object> statistics = statisticsService.getDormitoryUsageStatistics();
            return ApiResponse.ok(statistics);
        } catch (Exception e) {
            return ApiResponse.error("获取宿舍使用统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取缴费统计报表
     */
    @GetMapping("/payments")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> getPaymentStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            Map<String, Object> statistics = statisticsService.getPaymentStatistics(startDate, endDate);
            return ApiResponse.ok(statistics);
        } catch (Exception e) {
            return ApiResponse.error("获取缴费统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取维修统计报表
     */
    @GetMapping("/repairs")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> getRepairStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            Map<String, Object> statistics = statisticsService.getRepairStatistics(startDate, endDate);
            return ApiResponse.ok(statistics);
        } catch (Exception e) {
            return ApiResponse.error("获取维修统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取调换申请统计
     */
    @GetMapping("/switches")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> getSwitchStatistics() {
        try {
            Map<String, Object> statistics = statisticsService.getSwitchStatistics();
            return ApiResponse.ok(statistics);
        } catch (Exception e) {
            return ApiResponse.error("获取调换统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取趋势分析数据
     */
    @GetMapping("/trends")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> getTrendAnalysis() {
        try {
            Map<String, Object> trends = statisticsService.getTrendAnalysis();
            return ApiResponse.ok(trends);
        } catch (Exception e) {
            return ApiResponse.error("获取趋势分析失败: " + e.getMessage());
        }
    }

    /**
     * 生成综合报表
     */
    @GetMapping("/comprehensive-report")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> generateComprehensiveReport() {
        try {
            Map<String, Object> report = statisticsService.generateComprehensiveReport();
            return ApiResponse.ok(report);
        } catch (Exception e) {
            return ApiResponse.error("生成综合报表失败: " + e.getMessage());
        }
    }

    /**
     * 获取实时统计数据
     */
    @GetMapping("/realtime")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> getRealtimeStatistics() {
        try {
            Map<String, Object> realtimeStats = Map.of(
                "onlineUsers", 0, // 待实现
                "activeRequests", 0, // 待实现
                "systemLoad", "正常", // 待实现
                "lastUpdated", System.currentTimeMillis()
            );
            return ApiResponse.ok(realtimeStats);
        } catch (Exception e) {
            return ApiResponse.error("获取实时统计失败: " + e.getMessage());
        }
    }
}

