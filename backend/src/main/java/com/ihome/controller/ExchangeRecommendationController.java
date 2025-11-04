package com.ihome.controller;

import com.ihome.common.ApiResponse;
import com.ihome.entity.DormitoryAllocation;
import com.ihome.entity.QuestionnaireAnswer;
import com.ihome.entity.RoommateTag;
import com.ihome.entity.Student;
import com.ihome.mapper.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 调换推荐控制器
 * 处理宿舍调换推荐相关接口
 */
@RestController
@RequestMapping("/exchange/recommendations")
@Tag(name = "调换推荐", description = "宿舍调换推荐相关API接口")
public class ExchangeRecommendationController {

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private DormitoryAllocationMapper allocationMapper;

    @Autowired
    private QuestionnaireAnswerMapper questionnaireMapper;

    @Autowired
    private RoommateTagMapper roommateTagMapper;

    @Autowired
    private BedMapper bedMapper;

    /**
     * 获取调换推荐列表
     */
    @GetMapping("/for-student")
    @Operation(summary = "获取调换推荐列表", description = "获取基于匹配度算法的室友推荐列表")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<List<Map<String, Object>>> getRecommendations() {
        try {
            String studentId = SecurityContextHolder.getContext().getAuthentication().getName();

            // 获取学生当前宿舍信息
            var currentAllocation = getCurrentAllocation(studentId);
            if (currentAllocation == null) {
                return ApiResponse.error("您还没有分配宿舍");
            }

            // 获取室友列表
            List<String> roommateIds = getRoommateIds(currentAllocation.getBedId());

            // 获取所有其他学生（排除自己和室友）
            List<Student> allStudents = studentMapper.selectList(null);
            List<Student> candidateStudents = allStudents.stream()
                .filter(s -> !s.getId().equals(studentId) && !roommateIds.contains(s.getId()))
                .collect(Collectors.toList());

            // 计算匹配度并生成推荐
            List<Map<String, Object>> recommendations = new ArrayList<>();

            for (Student candidate : candidateStudents) {
                double compatibilityScore = calculateCompatibility(studentId, candidate.getId());
                
                if (compatibilityScore > 0.5) { // 只推荐匹配度超过50%的
                    Map<String, Object> recommendation = new HashMap<>();
                    recommendation.put("targetStudentId", candidate.getId());
                    recommendation.put("targetStudentName", candidate.getName());
                    recommendation.put("targetStudentMajor", candidate.getMajor());
                    recommendation.put("targetStudentCollege", candidate.getCollege());
                    recommendation.put("compatibilityScore", compatibilityScore);
                    recommendation.put("recommendationReason", generateRecommendationReason(candidate.getId(), compatibilityScore));
                    
                    recommendations.add(recommendation);
                }
            }

            // 按匹配度降序排序
            recommendations.sort((a, b) -> Double.compare(
                (Double) b.get("compatibilityScore"),
                (Double) a.get("compatibilityScore")
            ));

            // 限制返回前10个推荐
            if (recommendations.size() > 10) {
                recommendations = recommendations.subList(0, 10);
            }

            return ApiResponse.ok(recommendations);
        } catch (Exception e) {
            return ApiResponse.error("获取调换推荐失败: " + e.getMessage());
        }
    }

    /**
     * 获取匹配度详情
     */
    @GetMapping("/compatibility/{targetStudentId}")
    @Operation(summary = "获取匹配度详情", description = "获取与目标学生的详细匹配度分析")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<Map<String, Object>> getCompatibilityDetails(@PathVariable @Parameter(description = "目标学生学号") String targetStudentId) {
        try {
            String studentId = SecurityContextHolder.getContext().getAuthentication().getName();

            // 获取学生信息
            Student student1 = studentMapper.selectById(studentId);
            Student student2 = studentMapper.selectById(targetStudentId);

            if (student1 == null || student2 == null) {
                return ApiResponse.error("学生不存在");
            }

            // 计算各项匹配度
            double questionnaireCompatibility = calculateQuestionnaireCompatibility(studentId, targetStudentId);
            double tagCompatibility = calculateTagCompatibility(studentId, targetStudentId);
            double majorCompatibility = calculateMajorCompatibility(student1, student2);
            double overallScore = (questionnaireCompatibility * 0.4 + tagCompatibility * 0.3 + majorCompatibility * 0.3);

            Map<String, Object> details = new HashMap<>();
            details.put("overallScore", overallScore);
            
            Map<String, Object> compatibilityDetails = new HashMap<>();
            compatibilityDetails.put("questionnaireCompatibility", questionnaireCompatibility);
            compatibilityDetails.put("tagCompatibility", tagCompatibility);
            compatibilityDetails.put("majorCompatibility", majorCompatibility);
            details.put("details", compatibilityDetails);
            
            Map<String, Object> student1Info = new HashMap<>();
            student1Info.put("id", student1.getId());
            student1Info.put("name", student1.getName());
            student1Info.put("major", student1.getMajor());
            student1Info.put("college", student1.getCollege());
            details.put("student1", student1Info);
            
            Map<String, Object> student2Info = new HashMap<>();
            student2Info.put("id", student2.getId());
            student2Info.put("name", student2.getName());
            student2Info.put("major", student2.getMajor());
            student2Info.put("college", student2.getCollege());
            details.put("student2", student2Info);

            return ApiResponse.ok(details);
        } catch (Exception e) {
            return ApiResponse.error("获取匹配度详情失败: " + e.getMessage());
        }
    }

    /**
     * 接受推荐
     */
    @PostMapping("/accept/{targetStudentId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<?> acceptRecommendation(@PathVariable String targetStudentId) {
        try {
            // 这里应该创建一个调换申请
            // 暂时返回成功消息
            return ApiResponse.ok("推荐已接受，调换申请已提交");
        } catch (Exception e) {
            return ApiResponse.error("接受推荐失败: " + e.getMessage());
        }
    }

    /**
     * 拒绝推荐
     */
    @PostMapping("/reject/{targetStudentId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<?> rejectRecommendation(@PathVariable String targetStudentId) {
        try {
            // 记录拒绝推荐
            return ApiResponse.ok("推荐已拒绝");
        } catch (Exception e) {
            return ApiResponse.error("拒绝推荐失败: " + e.getMessage());
        }
    }

    /**
     * 计算综合匹配度
     */
    private double calculateCompatibility(String studentId1, String studentId2) {
        Student student1 = studentMapper.selectById(studentId1);
        Student student2 = studentMapper.selectById(studentId2);

        double questionnaireCompatibility = calculateQuestionnaireCompatibility(studentId1, studentId2);
        double tagCompatibility = calculateTagCompatibility(studentId1, studentId2);
        double majorCompatibility = calculateMajorCompatibility(student1, student2);

        // 加权平均
        return questionnaireCompatibility * 0.4 + tagCompatibility * 0.3 + majorCompatibility * 0.3;
    }

    /**
     * 计算问卷匹配度
     */
    private double calculateQuestionnaireCompatibility(String studentId1, String studentId2) {
        QuestionnaireAnswer qa1 = questionnaireMapper.selectByStudentId(studentId1);
        QuestionnaireAnswer qa2 = questionnaireMapper.selectByStudentId(studentId2);

        if (qa1 == null || qa2 == null) {
            return 0.5; // 没有问卷时返回中等匹配度
        }

        double score = 0.0;
        int factors = 0;

        // 作息时间匹配
        if (qa1.getSleepTimePreference() != null && qa2.getSleepTimePreference() != null) {
            if (qa1.getSleepTimePreference().equals(qa2.getSleepTimePreference())) {
                score += 1.0;
            }
            factors++;
        }

        // 卫生习惯匹配
        if (qa1.getCleanlinessLevel() != null && qa2.getCleanlinessLevel() != null) {
            if (qa1.getCleanlinessLevel().equals(qa2.getCleanlinessLevel())) {
                score += 1.0;
            }
            factors++;
        }

        // 噪音容忍度匹配
        if (qa1.getNoiseTolerance() != null && qa2.getNoiseTolerance() != null) {
            if (qa1.getNoiseTolerance().equals(qa2.getNoiseTolerance())) {
                score += 1.0;
            }
            factors++;
        }

        return factors > 0 ? score / factors : 0.5;
    }

    /**
     * 计算标签匹配度
     */
    private double calculateTagCompatibility(String studentId1, String studentId2) {
        List<RoommateTag> tags1 = roommateTagMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<RoommateTag>()
                .eq("student_id", studentId1)
        );

        List<RoommateTag> tags2 = roommateTagMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<RoommateTag>()
                .eq("student_id", studentId2)
        );

        if (tags1.isEmpty() || tags2.isEmpty()) {
            return 0.5;
        }

        Set<String> tagNames1 = tags1.stream().map(RoommateTag::getTagName).collect(Collectors.toSet());
        Set<String> tagNames2 = tags2.stream().map(RoommateTag::getTagName).collect(Collectors.toSet());

        long commonTags = tagNames1.stream().filter(tagNames2::contains).count();
        long totalTags = tagNames1.size() + tagNames2.size() - commonTags;

        return totalTags > 0 ? (double) commonTags / totalTags : 0.5;
    }

    /**
     * 计算专业匹配度
     */
    private double calculateMajorCompatibility(Student student1, Student student2) {
        if (student1.getMajor() != null && student2.getMajor() != null) {
            return student1.getMajor().equals(student2.getMajor()) ? 1.0 : 0.7;
        }
        return 0.5;
    }

    /**
     * 生成推荐理由
     */
    private String generateRecommendationReason(String studentId2, double score) {
        StringBuilder reason = new StringBuilder();
        
        String studentId = SecurityContextHolder.getContext().getAuthentication().getName();
        Student student1 = studentMapper.selectById(studentId);
        Student student2 = studentMapper.selectById(studentId2);

        if (student1 != null && student2 != null && student1.getMajor() != null && student2.getMajor() != null && student1.getMajor().equals(student2.getMajor())) {
            reason.append("同专业学生；");
        }

        if (score > 0.8) {
            reason.append("生活习惯高度匹配");
        } else if (score > 0.6) {
            reason.append("生活习惯匹配");
        } else {
            reason.append("生活习惯较为匹配");
        }

        return reason.toString();
    }

    /**
     * 获取当前宿舍分配
     */
    private DormitoryAllocation getCurrentAllocation(String studentId) {
        var allocations = allocationMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<DormitoryAllocation>()
                .eq("student_id", studentId)
                .eq("status", "在住")
                .orderByDesc("check_in_date")
                .last("LIMIT 1")
        );

        return allocations.isEmpty() ? null : allocations.get(0);
    }

    /**
     * 获取室友列表
     */
    private List<String> getRoommateIds(String bedId) {
        var bed = bedMapper.selectById(bedId);
        if (bed == null) {
            return new ArrayList<>();
        }

        // 通过bedId查找所有室友
        var allocations = allocationMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<DormitoryAllocation>()
                .eq("bed_id", bedId)
                .eq("status", "在住")
        );

        return allocations.stream()
            .map(DormitoryAllocation::getStudentId)
            .collect(Collectors.toList());
    }
}

