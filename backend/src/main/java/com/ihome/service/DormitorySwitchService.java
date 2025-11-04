package com.ihome.service;

import com.ihome.entity.*;
import com.ihome.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 宿舍调换服务类
 * 处理宿舍调换申请和审核流程
 */
@Service
public class DormitorySwitchService {

    @Autowired
    private DormitorySwitchMapper switchMapper;
    
    @Autowired
    private StudentMapper studentMapper;
    
    @Autowired
    private DormitoryAllocationMapper allocationMapper;
    
    @Autowired
    private BedMapper bedMapper;
    
    @Autowired
    private NotificationService notificationService;

    /**
     * 学生提交调换申请
     */
    @Transactional
    public Map<String, Object> submitSwitchRequest(DormitorySwitch switchRequest) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 1. 验证申请人是否存在
            Student applicant = studentMapper.selectById(switchRequest.getApplicantId());
            if (applicant == null) {
                result.put("success", false);
                result.put("message", "申请人不存在");
                return result;
            }
            
            // 2. 验证申请人当前是否有住宿分配
            DormitoryAllocation currentAllocation = allocationMapper.selectByStudentId(switchRequest.getApplicantId());
            if (currentAllocation == null || !"在住".equals(currentAllocation.getStatus())) {
                result.put("success", false);
                result.put("message", "申请人当前没有有效的住宿分配");
                return result;
            }
            
            // 3. 设置当前床位ID
            switchRequest.setCurrentBedId(currentAllocation.getBedId());
            
            // 4. 如果指定了目标学生，验证目标学生是否存在且有空床位
            if (switchRequest.getTargetStudentId() != null && !switchRequest.getTargetStudentId().trim().isEmpty()) {
                Student targetStudent = studentMapper.selectById(switchRequest.getTargetStudentId());
                if (targetStudent == null) {
                    result.put("success", false);
                    result.put("message", "目标学生不存在");
                    return result;
                }
                
                // 验证目标学生是否有住宿分配
                DormitoryAllocation targetAllocation = allocationMapper.selectByStudentId(switchRequest.getTargetStudentId());
                if (targetAllocation == null || !"在住".equals(targetAllocation.getStatus())) {
                    result.put("success", false);
                    result.put("message", "目标学生当前没有有效的住宿分配");
                    return result;
                }
                
                // 检查是否已有待审核的调换申请
                List<DormitorySwitch> existingSwitches = switchMapper.selectByTargetStudentId(switchRequest.getTargetStudentId());
                if (!existingSwitches.isEmpty()) {
                    result.put("success", false);
                    result.put("message", "目标学生已有待审核的调换申请");
                    return result;
                }
            }
            
            // 5. 设置申请状态和时间
            switchRequest.setStatus("待审核");
            switchRequest.setApplyTime(LocalDateTime.now());
            
            // 6. 保存申请
            switchMapper.insert(switchRequest);
            
            // 7. 发送通知给管理员
            String title = "新的宿舍调换申请";
            String content = String.format("学生 %s 提交了宿舍调换申请，请及时审核。", switchRequest.getApplicantId());
            notificationService.sendSwitchNotification(
                    switchRequest.getId().toString(),
                    "admin", // 发送给所有管理员
                    "admin",
                    title,
                    content,
                    "high"
            );
            
            result.put("success", true);
            result.put("message", "调换申请提交成功");
            result.put("switchId", switchRequest.getId());
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "提交申请失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 管理员审核调换申请
     */
    @Transactional
    public Map<String, Object> reviewSwitchRequest(Integer switchId, String status, String reviewerId, String reviewComment) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 1. 获取调换申请
            DormitorySwitch switchRequest = switchMapper.selectById(switchId);
            if (switchRequest == null) {
                result.put("success", false);
                result.put("message", "调换申请不存在");
                return result;
            }
            
            // 2. 验证申请状态
            if (!"待审核".equals(switchRequest.getStatus())) {
                result.put("success", false);
                result.put("message", "该申请已被处理");
                return result;
            }
            
            // 3. 更新申请状态
            switchRequest.setStatus(status);
            switchRequest.setReviewTime(LocalDateTime.now());
            switchRequest.setReviewerId(reviewerId);
            switchRequest.setReviewComment(reviewComment);
            
            switchMapper.updateById(switchRequest);
            
            // 4. 如果审核通过，执行调换
            if ("已通过".equals(status)) {
                Map<String, Object> switchResult = executeSwitch(switchRequest);
                if (!(Boolean) switchResult.get("success")) {
                    // 调换执行失败，回滚申请状态
                    switchRequest.setStatus("待审核");
                    switchRequest.setReviewTime(null);
                    switchRequest.setReviewerId(null);
                    switchRequest.setReviewComment(reviewComment + " (调换执行失败)");
                    switchMapper.updateById(switchRequest);
                    
                    result.put("success", false);
                    result.put("message", "审核通过但调换执行失败: " + switchResult.get("message"));
                    return result;
                }
                
                // 调换成功，更新完成时间
                switchRequest.setCompleteTime(LocalDateTime.now());
                switchMapper.updateById(switchRequest);
            }
            
            // 5. 发送审核结果通知给学生
            String title = "宿舍调换申请审核结果";
            String content = String.format("您的宿舍调换申请审核结果：%s", status);
            if (reviewComment != null && !reviewComment.trim().isEmpty()) {
                content += String.format("，审核意见：%s", reviewComment);
            }
            String priority = "已通过".equals(status) ? "normal" : "high";
            notificationService.sendSwitchNotification(
                    switchRequest.getId().toString(),
                    switchRequest.getApplicantId(),
                    "student",
                    title,
                    content,
                    priority
            );
            
            result.put("success", true);
            result.put("message", "审核完成");
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "审核失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 执行宿舍调换
     */
    @Transactional
    public Map<String, Object> executeSwitch(DormitorySwitch switchRequest) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 1. 获取申请人当前分配
            DormitoryAllocation applicantAllocation = allocationMapper.selectByStudentId(switchRequest.getApplicantId());
            if (applicantAllocation == null) {
                result.put("success", false);
                result.put("message", "申请人当前分配不存在");
                return result;
            }
            
            // 2. 如果指定了目标学生，执行双方调换
            if (switchRequest.getTargetStudentId() != null && !switchRequest.getTargetStudentId().trim().isEmpty()) {
                DormitoryAllocation targetAllocation = allocationMapper.selectByStudentId(switchRequest.getTargetStudentId());
                if (targetAllocation == null) {
                    result.put("success", false);
                    result.put("message", "目标学生当前分配不存在");
                    return result;
                }
                
                // 执行双方调换
                String tempBedId = applicantAllocation.getBedId();
                applicantAllocation.setBedId(targetAllocation.getBedId());
                targetAllocation.setBedId(tempBedId);
                
                allocationMapper.updateById(applicantAllocation);
                allocationMapper.updateById(targetAllocation);
                
                result.put("success", true);
                result.put("message", "双方调换完成");
                
            } else if (switchRequest.getTargetBedId() != null && !switchRequest.getTargetBedId().trim().isEmpty()) {
                // 3. 如果指定了目标床位，执行床位调换
                Bed targetBed = bedMapper.selectById(switchRequest.getTargetBedId());
                if (targetBed == null) {
                    result.put("success", false);
                    result.put("message", "目标床位不存在");
                    return result;
                }
                
                if (!"可用".equals(targetBed.getStatus())) {
                    result.put("success", false);
                    result.put("message", "目标床位不可用");
                    return result;
                }
                
                // 更新申请人分配
                applicantAllocation.setBedId(switchRequest.getTargetBedId());
                allocationMapper.updateById(applicantAllocation);
                
                // 更新床位状态
                Bed currentBed = bedMapper.selectById(switchRequest.getCurrentBedId());
                currentBed.setStatus("可用");
                targetBed.setStatus("已占用");
                
                bedMapper.updateById(currentBed);
                bedMapper.updateById(targetBed);
                
                result.put("success", true);
                result.put("message", "床位调换完成");
            } else {
                result.put("success", false);
                result.put("message", "调换目标不明确");
            }
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "调换执行失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 获取学生的调换申请列表
     */
    public List<DormitorySwitch> getStudentSwitchRequests(String studentId) {
        return switchMapper.selectByApplicantId(studentId);
    }

    /**
     * 获取待审核的调换申请列表
     */
    public List<DormitorySwitch> getPendingSwitchRequests() {
        return switchMapper.selectPendingSwitches();
    }

    /**
     * 获取所有调换申请（管理员用）
     */
    public List<DormitorySwitch> getAllSwitchRequests() {
        return switchMapper.selectList(null);
    }

    /**
     * 取消调换申请
     */
    @Transactional
    public Map<String, Object> cancelSwitchRequest(Integer switchId, String studentId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            DormitorySwitch switchRequest = switchMapper.selectById(switchId);
            if (switchRequest == null) {
                result.put("success", false);
                result.put("message", "调换申请不存在");
                return result;
            }
            
            if (!switchRequest.getApplicantId().equals(studentId)) {
                result.put("success", false);
                result.put("message", "无权限取消此申请");
                return result;
            }
            
            if (!"待审核".equals(switchRequest.getStatus())) {
                result.put("success", false);
                result.put("message", "只能取消待审核的申请");
                return result;
            }
            
            switchRequest.setStatus("已取消");
            switchRequest.setReviewTime(LocalDateTime.now());
            switchRequest.setReviewComment("学生主动取消");
            
            switchMapper.updateById(switchRequest);
            
            result.put("success", true);
            result.put("message", "申请已取消");
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "取消失败: " + e.getMessage());
        }
        
        return result;
    }
}
