package com.ihome.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ihome.entity.ElectricityBill;
import com.ihome.entity.ElectricityPayment;
import com.ihome.entity.ElectricityReminder;
import com.ihome.service.ElectricityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 电费控制器测试
 */
@WebMvcTest(ElectricityController.class)
public class ElectricityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ElectricityService electricityService;

    @Autowired
    private ObjectMapper objectMapper;

    private ElectricityBill testBill;
    private ElectricityReminder testReminder;
    private ElectricityPayment testPayment;
    private Map<String, Object> testStatistics;

    @BeforeEach
    void setUp() {
        setupTestData();
    }

    private void setupTestData() {
        // 创建测试账单
        testBill = new ElectricityBill();
        testBill.setId(1L);
        testBill.setDormitoryId("D001");
        testBill.setBillMonth("2024-12");
        testBill.setElectricityUsage(new BigDecimal("100.00"));
        testBill.setUnitPrice(new BigDecimal("0.5"));
        testBill.setTotalAmount(new BigDecimal("50.00"));
        testBill.setCurrentBalance(new BigDecimal("0.00"));
        testBill.setStatus("未缴费");
        testBill.setDueDate(LocalDateTime.now().plusDays(30));
        testBill.setCreatedAt(LocalDateTime.now());
        testBill.setUpdatedAt(LocalDateTime.now());

        // 创建测试提醒设置
        testReminder = new ElectricityReminder();
        testReminder.setId(1L);
        testReminder.setStudentId("S001");
        testReminder.setDormitoryId("D001");
        testReminder.setBalanceThreshold(new BigDecimal("50.00"));
        testReminder.setBalanceReminderEnabled(true);
        testReminder.setDueDateReminderDays(3);
        testReminder.setDueDateReminderEnabled(true);
        testReminder.setReminderMethod("站内信");
        testReminder.setCreatedAt(LocalDateTime.now());
        testReminder.setUpdatedAt(LocalDateTime.now());

        // 创建测试缴费记录
        testPayment = new ElectricityPayment();
        testPayment.setId(1L);
        testPayment.setBillId(1L);
        testPayment.setStudentId("S001");
        testPayment.setDormitoryId("D001");
        testPayment.setPaymentAmount(new BigDecimal("50.00"));
        testPayment.setPaymentMethod("支付宝");
        testPayment.setPaymentStatus("已支付");
        testPayment.setTransactionId("TXN123456");
        testPayment.setPaymentTime(LocalDateTime.now());
        testPayment.setCreatedAt(LocalDateTime.now());
        testPayment.setUpdatedAt(LocalDateTime.now());

        // 创建测试统计数据
        testStatistics = new HashMap<>();
        testStatistics.put("totalBills", 10);
        testStatistics.put("unpaidBills", 3);
        testStatistics.put("overdueBills", 1);
        testStatistics.put("monthlyTotal", new BigDecimal("500.00"));
        testStatistics.put("monthlyPaid", new BigDecimal("400.00"));
        testStatistics.put("paymentRate", new BigDecimal("80.00"));
    }

    @Test
    void testCreateBill_Success() throws Exception {
        // 准备测试数据
        Map<String, Object> request = new HashMap<>();
        request.put("dormitoryId", "D001");
        request.put("billMonth", "2024-12");
        request.put("electricityUsage", 100.00);
        request.put("unitPrice", 0.5);

        Map<String, Object> serviceResult = new HashMap<>();
        serviceResult.put("success", true);
        serviceResult.put("message", "电费账单创建成功");
        serviceResult.put("billId", 1);

        // 模拟服务调用
        when(electricityService.createElectricityBill(anyString(), anyString(), any(BigDecimal.class), any(BigDecimal.class)))
                .thenReturn(serviceResult);

        // 执行测试
        mockMvc.perform(post("/electricity/bills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.success").value(true))
                .andExpect(jsonPath("$.data.message").value("电费账单创建成功"))
                .andExpect(jsonPath("$.data.billId").value(1));

        // 验证服务调用
        verify(electricityService).createElectricityBill(anyString(), anyString(), any(BigDecimal.class), any(BigDecimal.class));
    }

    @Test
    void testCreateBill_Failure() throws Exception {
        // 准备测试数据
        Map<String, Object> request = new HashMap<>();
        request.put("dormitoryId", "D001");
        request.put("billMonth", "2024-12");
        request.put("electricityUsage", 100.00);
        request.put("unitPrice", 0.5);

        Map<String, Object> serviceResult = new HashMap<>();
        serviceResult.put("success", false);
        serviceResult.put("message", "该月份账单已存在");

        // 模拟服务调用
        when(electricityService.createElectricityBill(anyString(), anyString(), any(BigDecimal.class), any(BigDecimal.class)))
                .thenReturn(serviceResult);

        // 执行测试
        mockMvc.perform(post("/electricity/bills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("该月份账单已存在"));

        // 验证服务调用
        verify(electricityService).createElectricityBill(anyString(), anyString(), any(BigDecimal.class), any(BigDecimal.class));
    }

    @Test
    void testGetDormitoryBills_Success() throws Exception {
        // 准备测试数据
        String dormitoryId = "D001";
        List<ElectricityBill> expectedBills = Arrays.asList(testBill);

        // 模拟服务调用
        when(electricityService.getDormitoryBills(dormitoryId)).thenReturn(expectedBills);

        // 执行测试
        mockMvc.perform(get("/electricity/bills/dormitory/{dormitoryId}", dormitoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].dormitoryId").value("D001"))
                .andExpect(jsonPath("$.data[0].billMonth").value("2024-12"))
                .andExpect(jsonPath("$.data[0].totalAmount").value(50.00));

        // 验证服务调用
        verify(electricityService).getDormitoryBills(dormitoryId);
    }

    @Test
    void testGetStudentBills_Success() throws Exception {
        // 准备测试数据
        String studentId = "S001";
        List<ElectricityBill> expectedBills = Arrays.asList(testBill);

        // 模拟服务调用
        when(electricityService.getStudentBills(studentId)).thenReturn(expectedBills);

        // 执行测试
        mockMvc.perform(get("/electricity/bills/student")
                        .param("studentId", studentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].dormitoryId").value("D001"));

        // 验证服务调用
        verify(electricityService).getStudentBills(studentId);
    }

    @Test
    void testSetReminder_Success() throws Exception {
        // 准备测试数据
        Map<String, Object> request = new HashMap<>();
        request.put("studentId", "S001");
        request.put("balanceThreshold", 50.00);
        request.put("dueDateReminderDays", 3);
        request.put("reminderMethod", "站内信");

        Map<String, Object> serviceResult = new HashMap<>();
        serviceResult.put("success", true);
        serviceResult.put("message", "电费提醒设置成功");

        // 模拟服务调用
        when(electricityService.setElectricityReminder(anyString(), any(BigDecimal.class), anyInt(), anyString()))
                .thenReturn(serviceResult);

        // 执行测试
        mockMvc.perform(post("/electricity/reminders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.success").value(true))
                .andExpect(jsonPath("$.data.message").value("电费提醒设置成功"));

        // 验证服务调用
        verify(electricityService).setElectricityReminder(anyString(), any(BigDecimal.class), anyInt(), anyString());
    }

    @Test
    void testGetReminderSettings_Success() throws Exception {
        // 准备测试数据
        String studentId = "S001";

        // 模拟服务调用
        when(electricityService.getStudentReminderSettings(studentId)).thenReturn(testReminder);

        // 执行测试
        mockMvc.perform(get("/electricity/reminders/{studentId}", studentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.studentId").value("S001"))
                .andExpect(jsonPath("$.data.dormitoryId").value("D001"))
                .andExpect(jsonPath("$.data.balanceThreshold").value(50.00))
                .andExpect(jsonPath("$.data.reminderMethod").value("站内信"));

        // 验证服务调用
        verify(electricityService).getStudentReminderSettings(studentId);
    }

    @Test
    void testPayBill_Success() throws Exception {
        // 准备测试数据
        Map<String, Object> request = new HashMap<>();
        request.put("billId", 1);
        request.put("studentId", "S001");
        request.put("paymentAmount", 50.00);
        request.put("paymentMethod", "支付宝");

        Map<String, Object> serviceResult = new HashMap<>();
        serviceResult.put("success", true);
        serviceResult.put("message", "缴费成功");
        serviceResult.put("paymentId", 1);
        serviceResult.put("transactionId", "TXN123456");

        // 模拟服务调用
        when(electricityService.payElectricityBill(anyLong(), anyString(), any(BigDecimal.class), anyString()))
                .thenReturn(serviceResult);

        // 执行测试
        mockMvc.perform(post("/electricity/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.success").value(true))
                .andExpect(jsonPath("$.data.message").value("缴费成功"))
                .andExpect(jsonPath("$.data.paymentId").value(1))
                .andExpect(jsonPath("$.data.transactionId").value("TXN123456"));

        // 验证服务调用
        verify(electricityService).payElectricityBill(anyLong(), anyString(), any(BigDecimal.class), anyString());
    }

    @Test
    void testGetPaymentHistory_Success() throws Exception {
        // 准备测试数据
        String studentId = "S001";
        List<ElectricityPayment> expectedPayments = Arrays.asList(testPayment);

        // 模拟服务调用
        when(electricityService.getPaymentHistory(studentId)).thenReturn(expectedPayments);

        // 执行测试
        mockMvc.perform(get("/electricity/payments/{studentId}", studentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].studentId").value("S001"))
                .andExpect(jsonPath("$.data[0].paymentAmount").value(50.00))
                .andExpect(jsonPath("$.data[0].paymentMethod").value("支付宝"));

        // 验证服务调用
        verify(electricityService).getPaymentHistory(studentId);
    }

    @Test
    void testGetStatistics_Success() throws Exception {
        // 模拟服务调用
        when(electricityService.getElectricityStatistics()).thenReturn(testStatistics);

        // 执行测试
        mockMvc.perform(get("/electricity/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalBills").value(10))
                .andExpect(jsonPath("$.data.unpaidBills").value(3))
                .andExpect(jsonPath("$.data.overdueBills").value(1))
                .andExpect(jsonPath("$.data.monthlyTotal").value(500.00))
                .andExpect(jsonPath("$.data.monthlyPaid").value(400.00))
                .andExpect(jsonPath("$.data.paymentRate").value(80.00));

        // 验证服务调用
        verify(electricityService).getElectricityStatistics();
    }

    @Test
    void testCheckBalanceReminders_Success() throws Exception {
        // 执行测试
        mockMvc.perform(post("/electricity/reminders/check-balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("余额提醒检查完成"));

        // 验证服务调用
        verify(electricityService).checkBalanceReminders();
    }

    @Test
    void testCheckDueDateReminders_Success() throws Exception {
        // 执行测试
        mockMvc.perform(post("/electricity/reminders/check-due-date"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("截止日期提醒检查完成"));

        // 验证服务调用
        verify(electricityService).checkDueDateReminders();
    }
}

