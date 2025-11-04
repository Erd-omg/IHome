package com.ihome.controller;

import com.ihome.common.ApiResponse;
import com.ihome.common.OperationLog;
import com.ihome.entity.ElectricityBill;
import com.ihome.entity.ElectricityPayment;
import com.ihome.entity.ElectricityReminder;
import com.ihome.service.ElectricityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 电费管理控制器
 */
@RestController
@RequestMapping("/electricity")
public class ElectricityController {

    @Autowired
    private ElectricityService electricityService;

    /**
     * 创建电费账单 (管理员)
     */
    @PostMapping("/bills")
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog(module = "电费管理", operationType = "CREATE", description = "创建电费账单")
    public ApiResponse<Map<String, Object>> createBill(@RequestBody Map<String, Object> request) {
        String dormitoryId = (String) request.get("dormitoryId");
        String billMonth = (String) request.get("billMonth");
        BigDecimal electricityUsage = new BigDecimal(request.get("electricityUsage").toString());
        BigDecimal unitPrice = new BigDecimal(request.get("unitPrice").toString());

        Map<String, Object> result = electricityService.createElectricityBill(
                dormitoryId, billMonth, electricityUsage, unitPrice);
        
        if ((Boolean) result.get("success")) {
            return ApiResponse.ok(result);
        } else {
            return ApiResponse.error((String) result.get("message"));
        }
    }

    /**
     * 获取宿舍电费账单
     */
    @GetMapping("/bills/dormitory/{dormitoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog(module = "电费管理", operationType = "QUERY", description = "获取宿舍电费账单")
    public ApiResponse<List<ElectricityBill>> getDormitoryBills(@PathVariable String dormitoryId) {
        List<ElectricityBill> bills = electricityService.getDormitoryBills(dormitoryId);
        return ApiResponse.ok(bills);
    }

    /**
     * 获取学生电费账单
     */
    @GetMapping("/bills/student")
    @PreAuthorize("hasRole('STUDENT')")
    @OperationLog(module = "电费管理", operationType = "QUERY", description = "获取学生电费账单")
    public ApiResponse<List<ElectricityBill>> getStudentBills(@RequestParam String studentId) {
        List<ElectricityBill> bills = electricityService.getStudentBills(studentId);
        return ApiResponse.ok(bills);
    }

    /**
     * 设置电费提醒
     */
    @PostMapping("/reminders")
    @PreAuthorize("hasRole('STUDENT')")
    @OperationLog(module = "电费管理", operationType = "UPDATE", description = "设置电费提醒")
    public ApiResponse<Map<String, Object>> setReminder(@RequestBody Map<String, Object> request) {
        String studentId = (String) request.get("studentId");
        BigDecimal balanceThreshold = new BigDecimal(request.get("balanceThreshold").toString());
        Integer dueDateReminderDays = (Integer) request.get("dueDateReminderDays");
        String reminderMethod = (String) request.get("reminderMethod");

        Map<String, Object> result = electricityService.setElectricityReminder(
                studentId, balanceThreshold, dueDateReminderDays, reminderMethod);
        
        if ((Boolean) result.get("success")) {
            return ApiResponse.ok(result);
        } else {
            return ApiResponse.error((String) result.get("message"));
        }
    }

    /**
     * 获取学生提醒设置
     */
    @GetMapping("/reminders/{studentId}")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    @OperationLog(module = "电费管理", operationType = "QUERY", description = "获取学生提醒设置")
    public ApiResponse<ElectricityReminder> getReminderSettings(@PathVariable String studentId) {
        ElectricityReminder reminder = electricityService.getStudentReminderSettings(studentId);
        return ApiResponse.ok(reminder);
    }

    /**
     * 电费缴费
     */
    @PostMapping("/payments")
    @PreAuthorize("hasRole('STUDENT')")
    @OperationLog(module = "电费管理", operationType = "CREATE", description = "电费缴费")
    public ApiResponse<Map<String, Object>> payBill(@RequestBody Map<String, Object> request) {
        Long billId = Long.valueOf(request.get("billId").toString());
        String studentId = (String) request.get("studentId");
        BigDecimal paymentAmount = new BigDecimal(request.get("paymentAmount").toString());
        String paymentMethod = (String) request.get("paymentMethod");

        Map<String, Object> result = electricityService.payElectricityBill(
                billId, studentId, paymentAmount, paymentMethod);
        
        if ((Boolean) result.get("success")) {
            return ApiResponse.ok(result);
        } else {
            return ApiResponse.error((String) result.get("message"));
        }
    }

    /**
     * 获取缴费记录
     */
    @GetMapping("/payments/{studentId}")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    @OperationLog(module = "电费管理", operationType = "QUERY", description = "获取缴费记录")
    public ApiResponse<List<ElectricityPayment>> getPaymentHistory(@PathVariable String studentId) {
        List<ElectricityPayment> payments = electricityService.getPaymentHistory(studentId);
        return ApiResponse.ok(payments);
    }

    /**
     * 获取电费统计信息 (管理员)
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog(module = "电费管理", operationType = "QUERY", description = "获取电费统计信息")
    public ApiResponse<Map<String, Object>> getStatistics() {
        Map<String, Object> statistics = electricityService.getElectricityStatistics();
        return ApiResponse.ok(statistics);
    }

    /**
     * 手动触发余额提醒检查 (管理员)
     */
    @PostMapping("/reminders/check-balance")
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog(module = "电费管理", operationType = "UPDATE", description = "手动触发余额提醒检查")
    public ApiResponse<String> checkBalanceReminders() {
        electricityService.checkBalanceReminders();
        return ApiResponse.ok("余额提醒检查完成");
    }

    /**
     * 手动触发截止日期提醒检查 (管理员)
     */
    @PostMapping("/reminders/check-due-date")
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog(module = "电费管理", operationType = "UPDATE", description = "手动触发截止日期提醒检查")
    public ApiResponse<String> checkDueDateReminders() {
        electricityService.checkDueDateReminders();
        return ApiResponse.ok("截止日期提醒检查完成");
    }
}

