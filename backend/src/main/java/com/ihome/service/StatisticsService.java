package com.ihome.service;

import com.ihome.entity.*;
import com.ihome.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 数据统计和报表服务类
 * 提供各种统计数据和报表功能
 */
@Service
public class StatisticsService {

    @Autowired
    private StudentMapper studentMapper;
    
    @Autowired
    private DormitoryMapper dormitoryMapper;
    
    @Autowired
    private BedMapper bedMapper;
    
    @Autowired
    private DormitoryAllocationMapper allocationMapper;
    
    @Autowired
    private PaymentRecordMapper paymentMapper;
    
    @Autowired
    private RepairOrderMapper repairMapper;
    
    @Autowired
    private DormitorySwitchMapper switchMapper;

    /**
     * 获取仪表盘统计数据
     */
    public Map<String, Object> getDashboardStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // 学生统计
        long totalStudents = studentMapper.selectCount(null);
        long allocatedStudents = allocationMapper.selectCount(null);
        long unallocatedStudents = totalStudents - allocatedStudents;
        
        // 宿舍统计
        long totalDormitories = dormitoryMapper.selectCount(null);
        long totalBeds = bedMapper.selectCount(null);
        long occupiedBeds = bedMapper.selectList(null).stream()
                .filter(bed -> "已占用".equals(bed.getStatus()))
                .count();
        long availableBeds = totalBeds - occupiedBeds;
        
        // 缴费统计
        long totalPayments = paymentMapper.selectCount(null);
        // PaymentRecord没有status字段，暂时设为0
        long pendingPayments = 0;
        
        // 维修统计
        long totalRepairs = repairMapper.selectCount(null);
        long pendingRepairs = repairMapper.selectList(null).stream()
                .filter(repair -> "待处理".equals(repair.getStatus()))
                .count();
        
        // 调换统计
        long totalSwitches = switchMapper.selectCount(null);
        long pendingSwitches = switchMapper.selectList(null).stream()
                .filter(switchRequest -> "待审核".equals(switchRequest.getStatus()))
                .count();
        
        statistics.put("totalStudents", totalStudents);
        statistics.put("allocatedStudents", allocatedStudents);
        statistics.put("unallocatedStudents", unallocatedStudents);
        statistics.put("allocationRate", totalStudents > 0 ? (double) allocatedStudents / totalStudents * 100 : 0);
        
        statistics.put("totalDormitories", totalDormitories);
        statistics.put("totalBeds", totalBeds);
        statistics.put("occupiedBeds", occupiedBeds);
        statistics.put("availableBeds", availableBeds);
        statistics.put("occupancyRate", totalBeds > 0 ? (double) occupiedBeds / totalBeds * 100 : 0);
        
        statistics.put("totalPayments", totalPayments);
        statistics.put("pendingPayments", pendingPayments);
        
        statistics.put("totalRepairs", totalRepairs);
        statistics.put("pendingRepairs", pendingRepairs);
        
        statistics.put("totalSwitches", totalSwitches);
        statistics.put("pendingSwitches", pendingSwitches);
        
        return statistics;
    }

    /**
     * 获取学生分布统计
     */
    public Map<String, Object> getStudentDistributionStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // 按专业统计
        Map<String, Long> majorDistribution = new HashMap<>();
        List<Student> students = studentMapper.selectList(null);
        for (Student student : students) {
            majorDistribution.merge(student.getMajor(), 1L, Long::sum);
        }
        
        // 按性别统计
        Map<String, Long> genderDistribution = new HashMap<>();
        for (Student student : students) {
            genderDistribution.merge(student.getGender(), 1L, Long::sum);
        }
        
        // 按学院统计
        Map<String, Long> collegeDistribution = new HashMap<>();
        for (Student student : students) {
            collegeDistribution.merge(student.getCollege(), 1L, Long::sum);
        }
        
        statistics.put("majorDistribution", majorDistribution);
        statistics.put("genderDistribution", genderDistribution);
        statistics.put("collegeDistribution", collegeDistribution);
        statistics.put("totalStudents", students.size());
        
        return statistics;
    }

    /**
     * 获取宿舍使用情况统计
     */
    public Map<String, Object> getDormitoryUsageStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // 按楼栋统计
        Map<String, Map<String, Object>> buildingStats = new HashMap<>();
        List<Dormitory> dormitories = dormitoryMapper.selectList(null);
        
        for (Dormitory dormitory : dormitories) {
            String buildingId = dormitory.getBuildingId();
            buildingStats.computeIfAbsent(buildingId, k -> {
                Map<String, Object> stats = new HashMap<>();
                stats.put("totalDormitories", 0);
                stats.put("totalBeds", 0);
                stats.put("occupiedBeds", 0);
                stats.put("availableBeds", 0);
                return stats;
            });
            
            Map<String, Object> stats = buildingStats.get(buildingId);
            stats.put("totalDormitories", (Integer) stats.get("totalDormitories") + 1);
            
            // 统计床位
            List<Bed> beds = bedMapper.selectList(null).stream()
                    .filter(bed -> dormitory.getId().equals(bed.getDormitoryId()))
                    .toList();
            
            stats.put("totalBeds", (Integer) stats.get("totalBeds") + beds.size());
            
            long occupiedBeds = beds.stream()
                    .filter(bed -> "已占用".equals(bed.getStatus()))
                    .count();
            
            stats.put("occupiedBeds", (Integer) stats.get("occupiedBeds") + (int) occupiedBeds);
            stats.put("availableBeds", (Integer) stats.get("totalBeds") - (Integer) stats.get("occupiedBeds"));
        }
        
        // 计算使用率
        for (Map<String, Object> stats : buildingStats.values()) {
            int totalBeds = (Integer) stats.get("totalBeds");
            int occupiedBeds = (Integer) stats.get("occupiedBeds");
            stats.put("occupancyRate", totalBeds > 0 ? (double) occupiedBeds / totalBeds * 100 : 0);
        }
        
        statistics.put("buildingStatistics", buildingStats);
        
        return statistics;
    }

    /**
     * 获取缴费统计报表
     */
    public Map<String, Object> getPaymentStatistics(String startDate, String endDate) {
        Map<String, Object> statistics = new HashMap<>();
        
        // 时间范围统计
        List<PaymentRecord> payments = paymentMapper.selectList(null);
        
        // 按缴费方式统计
        Map<String, Long> paymentMethodStats = new HashMap<>();
        Map<String, Double> paymentAmountStats = new HashMap<>();
        
        double totalAmount = 0;
        for (PaymentRecord payment : payments) {
            paymentMethodStats.merge(payment.getPaymentMethod(), 1L, Long::sum);
            double amount = payment.getAmount().doubleValue();
            paymentAmountStats.merge(payment.getPaymentMethod(), amount, Double::sum);
            totalAmount += amount;
        }
        
        // 按状态统计（PaymentRecord没有status字段，暂时为空）
        Map<String, Long> statusStats = new HashMap<>();
        
        statistics.put("totalPayments", payments.size());
        statistics.put("totalAmount", totalAmount);
        statistics.put("paymentMethodStats", paymentMethodStats);
        statistics.put("paymentAmountStats", paymentAmountStats);
        statistics.put("statusStats", statusStats);
        
        return statistics;
    }

    /**
     * 获取维修统计报表
     */
    public Map<String, Object> getRepairStatistics(String startDate, String endDate) {
        Map<String, Object> statistics = new HashMap<>();
        
        List<RepairOrder> repairs = repairMapper.selectList(null);
        
        // 按维修类型统计
        Map<String, Long> repairTypeStats = new HashMap<>();
        for (RepairOrder repair : repairs) {
            repairTypeStats.merge(repair.getRepairType(), 1L, Long::sum);
        }
        
        // 按紧急程度统计
        Map<String, Long> urgencyStats = new HashMap<>();
        for (RepairOrder repair : repairs) {
            urgencyStats.merge(repair.getUrgencyLevel(), 1L, Long::sum);
        }
        
        // 按状态统计
        Map<String, Long> statusStats = new HashMap<>();
        for (RepairOrder repair : repairs) {
            statusStats.merge(repair.getStatus(), 1L, Long::sum);
        }
        
        // 按宿舍统计
        Map<String, Long> dormitoryStats = new HashMap<>();
        for (RepairOrder repair : repairs) {
            dormitoryStats.merge(repair.getDormitoryId(), 1L, Long::sum);
        }
        
        statistics.put("totalRepairs", repairs.size());
        statistics.put("repairTypeStats", repairTypeStats);
        statistics.put("urgencyStats", urgencyStats);
        statistics.put("statusStats", statusStats);
        statistics.put("dormitoryStats", dormitoryStats);
        
        return statistics;
    }

    /**
     * 获取调换申请统计
     */
    public Map<String, Object> getSwitchStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        List<DormitorySwitch> switches = switchMapper.selectList(null);
        
        // 按状态统计
        Map<String, Long> statusStats = new HashMap<>();
        for (DormitorySwitch switchRequest : switches) {
            statusStats.merge(switchRequest.getStatus(), 1L, Long::sum);
        }
        
        // 按调换类型统计
        Map<String, Long> typeStats = new HashMap<>();
        for (DormitorySwitch switchRequest : switches) {
            if (switchRequest.getTargetStudentId() != null) {
                typeStats.merge("室友调换", 1L, Long::sum);
            } else if (switchRequest.getTargetBedId() != null) {
                typeStats.merge("床位调换", 1L, Long::sum);
            } else {
                typeStats.merge("其他", 1L, Long::sum);
            }
        }
        
        // 按月份统计申请数量
        Map<String, Long> monthlyStats = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        for (DormitorySwitch switchRequest : switches) {
            if (switchRequest.getApplyTime() != null) {
                String month = switchRequest.getApplyTime().format(formatter);
                monthlyStats.merge(month, 1L, Long::sum);
            }
        }
        
        statistics.put("totalSwitches", switches.size());
        statistics.put("statusStats", statusStats);
        statistics.put("typeStats", typeStats);
        statistics.put("monthlyStats", monthlyStats);
        
        return statistics;
    }

    /**
     * 获取趋势分析数据
     */
    public Map<String, Object> getTrendAnalysis() {
        Map<String, Object> trends = new HashMap<>();
        
        // 最近7天的数据趋势
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(7);
        
        // 这里可以实现更复杂的趋势分析逻辑
        // 暂时返回基础数据
        
        trends.put("period", "最近7天");
        trends.put("startDate", startDate.toString());
        trends.put("endDate", endDate.toString());
        
        return trends;
    }

    /**
     * 生成综合报表
     */
    public Map<String, Object> generateComprehensiveReport() {
        Map<String, Object> report = new HashMap<>();
        
        report.put("dashboard", getDashboardStatistics());
        report.put("studentDistribution", getStudentDistributionStatistics());
        report.put("dormitoryUsage", getDormitoryUsageStatistics());
        report.put("paymentStats", getPaymentStatistics(null, null));
        report.put("repairStats", getRepairStatistics(null, null));
        report.put("switchStats", getSwitchStatistics());
        report.put("trends", getTrendAnalysis());
        report.put("generatedAt", LocalDateTime.now());
        
        return report;
    }
}
