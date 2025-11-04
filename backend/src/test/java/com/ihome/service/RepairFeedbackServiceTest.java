package com.ihome.service;

import com.ihome.entity.RepairFeedback;
import com.ihome.entity.RepairOrder;
import com.ihome.mapper.RepairFeedbackMapper;
import com.ihome.mapper.RepairOrderMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 维修反馈服务测试
 */
@ExtendWith(MockitoExtension.class)
public class RepairFeedbackServiceTest {

    @Mock
    private RepairFeedbackMapper repairFeedbackMapper;

    @Mock
    private RepairOrderMapper repairOrderMapper;

    @InjectMocks
    private RepairFeedbackService repairFeedbackService;

    private RepairFeedback testFeedback;
    private RepairOrder testOrder;

    @BeforeEach
    void setUp() {
        setupTestData();
    }

    private void setupTestData() {
        testOrder = new RepairOrder();
        testOrder.setId(1);
        testOrder.setStatus("已完成");
        testOrder.setStudentId("2024001");

        testFeedback = new RepairFeedback();
        testFeedback.setId(1);
        testFeedback.setStudentId("2024001");
        testFeedback.setRepairOrderId(1);
        testFeedback.setRating(5);
        testFeedback.setContent("维修服务很好");
    }

    @Test
    void testSubmitFeedback_Success() {
        // 模拟数据库查询
        when(repairOrderMapper.selectById(1)).thenReturn(testOrder);
        when(repairFeedbackMapper.selectByRepairOrderId(1)).thenReturn(null);
        when(repairFeedbackMapper.insert(any(RepairFeedback.class))).thenReturn(1);

        // 执行测试
        assertDoesNotThrow(() -> repairFeedbackService.submitFeedback(testFeedback));

        // 验证调用
        verify(repairOrderMapper).selectById(1);
        verify(repairFeedbackMapper).selectByRepairOrderId(1);
        verify(repairFeedbackMapper).insert(any(RepairFeedback.class));
    }

    @Test
    void testSubmitFeedback_OrderNotFound() {
        // 模拟订单不存在
        when(repairOrderMapper.selectById(1)).thenReturn(null);

        // 执行测试并验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> repairFeedbackService.submitFeedback(testFeedback));
        
        assertEquals("维修订单不存在", exception.getMessage());

        // 验证调用
        verify(repairOrderMapper).selectById(1);
        verify(repairFeedbackMapper, never()).insert(any(RepairFeedback.class));
    }

    @Test
    void testSubmitFeedback_OrderNotCompleted() {
        // 模拟订单未完成
        RepairOrder pendingOrder = new RepairOrder();
        pendingOrder.setId(1);
        pendingOrder.setStatus("待处理");

        when(repairOrderMapper.selectById(1)).thenReturn(pendingOrder);

        // 执行测试并验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> repairFeedbackService.submitFeedback(testFeedback));
        
        assertEquals("维修订单尚未完成，无法提交反馈", exception.getMessage());

        // 验证调用
        verify(repairOrderMapper).selectById(1);
        verify(repairFeedbackMapper, never()).insert(any(RepairFeedback.class));
    }

    @Test
    void testSubmitFeedback_AlreadySubmitted() {
        // 模拟已提交过反馈
        when(repairOrderMapper.selectById(1)).thenReturn(testOrder);
        when(repairFeedbackMapper.selectByRepairOrderId(1)).thenReturn(testFeedback);

        // 执行测试并验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> repairFeedbackService.submitFeedback(testFeedback));
        
        assertEquals("该维修订单已经提交过反馈", exception.getMessage());

        // 验证调用
        verify(repairOrderMapper).selectById(1);
        verify(repairFeedbackMapper).selectByRepairOrderId(1);
        verify(repairFeedbackMapper, never()).insert(any(RepairFeedback.class));
    }

    @Test
    void testSubmitFeedback_InvalidRating() {
        // 准备测试数据（评分超出范围）
        testFeedback.setRating(6);

        // 模拟数据库查询
        when(repairOrderMapper.selectById(1)).thenReturn(testOrder);
        when(repairFeedbackMapper.selectByRepairOrderId(1)).thenReturn(null);

        // 执行测试并验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> repairFeedbackService.submitFeedback(testFeedback));
        
        assertEquals("评分必须在1-5之间", exception.getMessage());

        // 验证调用
        verify(repairOrderMapper).selectById(1);
        verify(repairFeedbackMapper, never()).insert(any(RepairFeedback.class));
    }

    @Test
    void testGetFeedbackByStudentId_Success() {
        // 准备测试数据
        String studentId = "2024001";
        List<RepairFeedback> feedbacks = Arrays.asList(testFeedback);

        // 模拟数据库查询
        when(repairFeedbackMapper.selectByStudentId(studentId)).thenReturn(feedbacks);

        // 执行测试
        List<RepairFeedback> result = repairFeedbackService.getFeedbackByStudentId(studentId);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(studentId, result.get(0).getStudentId());

        // 验证调用
        verify(repairFeedbackMapper).selectByStudentId(studentId);
    }

    @Test
    void testGetFeedbackById_Success() {
        // 模拟数据库查询
        when(repairFeedbackMapper.selectById(1)).thenReturn(testFeedback);

        // 执行测试
        RepairFeedback result = repairFeedbackService.getFeedbackById(1);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getId());

        // 验证调用
        verify(repairFeedbackMapper).selectById(1);
    }

    @Test
    void testReplyFeedback_Success() {
        // 模拟数据库查询和更新
        when(repairFeedbackMapper.selectById(1)).thenReturn(testFeedback);
        when(repairFeedbackMapper.updateById(any(RepairFeedback.class))).thenReturn(1);

        // 执行测试
        assertDoesNotThrow(() -> repairFeedbackService.replyFeedback(1, "已收到反馈"));

        // 验证调用
        verify(repairFeedbackMapper).selectById(1);
        verify(repairFeedbackMapper).updateById(any(RepairFeedback.class));
    }

    @Test
    void testReplyFeedback_NotFound() {
        // 模拟反馈不存在
        when(repairFeedbackMapper.selectById(1)).thenReturn(null);

        // 执行测试并验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> repairFeedbackService.replyFeedback(1, "已收到反馈"));
        
        assertEquals("反馈记录不存在", exception.getMessage());

        // 验证调用
        verify(repairFeedbackMapper).selectById(1);
        verify(repairFeedbackMapper, never()).updateById(any(RepairFeedback.class));
    }
}

