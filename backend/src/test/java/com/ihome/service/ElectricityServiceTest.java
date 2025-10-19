package com.ihome.service;

import com.ihome.entity.*;
import com.ihome.mapper.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 电费服务测试
 */
@ExtendWith(MockitoExtension.class)
public class ElectricityServiceTest {

    @Mock
    private ElectricityBillMapper billMapper;
    
    @Mock
    private ElectricityReminderMapper reminderMapper;
    
    @Mock
    private ElectricityPaymentMapper paymentMapper;
    
    @Mock
    private StudentMapper studentMapper;
    
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ElectricityService electricityService;

    private ElectricityBill testBill;
    private ElectricityReminder testReminder;
    private Student testStudent;

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

        // 创建测试学生
        testStudent = new Student();
        testStudent.setId("S001");
        testStudent.setName("张三");
        testStudent.setGender("男");
        testStudent.setMajor("计算机科学");
        testStudent.setStatus("在校");
        testStudent.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void testCreateElectricityBill_Success() {
        // 准备测试数据
        String dormitoryId = "D001";
        String billMonth = "2024-12";
        BigDecimal electricityUsage = new BigDecimal("100.00");
        BigDecimal unitPrice = new BigDecimal("0.5");

        // 模拟数据库查询
        when(billMapper.selectByBillMonth(billMonth)).thenReturn(new ArrayList<>());
        when(billMapper.insert(any(ElectricityBill.class))).thenReturn(1);

        // 执行测试
        Map<String, Object> result = electricityService.createElectricityBill(
                dormitoryId, billMonth, electricityUsage, unitPrice);

        // 验证结果
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        assertEquals("电费账单创建成功", result.get("message"));
        assertNotNull(result.get("billId"));

        // 验证数据库操作
        verify(billMapper).insert(any(ElectricityBill.class));
    }

    @Test
    void testCreateElectricityBill_BillExists() {
        // 准备测试数据
        String dormitoryId = "D001";
        String billMonth = "2024-12";
        BigDecimal electricityUsage = new BigDecimal("100.00");
        BigDecimal unitPrice = new BigDecimal("0.5");

        // 模拟已存在账单
        List<ElectricityBill> existingBills = Arrays.asList(testBill);
        when(billMapper.selectByBillMonth(billMonth)).thenReturn(existingBills);

        // 执行测试
        Map<String, Object> result = electricityService.createElectricityBill(
                dormitoryId, billMonth, electricityUsage, unitPrice);

        // 验证结果
        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
        assertEquals("该月份账单已存在", result.get("message"));

        // 验证没有插入新账单
        verify(billMapper, never()).insert(any(ElectricityBill.class));
    }

    @Test
    void testGetDormitoryBills_Success() {
        // 准备测试数据
        String dormitoryId = "D001";
        List<ElectricityBill> expectedBills = Arrays.asList(testBill);

        // 模拟数据库查询
        when(billMapper.selectByDormitoryId(dormitoryId)).thenReturn(expectedBills);

        // 执行测试
        List<ElectricityBill> result = electricityService.getDormitoryBills(dormitoryId);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(dormitoryId, result.get(0).getDormitoryId());

        // 验证数据库操作
        verify(billMapper).selectByDormitoryId(dormitoryId);
    }

    @Test
    void testSetElectricityReminder_Success() {
        // 准备测试数据
        String studentId = "S001";
        BigDecimal balanceThreshold = new BigDecimal("50.00");
        Integer dueDateReminderDays = 3;
        String reminderMethod = "站内信";

        // 模拟数据库查询
        when(reminderMapper.selectByStudentId(studentId)).thenReturn(null);
        when(reminderMapper.insert(any(ElectricityReminder.class))).thenReturn(1);

        // 执行测试
        Map<String, Object> result = electricityService.setElectricityReminder(
                studentId, balanceThreshold, dueDateReminderDays, reminderMethod);

        // 验证结果
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        assertEquals("电费提醒设置成功", result.get("message"));

        // 验证数据库操作
        verify(reminderMapper).insert(any(ElectricityReminder.class));
    }

    @Test
    void testSetElectricityReminder_UpdateExisting() {
        // 准备测试数据
        String studentId = "S001";
        BigDecimal balanceThreshold = new BigDecimal("100.00");
        Integer dueDateReminderDays = 5;
        String reminderMethod = "短信";

        // 模拟已存在提醒设置
        when(reminderMapper.selectByStudentId(studentId)).thenReturn(testReminder);
        when(reminderMapper.updateById(any(ElectricityReminder.class))).thenReturn(1);

        // 执行测试
        Map<String, Object> result = electricityService.setElectricityReminder(
                studentId, balanceThreshold, dueDateReminderDays, reminderMethod);

        // 验证结果
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        assertEquals("电费提醒设置成功", result.get("message"));

        // 验证数据库操作
        verify(reminderMapper).updateById(any(ElectricityReminder.class));
        verify(reminderMapper, never()).insert(any(ElectricityReminder.class));
    }

    @Test
    void testGetStudentReminderSettings_Success() {
        // 准备测试数据
        String studentId = "S001";

        // 模拟数据库查询
        when(reminderMapper.selectByStudentId(studentId)).thenReturn(testReminder);

        // 执行测试
        ElectricityReminder result = electricityService.getStudentReminderSettings(studentId);

        // 验证结果
        assertNotNull(result);
        assertEquals(studentId, result.getStudentId());
        assertEquals("D001", result.getDormitoryId());

        // 验证数据库操作
        verify(reminderMapper).selectByStudentId(studentId);
    }

    @Test
    void testPayElectricityBill_Success() {
        // 准备测试数据
        Long billId = 1L;
        String studentId = "S001";
        BigDecimal paymentAmount = new BigDecimal("50.00");
        String paymentMethod = "支付宝";

        // 模拟数据库查询
        when(billMapper.selectById(billId)).thenReturn(testBill);
        when(paymentMapper.insert(any(ElectricityPayment.class))).thenReturn(1);
        when(billMapper.updateById(any(ElectricityBill.class))).thenReturn(1);

        // 执行测试
        Map<String, Object> result = electricityService.payElectricityBill(
                billId, studentId, paymentAmount, paymentMethod);

        // 验证结果
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        assertEquals("缴费成功", result.get("message"));
        assertNotNull(result.get("paymentId"));
        assertNotNull(result.get("transactionId"));

        // 验证数据库操作
        verify(paymentMapper).insert(any(ElectricityPayment.class));
        verify(billMapper).updateById(any(ElectricityBill.class));
    }

    @Test
    void testPayElectricityBill_BillNotFound() {
        // 准备测试数据
        Long billId = 999L;
        String studentId = "S001";
        BigDecimal paymentAmount = new BigDecimal("50.00");
        String paymentMethod = "支付宝";

        // 模拟账单不存在
        when(billMapper.selectById(billId)).thenReturn(null);

        // 执行测试
        Map<String, Object> result = electricityService.payElectricityBill(
                billId, studentId, paymentAmount, paymentMethod);

        // 验证结果
        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
        assertEquals("账单不存在", result.get("message"));

        // 验证没有进行数据库操作
        verify(paymentMapper, never()).insert(any(ElectricityPayment.class));
        verify(billMapper, never()).updateById(any(ElectricityBill.class));
    }

    @Test
    void testPayElectricityBill_InsufficientAmount() {
        // 准备测试数据
        Long billId = 1L;
        String studentId = "S001";
        BigDecimal paymentAmount = new BigDecimal("30.00"); // 少于账单金额
        String paymentMethod = "支付宝";

        // 模拟数据库查询
        when(billMapper.selectById(billId)).thenReturn(testBill);

        // 执行测试
        Map<String, Object> result = electricityService.payElectricityBill(
                billId, studentId, paymentAmount, paymentMethod);

        // 验证结果
        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
        assertEquals("缴费金额不足", result.get("message"));

        // 验证没有进行数据库操作
        verify(paymentMapper, never()).insert(any(ElectricityPayment.class));
        verify(billMapper, never()).updateById(any(ElectricityBill.class));
    }

    @Test
    void testGetPaymentHistory_Success() {
        // 准备测试数据
        String studentId = "S001";
        List<ElectricityPayment> expectedPayments = Arrays.asList(createTestPayment());

        // 模拟数据库查询
        when(paymentMapper.selectByStudentId(studentId)).thenReturn(expectedPayments);

        // 执行测试
        List<ElectricityPayment> result = electricityService.getPaymentHistory(studentId);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(studentId, result.get(0).getStudentId());

        // 验证数据库操作
        verify(paymentMapper).selectByStudentId(studentId);
    }

    @Test
    void testGetElectricityStatistics_Success() {
        // 准备测试数据
        List<ElectricityBill> allBills = Arrays.asList(testBill, createTestBill("D002", "已缴费"));

        // 模拟数据库查询
        when(billMapper.selectList(null)).thenReturn(allBills);

        // 执行测试
        Map<String, Object> result = electricityService.getElectricityStatistics();

        // 验证结果
        assertNotNull(result);
        assertTrue(result.containsKey("totalBills"));
        assertTrue(result.containsKey("unpaidBills"));
        assertTrue(result.containsKey("overdueBills"));
        assertTrue(result.containsKey("monthlyTotal"));
        assertTrue(result.containsKey("monthlyPaid"));
        assertTrue(result.containsKey("paymentRate"));

        assertEquals(2, result.get("totalBills"));
        assertEquals(1L, result.get("unpaidBills"));

        // 验证数据库操作
        verify(billMapper).selectList(null);
    }

    private ElectricityPayment createTestPayment() {
        ElectricityPayment payment = new ElectricityPayment();
        payment.setId(1L);
        payment.setBillId(1L);
        payment.setStudentId("S001");
        payment.setDormitoryId("D001");
        payment.setPaymentAmount(new BigDecimal("50.00"));
        payment.setPaymentMethod("支付宝");
        payment.setPaymentStatus("已支付");
        payment.setTransactionId("TXN123456");
        payment.setPaymentTime(LocalDateTime.now());
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());
        return payment;
    }

    private ElectricityBill createTestBill(String dormitoryId, String status) {
        ElectricityBill bill = new ElectricityBill();
        bill.setId(2L);
        bill.setDormitoryId(dormitoryId);
        bill.setBillMonth("2024-12");
        bill.setElectricityUsage(new BigDecimal("80.00"));
        bill.setUnitPrice(new BigDecimal("0.5"));
        bill.setTotalAmount(new BigDecimal("40.00"));
        bill.setCurrentBalance(new BigDecimal("40.00"));
        bill.setStatus(status);
        bill.setDueDate(LocalDateTime.now().plusDays(30));
        bill.setCreatedAt(LocalDateTime.now());
        bill.setUpdatedAt(LocalDateTime.now());
        return bill;
    }
}

