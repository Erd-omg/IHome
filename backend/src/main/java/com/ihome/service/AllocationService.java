package com.ihome.service;

import com.ihome.entity.*;
import com.ihome.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 智能分配算法服务
 * 实现基于问卷匹配、专业优先、下铺优先的宿舍分配算法
 */
@Service
public class AllocationService {

    @Autowired
    private StudentMapper studentMapper;
    
    @Autowired
    private DormitoryMapper dormitoryMapper;
    
    @Autowired
    private BedMapper bedMapper;
    
    @Autowired
    private DormitoryAllocationMapper allocationMapper;
    
    @Autowired
    private QuestionnaireAnswerMapper questionnaireMapper;
    
    @Autowired
    private RoommateTagMapper tagMapper;
    
    @Autowired
    private AllocationFeedbackMapper feedbackMapper;
    
    @Autowired
    private AlgorithmWeightsMapper weightsMapper;

    /**
     * 智能分配算法
     * @param studentIds 待分配的学生ID列表
     * @return 分配结果
     */
    @Transactional
    public Map<String, Object> intelligentAllocation(List<String> studentIds) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> allocations = new ArrayList<>();
        List<String> unallocatedStudents = new ArrayList<>();

        // 1. 获取所有待分配学生信息
        List<Student> students = studentIds.stream()
                .map(studentMapper::selectById)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 2. 获取所有可用床位
        List<Bed> availableBeds = bedMapper.selectList(null).stream()
                .filter(bed -> "可用".equals(bed.getStatus()))
                .collect(Collectors.toList());

        // 3. 按专业和性别分组学生
        Map<String, Map<String, List<Student>>> studentsByMajorAndGender = students.stream()
                .collect(Collectors.groupingBy(Student::getMajor,
                        Collectors.groupingBy(Student::getGender)));

        // 4. 为每个专业和性别的学生进行分配
        for (Map.Entry<String, Map<String, List<Student>>> majorEntry : studentsByMajorAndGender.entrySet()) {
            String major = majorEntry.getKey();
            Map<String, List<Student>> genderGroups = majorEntry.getValue();
            
            for (Map.Entry<String, List<Student>> genderEntry : genderGroups.entrySet()) {
                String gender = genderEntry.getKey();
                List<Student> genderStudents = genderEntry.getValue();
                
                // 按问卷匹配度排序
                List<Student> sortedStudents = sortStudentsByCompatibility(genderStudents);
                
                // 分配床位（确保同性别分配）
                List<Map<String, Object>> genderAllocations = allocateStudentsToBedsByGender(sortedStudents, availableBeds, gender);
                allocations.addAll(genderAllocations);
            }
        }

        // 5. 记录未分配的学生
        Set<String> allocatedStudentIds = allocations.stream()
                .map(allocation -> (String) allocation.get("studentId"))
                .collect(Collectors.toSet());
        
        unallocatedStudents = students.stream()
                .map(Student::getId)
                .filter(id -> !allocatedStudentIds.contains(id))
                .collect(Collectors.toList());

        result.put("allocations", allocations);
        result.put("unallocatedStudents", unallocatedStudents);
        result.put("totalAllocated", allocations.size());
        result.put("totalUnallocated", unallocatedStudents.size());

        return result;
    }

    /**
     * 根据兼容性对学生进行排序
     */
    private List<Student> sortStudentsByCompatibility(List<Student> students) {
        return students.stream()
                .sorted((s1, s2) -> {
                    // 计算兼容性分数
                    double score1 = calculateCompatibilityScore(s1, students);
                    double score2 = calculateCompatibilityScore(s2, students);
                    return Double.compare(score2, score1); // 降序排列
                })
                .collect(Collectors.toList());
    }

    /**
     * 计算学生与同专业其他学生的兼容性分数（使用动态权重）
     */
    private double calculateCompatibilityScore(Student student, List<Student> allStudents) {
        List<RoommateTag> tags = tagMapper.selectByStudentId(student.getId());
        
        // 获取动态权重配置
        Map<String, Double> weights = getDynamicWeights();
        
        double score = 0.0;
        
        // 1. 标签匹配度 - 包含问卷自动生成标签和手动选择标签
        score += calculateTagScore(tags) * weights.getOrDefault("TAG", 0.15);
        
        // 2. 专业匹配度 - 次要因素
        score += calculateMajorScore(student, allStudents) * weights.getOrDefault("MAJOR", 0.35);
        
        // 3. 床位类型匹配度
        score += calculateBedTypeScore(student) * weights.getOrDefault("BED_TYPE", 0.20);
        
        // 4. 随机因子 - 避免完全相同的分数
        score += Math.random() * 0.1;
        
        return score;
    }
    
    /**
     * 获取动态权重配置
     */
    private Map<String, Double> getDynamicWeights() {
        Map<String, Double> weights = new HashMap<>();
        
        // 默认权重（更新后的权重分配）
        weights.put("TAG", 0.15);        // 标签匹配度
        weights.put("MAJOR", 0.35);      // 专业匹配度
        weights.put("BED_TYPE", 0.20);   // 床位类型匹配度
        
        // 从数据库获取实际权重配置
        List<AlgorithmWeights> weightConfigs = weightsMapper.selectList(null);
        for (AlgorithmWeights config : weightConfigs) {
            if (config.getEnabled()) {
                weights.put(config.getWeightType(), config.getWeightValue());
            }
        }
        
        return weights;
    }
    
    /**
     * 计算专业匹配度分数
     */
    private double calculateMajorScore(Student student, List<Student> allStudents) {
        long sameMajorCount = allStudents.stream()
                .filter(s -> s.getMajor().equals(student.getMajor()))
                .count();
        
        // 同专业学生越多，匹配度越高
        return Math.min(sameMajorCount / (double) allStudents.size(), 1.0);
    }


    /**
     * 计算标签分数（包含问卷自动生成标签和手动选择标签）
     */
    private double calculateTagScore(List<RoommateTag> tags) {
        if (tags == null || tags.isEmpty()) {
            return 0.5; // 没有标签的中等分数
        }
        
        double score = 0.5; // 基础分数
        
        // 积极标签加分
        Set<String> positiveTags = Set.of(
            "安静", "整洁", "早睡", "适度噪音", 
            "不在宿舍用餐", "集体消费", "爱学习", 
            "友善", "负责任", "守时", "环保", "不吸烟"
        );
        
        // 消极标签减分
        Set<String> negativeTags = Set.of(
            "吵闹", "邋遢", "随意", "晚睡", 
            "作息不规律", "宿舍用餐", "独立消费"
        );
        
        for (RoommateTag tag : tags) {
            if (positiveTags.contains(tag.getTagName())) {
                score += 0.1; // 积极标签加分
            } else if (negativeTags.contains(tag.getTagName())) {
                score -= 0.1; // 消极标签减分
            }
        }
        
        return Math.max(0.0, Math.min(score, 1.0));
    }
    
    /**
     * 计算床位类型匹配度分数
     */
    private double calculateBedTypeScore(Student student) {
        // 这里可以根据学生的床位偏好来计算
        // 暂时返回一个基础分数，可以根据实际需求扩展
        return 0.5; // 基础分数
    }


    /**
     * 将学生按性别分配到床位
     */
    private List<Map<String, Object>> allocateStudentsToBedsByGender(List<Student> students, List<Bed> availableBeds, String gender) {
        List<Map<String, Object>> allocations = new ArrayList<>();
        
        // 按宿舍分组床位，并检查宿舍性别约束
        Map<String, List<Bed>> bedsByDormitory = availableBeds.stream()
                .collect(Collectors.groupingBy(Bed::getDormitoryId));
        
        // 优先分配下铺，确保同性别分配
        for (Map.Entry<String, List<Bed>> entry : bedsByDormitory.entrySet()) {
            String dormitoryId = entry.getKey();
            List<Bed> beds = entry.getValue();
            
            // 检查宿舍是否已有其他性别的学生
            if (!isDormitoryGenderCompatible(dormitoryId, gender)) {
                continue; // 跳过不兼容的宿舍
            }
            
            // 按床位类型排序：下铺优先
            beds.sort((b1, b2) -> {
                if ("下铺".equals(b1.getBedType()) && !"下铺".equals(b2.getBedType())) {
                    return -1;
                } else if (!"下铺".equals(b1.getBedType()) && "下铺".equals(b2.getBedType())) {
                    return 1;
                }
                return 0;
            });
            
            // 为每个宿舍分配学生
            for (int i = 0; i < Math.min(students.size(), beds.size()); i++) {
                Student student = students.get(i);
                Bed bed = beds.get(i);
                
                // 创建分配记录
                DormitoryAllocation allocation = new DormitoryAllocation();
                allocation.setStudentId(student.getId());
                allocation.setBedId(bed.getId());
                allocation.setCheckInDate(java.time.LocalDate.now());
                allocation.setStatus("在住");
                
                allocationMapper.insert(allocation);
                
                // 更新床位状态
                bed.setStatus("已占用");
                bedMapper.updateById(bed);
                
                // 记录分配结果
                Map<String, Object> allocationResult = new HashMap<>();
                allocationResult.put("studentId", student.getId());
                allocationResult.put("studentName", student.getName());
                allocationResult.put("bedId", bed.getId());
                allocationResult.put("dormitoryId", dormitoryId);
                allocationResult.put("bedType", bed.getBedType());
                allocationResult.put("gender", gender);
                allocationResult.put("allocationId", allocation.getId());
                
                allocations.add(allocationResult);
            }
        }
        
        return allocations;
    }

    /**
     * 检查宿舍性别兼容性
     * 确保宿舍中只有同性别学生
     */
    private boolean isDormitoryGenderCompatible(String dormitoryId, String gender) {
        // 查询宿舍中已有的学生分配
        List<DormitoryAllocation> existingAllocations = allocationMapper.selectByDormitoryId(dormitoryId);
        
        if (existingAllocations.isEmpty()) {
            return true; // 空宿舍，可以分配
        }
        
        // 检查已有学生的性别
        for (DormitoryAllocation allocation : existingAllocations) {
            if ("在住".equals(allocation.getStatus())) {
                Student student = studentMapper.selectById(allocation.getStudentId());
                if (student != null && !gender.equals(student.getGender())) {
                    return false; // 发现不同性别的学生
                }
            }
        }
        
        return true; // 所有学生都是同性别
    }

    /**
     * 获取分配建议
     */
    public Map<String, Object> getAllocationSuggestions(String studentId) {
        Map<String, Object> result = new HashMap<>();
        
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            result.put("error", "学生不存在");
            return result;
        }
        
        // 获取同专业同性别学生
        List<Student> sameMajorStudents = studentMapper.selectByMajor(student.getMajor());
        List<Student> sameGenderStudents = sameMajorStudents.stream()
                .filter(s -> s.getGender().equals(student.getGender()))
                .collect(Collectors.toList());
        
        // 计算兼容性
        List<Map<String, Object>> compatibleStudents = sameGenderStudents.stream()
                .filter(s -> !s.getId().equals(studentId))
                .map(s -> {
                    Map<String, Object> suggestion = new HashMap<>();
                    suggestion.put("studentId", s.getId());
                    suggestion.put("studentName", s.getName());
                    suggestion.put("gender", s.getGender());
                    suggestion.put("compatibilityScore", calculateCompatibilityScore(s, sameGenderStudents));
                    return suggestion;
                })
                .sorted((s1, s2) -> Double.compare(
                    (Double) s2.get("compatibilityScore"),
                    (Double) s1.get("compatibilityScore")
                ))
                .limit(5) // 返回前5个最兼容的学生
                .collect(Collectors.toList());
        
        result.put("suggestions", compatibleStudents);
        result.put("studentMajor", student.getMajor());
        
        return result;
    }
    
    /**
     * 提交分配反馈
     */
    @Transactional
    public Map<String, Object> submitAllocationFeedback(AllocationFeedback feedback) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            feedback.setFeedbackTime(LocalDateTime.now());
            feedback.setCreatedAt(LocalDateTime.now());
            feedbackMapper.insert(feedback);
            
            // 根据反馈调整权重
            adjustWeightsBasedOnFeedback(feedback);
            
            result.put("success", true);
            result.put("message", "反馈提交成功");
            result.put("feedbackId", feedback.getId());
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "反馈提交失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 根据反馈调整算法权重
     */
    private void adjustWeightsBasedOnFeedback(AllocationFeedback feedback) {
        // 计算满意度分数
        double satisfactionScore = (feedback.getRoommateSatisfaction() + 
                                  feedback.getEnvironmentSatisfaction() + 
                                  feedback.getOverallSatisfaction()) / 3.0;
        
        // 如果满意度低于3分，降低问卷权重，提高专业权重
        if (satisfactionScore < 3.0) {
            updateWeight("QUESTIONNAIRE", 0.3);
            updateWeight("MAJOR", 0.5);
        } else if (satisfactionScore > 4.0) {
            // 如果满意度高于4分，提高问卷权重
            updateWeight("QUESTIONNAIRE", 0.6);
        }
    }
    
    /**
     * 更新权重配置
     */
    private void updateWeight(String weightType, Double weightValue) {
        AlgorithmWeights weight = weightsMapper.selectList(null).stream()
                .filter(w -> w.getWeightType().equals(weightType))
                .findFirst()
                .orElse(null);
        
        if (weight != null) {
            weight.setWeightValue(weightValue);
            weight.setLastUpdated(LocalDateTime.now());
            weightsMapper.updateById(weight);
        } else {
            // 创建新的权重配置
            weight = new AlgorithmWeights();
            weight.setWeightType(weightType);
            weight.setWeightValue(weightValue);
            weight.setEnabled(true);
            weight.setDescription("动态调整的权重配置");
            weight.setCreatedAt(LocalDateTime.now());
            weight.setLastUpdated(LocalDateTime.now());
            weightsMapper.insert(weight);
        }
    }
    
    /**
     * 获取分配效果统计
     */
    public Map<String, Object> getAllocationStatistics() {
        Map<String, Object> result = new HashMap<>();
        
        // 获取所有反馈
        List<AllocationFeedback> allFeedbacks = feedbackMapper.selectList(null);
        
        if (allFeedbacks.isEmpty()) {
            result.put("totalFeedbacks", 0);
            result.put("averageSatisfaction", 0.0);
            result.put("satisfactionDistribution", Map.of());
            return result;
        }
        
        // 计算平均满意度
        double avgRoommateSatisfaction = allFeedbacks.stream()
                .mapToInt(AllocationFeedback::getRoommateSatisfaction)
                .average().orElse(0.0);
        
        double avgEnvironmentSatisfaction = allFeedbacks.stream()
                .mapToInt(AllocationFeedback::getEnvironmentSatisfaction)
                .average().orElse(0.0);
        
        double avgOverallSatisfaction = allFeedbacks.stream()
                .mapToInt(AllocationFeedback::getOverallSatisfaction)
                .average().orElse(0.0);
        
        // 满意度分布
        Map<String, Long> satisfactionDistribution = allFeedbacks.stream()
                .collect(Collectors.groupingBy(
                    feedback -> {
                        int avg = (feedback.getRoommateSatisfaction() + 
                                 feedback.getEnvironmentSatisfaction() + 
                                 feedback.getOverallSatisfaction()) / 3;
                        return avg + "分";
                    },
                    Collectors.counting()
                ));
        
        result.put("totalFeedbacks", allFeedbacks.size());
        result.put("averageRoommateSatisfaction", avgRoommateSatisfaction);
        result.put("averageEnvironmentSatisfaction", avgEnvironmentSatisfaction);
        result.put("averageOverallSatisfaction", avgOverallSatisfaction);
        result.put("satisfactionDistribution", satisfactionDistribution);
        
        // 当前权重配置
        result.put("currentWeights", getDynamicWeights());
        
        return result;
    }
    
    /**
     * 获取学生分配反馈
     */
    public List<AllocationFeedback> getStudentFeedback(String studentId) {
        return feedbackMapper.selectList(null).stream()
                .filter(feedback -> feedback.getStudentId().equals(studentId))
                .collect(Collectors.toList());
    }
}
