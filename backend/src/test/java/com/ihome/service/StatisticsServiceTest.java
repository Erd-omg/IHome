package com.ihome.service;

import com.ihome.entity.*;
import com.ihome.mapper.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 统计服务测试
 */
@ExtendWith(MockitoExtension.class)
public class StatisticsServiceTest {

    @Mock
    private StudentMapper studentMapper;

    @Mock
    private DormitoryMapper dormitoryMapper;

    @Mock
    private BedMapper bedMapper;

    @Mock
    private DormitoryAllocationMapper allocationMapper;

    @Mock
    private PaymentRecordMapper paymentMapper;

    @Mock
    private RepairOrderMapper repairMapper;

    @Mock
    private DormitorySwitchMapper switchMapper;

    @InjectMocks
    private StatisticsService statisticsService;

    @BeforeEach
    void setUp() {
        // 测试前准备
    }

    @Test
    void testGetDashboardStatistics_Success() {
        // 模拟数据库查询
        when(studentMapper.selectCount(null)).thenReturn(100L);
        when(allocationMapper.selectCount(null)).thenReturn(80L);
        when(dormitoryMapper.selectCount(null)).thenReturn(50L);
        when(bedMapper.selectCount(null)).thenReturn(200L);
        when(bedMapper.selectList(null)).thenReturn(Arrays.asList());
        when(paymentMapper.selectCount(null)).thenReturn(150L);
        when(repairMapper.selectCount(null)).thenReturn(30L);
        when(repairMapper.selectList(null)).thenReturn(Arrays.asList());
        when(switchMapper.selectCount(null)).thenReturn(10L);
        when(switchMapper.selectList(null)).thenReturn(Arrays.asList());

        // 执行测试
        Map<String, Object> result = statisticsService.getDashboardStatistics();

        // 验证结果
        assertNotNull(result);
        assertTrue(result.containsKey("totalStudents"));
        assertTrue(result.containsKey("totalDormitories"));
        assertTrue(result.containsKey("totalBeds"));
        assertEquals(100L, result.get("totalStudents"));

        // 验证调用
        verify(studentMapper).selectCount(null);
        verify(dormitoryMapper).selectCount(null);
    }

    @Test
    void testGetStudentDistributionStatistics_Success() {
        // 准备测试数据
        Student student1 = new Student();
        student1.setCollege("计算机学院");
        student1.setMajor("计算机科学");
        Student student2 = new Student();
        student2.setCollege("软件学院");
        student2.setMajor("软件工程");

        // 模拟数据库查询
        when(studentMapper.selectList(null)).thenReturn(Arrays.asList(student1, student2));

        // 执行测试
        Map<String, Object> result = statisticsService.getStudentDistributionStatistics();

        // 验证结果
        assertNotNull(result);
        assertTrue(result.containsKey("collegeDistribution"));
        assertTrue(result.containsKey("majorDistribution"));

        // 验证调用
        verify(studentMapper).selectList(null);
    }

    @Test
    void testGetDormitoryUsageStatistics_Success() {
        // 准备测试数据
        Dormitory dorm1 = new Dormitory();
        dorm1.setBedCount(4);
        dorm1.setCurrentOccupancy(3);
        Dormitory dorm2 = new Dormitory();
        dorm2.setBedCount(4);
        dorm2.setCurrentOccupancy(2);

        // 模拟数据库查询
        when(dormitoryMapper.selectList(null)).thenReturn(Arrays.asList(dorm1, dorm2));
        when(bedMapper.selectList(null)).thenReturn(Arrays.asList());

        // 执行测试
        Map<String, Object> result = statisticsService.getDormitoryUsageStatistics();

        // 验证结果
        assertNotNull(result);
        assertTrue(result.containsKey("buildingStatistics"));
        // 注意：totalDormitories, totalBeds, occupancyRate 在 buildingStatistics 内部

        // 验证调用
        verify(dormitoryMapper).selectList(null);
    }

    @Test
    void testGetPaymentStatistics_Success() {
        // 准备测试数据
        PaymentRecord payment = new PaymentRecord();
        payment.setAmount(java.math.BigDecimal.valueOf(100.00));

        // 模拟数据库查询
        when(paymentMapper.selectList(null)).thenReturn(Arrays.asList(payment));

        // 执行测试
        Map<String, Object> result = statisticsService.getPaymentStatistics(null, null);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.containsKey("totalAmount"));
        assertTrue(result.containsKey("totalPayments"));

        // 验证调用
        verify(paymentMapper).selectList(null);
    }

    @Test
    void testGetRepairStatistics_Success() {
        // 准备测试数据
        RepairOrder repair = new RepairOrder();
        repair.setStatus("已完成");

        // 模拟数据库查询
        when(repairMapper.selectList(null)).thenReturn(Arrays.asList(repair));

        // 执行测试
        Map<String, Object> result = statisticsService.getRepairStatistics(null, null);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.containsKey("totalRepairs"));

        // 验证调用
        verify(repairMapper).selectList(null);
    }
}

