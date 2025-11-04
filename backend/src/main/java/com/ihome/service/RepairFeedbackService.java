package com.ihome.service;

import com.ihome.entity.RepairFeedback;
import com.ihome.entity.RepairOrder;
import com.ihome.mapper.RepairFeedbackMapper;
import com.ihome.mapper.RepairOrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RepairFeedbackService {

    @Autowired
    private RepairFeedbackMapper repairFeedbackMapper;

    @Autowired
    private RepairOrderMapper repairOrderMapper;

    /**
     * 提交维修反馈
     */
    @Transactional
    public void submitFeedback(RepairFeedback feedback) {
        // 验证维修订单是否存在且已完成
        RepairOrder repairOrder = repairOrderMapper.selectById(feedback.getRepairOrderId());
        if (repairOrder == null) {
            throw new RuntimeException("维修订单不存在");
        }

        if (!"已完成".equals(repairOrder.getStatus())) {
            throw new RuntimeException("维修订单尚未完成，无法提交反馈");
        }

        // 检查是否已经提交过反馈
        RepairFeedback existingFeedback = repairFeedbackMapper.selectByRepairOrderId(feedback.getRepairOrderId());
        if (existingFeedback != null) {
            throw new RuntimeException("该维修订单已经提交过反馈");
        }

        // 验证评分范围
        if (feedback.getRating() < 1 || feedback.getRating() > 5) {
            throw new RuntimeException("评分必须在1-5之间");
        }

        // 设置创建时间
        feedback.setCreatedAt(LocalDateTime.now());
        feedback.setUpdatedAt(LocalDateTime.now());

        // 插入反馈记录
        repairFeedbackMapper.insert(feedback);
    }

    /**
     * 根据学生ID获取反馈列表
     */
    public List<RepairFeedback> getFeedbackByStudentId(String studentId) {
        return repairFeedbackMapper.selectByStudentId(studentId);
    }

    /**
     * 根据ID获取反馈详情
     */
    public RepairFeedback getFeedbackById(Integer feedbackId) {
        return repairFeedbackMapper.selectById(feedbackId);
    }

    /**
     * 获取所有反馈（管理员）
     */
    public List<RepairFeedback> getAllFeedback(int page, int size, String repairOrderId, String studentId, Integer rating) {
        return repairFeedbackMapper.selectAllFeedback(page, size, repairOrderId, studentId, rating);
    }

    /**
     * 管理员回复反馈
     */
    @Transactional
    public void replyFeedback(Integer feedbackId, String reply) {
        RepairFeedback feedback = repairFeedbackMapper.selectById(feedbackId);
        if (feedback == null) {
            throw new RuntimeException("反馈记录不存在");
        }

        feedback.setReply(reply);
        feedback.setUpdatedAt(LocalDateTime.now());
        repairFeedbackMapper.updateById(feedback);
    }

    /**
     * 获取反馈统计信息
     */
    public Map<String, Object> getFeedbackStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        // 总反馈数
        int totalFeedbacks = repairFeedbackMapper.countTotalFeedbacks();
        statistics.put("totalFeedbacks", totalFeedbacks);

        // 平均评分
        Double averageRating = repairFeedbackMapper.getAverageRating();
        statistics.put("averageRating", averageRating != null ? averageRating : 0.0);

        // 各评分分布
        Map<Integer, Integer> ratingDistribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            int count = repairFeedbackMapper.countByRating(i);
            ratingDistribution.put(i, count);
        }
        statistics.put("ratingDistribution", ratingDistribution);

        // 有回复的反馈数
        int repliedFeedbacks = repairFeedbackMapper.countRepliedFeedbacks();
        statistics.put("repliedFeedbacks", repliedFeedbacks);

        // 回复率
        double replyRate = totalFeedbacks > 0 ? (double) repliedFeedbacks / totalFeedbacks * 100 : 0;
        statistics.put("replyRate", replyRate);

        return statistics;
    }
}

