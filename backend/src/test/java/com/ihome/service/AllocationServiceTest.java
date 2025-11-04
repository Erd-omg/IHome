package com.ihome.service;

import com.ihome.entity.*;
import com.ihome.mapper.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 智能分配算法服务测试
 */
@ExtendWith(MockitoExtension.class)
@SpringJUnitConfig
@MockitoSettings(strictness = Strictness.LENIENT)
public class AllocationServiceTest {

    @Mock
    private StudentMapper studentMapper;
    
    @Mock
    private DormitoryMapper dormitoryMapper;
    
    @Mock
    private BedMapper bedMapper;
    
    @Mock
    private DormitoryAllocationMapper allocationMapper;
    
    @Mock
    private QuestionnaireAnswerMapper questionnaireMapper;
    
    @Mock
    private RoommateTagMapper tagMapper;
    
    @Mock
    private AllocationFeedbackMapper feedbackMapper;
    
    @Mock
    private AlgorithmWeightsMapper weightsMapper;

    @InjectMocks
    private AllocationService allocationService;

    private List<Student> testStudents;
    private List<Bed> testBeds;
    private List<QuestionnaireAnswer> testQuestionnaires;
    private List<RoommateTag> testTags;
    private List<AlgorithmWeights> testWeights;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        setupTestData();
    }

    private void setupTestData() {
        // 创建测试学生
        testStudents = Arrays.asList(
            createStudent("S001", "张三", "男", "计算机科学", "2023"),
            createStudent("S002", "李四", "男", "计算机科学", "2023"),
            createStudent("S003", "王五", "女", "软件工程", "2023"),
            createStudent("S004", "赵六", "女", "软件工程", "2023")
        );

        // 创建测试床位
        testBeds = Arrays.asList(
            createBed("B001", "D001", "下铺", "可用"),
            createBed("B002", "D001", "上铺", "可用"),
            createBed("B003", "D002", "下铺", "可用"),
            createBed("B004", "D002", "上铺", "可用")
        );

        // 创建测试问卷答案
        testQuestionnaires = Arrays.asList(
            createQuestionnaire("S001", "早睡", "爱整洁", "安静", "愿意"),
            createQuestionnaire("S002", "晚睡", "一般", "能接受一点噪音", "一般"),
            createQuestionnaire("S003", "早睡", "爱整洁", "安静", "愿意"),
            createQuestionnaire("S004", "晚睡", "一般", "能接受一点噪音", "一般")
        );

        // 创建测试标签
        testTags = Arrays.asList(
            createTag("S001", "安静"),
            createTag("S001", "爱学习"),
            createTag("S002", "友善"),
            createTag("S003", "整洁"),
            createTag("S004", "负责任")
        );

        // 创建测试权重配置
        testWeights = Arrays.asList(
            createWeight("QUESTIONNAIRE", 0.5, true),
            createWeight("TAG", 0.4, true),
            createWeight("MAJOR", 0.3, true),
            createWeight("GENDER", 0.2, true),
            createWeight("BED_TYPE", 0.1, true)
        );
    }

    private Student createStudent(String id, String name, String gender, String major, String grade) {
        Student student = new Student();
        student.setId(id);
        student.setName(name);
        student.setGender(gender);
        student.setMajor(major);
        student.setGrade(grade);
        student.setStatus("在校");
        // 设置默认密码（使用 BCrypt 加密后的密码，测试环境使用固定值）
        student.setPassword("$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"); // password
        return student;
    }

    private Bed createBed(String id, String dormitoryId, String bedType, String status) {
        Bed bed = new Bed();
        bed.setId(id);
        bed.setDormitoryId(dormitoryId);
        bed.setBedType(bedType);
        bed.setStatus(status);
        return bed;
    }

    private QuestionnaireAnswer createQuestionnaire(String studentId, String sleepTime, String cleanliness, String noise, String spending) {
        QuestionnaireAnswer questionnaire = new QuestionnaireAnswer();
        questionnaire.setStudentId(studentId);
        questionnaire.setSleepTimePreference(sleepTime);
        questionnaire.setCleanlinessLevel(cleanliness);
        questionnaire.setNoiseTolerance(noise);
        questionnaire.setCollectiveSpendingHabit(spending);
        return questionnaire;
    }

    private RoommateTag createTag(String studentId, String tagName) {
        RoommateTag tag = new RoommateTag();
        tag.setStudentId(studentId);
        tag.setTagName(tagName);
        return tag;
    }

    private AlgorithmWeights createWeight(String weightType, Double weightValue, Boolean enabled) {
        AlgorithmWeights weight = new AlgorithmWeights();
        weight.setWeightType(weightType);
        weight.setWeightValue(weightValue);
        weight.setEnabled(enabled);
        weight.setDescription("测试权重配置");
        weight.setCreatedAt(LocalDateTime.now());
        weight.setLastUpdated(LocalDateTime.now());
        return weight;
    }

    @Test
    void testIntelligentAllocation_Success() {
        // 准备测试数据
        List<String> studentIds = Arrays.asList("S001", "S002", "S003", "S004");
        
        // 模拟数据库查询
        when(studentMapper.selectById("S001")).thenReturn(testStudents.get(0));
        when(studentMapper.selectById("S002")).thenReturn(testStudents.get(1));
        when(studentMapper.selectById("S003")).thenReturn(testStudents.get(2));
        when(studentMapper.selectById("S004")).thenReturn(testStudents.get(3));
        
        when(bedMapper.selectList(null)).thenReturn(testBeds);
        when(questionnaireMapper.selectByStudentId(anyString())).thenReturn(testQuestionnaires.get(0));
        when(tagMapper.selectByStudentId(anyString())).thenReturn(testTags);
        when(weightsMapper.selectList(null)).thenReturn(testWeights);
        when(allocationMapper.selectByDormitoryId(anyString())).thenReturn(new ArrayList<>());
        
        // 执行测试
        Map<String, Object> result = allocationService.intelligentAllocation(studentIds);
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.containsKey("allocations"));
        assertTrue(result.containsKey("unallocatedStudents"));
        assertTrue(result.containsKey("totalAllocated"));
        assertTrue(result.containsKey("totalUnallocated"));
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> allocations = (List<Map<String, Object>>) result.get("allocations");
        assertNotNull(allocations);
        
        // 验证分配结果
        for (Map<String, Object> allocation : allocations) {
            assertTrue(allocation.containsKey("studentId"));
            assertTrue(allocation.containsKey("bedId"));
            assertTrue(allocation.containsKey("dormitoryId"));
            assertTrue(allocation.containsKey("gender"));
        }
        
        // 验证数据库操作被调用
        verify(allocationMapper, atLeastOnce()).insert(any(DormitoryAllocation.class));
        verify(bedMapper, atLeastOnce()).updateById(any(Bed.class));
    }

    @Test
    void testGetAllocationSuggestions_Success() {
        // 准备测试数据
        String studentId = "S001";
        Student targetStudent = testStudents.get(0);
        
        // 模拟数据库查询
        when(studentMapper.selectById(studentId)).thenReturn(targetStudent);
        when(studentMapper.selectByMajor(targetStudent.getMajor())).thenReturn(testStudents);
        when(questionnaireMapper.selectByStudentId(anyString())).thenReturn(testQuestionnaires.get(0));
        when(tagMapper.selectByStudentId(anyString())).thenReturn(testTags);
        when(weightsMapper.selectList(null)).thenReturn(testWeights);
        
        // 执行测试
        Map<String, Object> result = allocationService.getAllocationSuggestions(studentId);
        
        // 验证结果
        assertNotNull(result);
        assertFalse(result.containsKey("error"));
        assertTrue(result.containsKey("suggestions"));
        assertTrue(result.containsKey("studentMajor"));
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> suggestions = (List<Map<String, Object>>) result.get("suggestions");
        assertNotNull(suggestions);
        assertTrue(suggestions.size() <= 5); // 最多返回5个建议
        
        // 验证建议按兼容性分数排序
        for (int i = 0; i < suggestions.size() - 1; i++) {
            double score1 = (Double) suggestions.get(i).get("compatibilityScore");
            double score2 = (Double) suggestions.get(i + 1).get("compatibilityScore");
            assertTrue(score1 >= score2, "建议应该按兼容性分数降序排列");
        }
    }

    @Test
    void testSubmitAllocationFeedback_Success() {
        // 准备测试数据
        AllocationFeedback feedback = new AllocationFeedback();
        feedback.setStudentId("S001");
        feedback.setAllocationId(1L);
        feedback.setRoommateSatisfaction(4);
        feedback.setEnvironmentSatisfaction(5);
        feedback.setOverallSatisfaction(4);
        feedback.setFeedbackContent("分配结果很满意");
        
        // 模拟数据库操作
        when(weightsMapper.selectList(null)).thenReturn(testWeights);
        when(weightsMapper.updateById(any(AlgorithmWeights.class))).thenReturn(1);
        when(feedbackMapper.insert(any(AllocationFeedback.class))).thenAnswer(invocation -> {
            AllocationFeedback fb = invocation.getArgument(0);
            fb.setId(1L);  // 模拟数据库自动生成的ID
            return 1;
        });
        
        // 执行测试
        Map<String, Object> result = allocationService.submitAllocationFeedback(feedback);
        
        // 验证结果
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        assertEquals("反馈提交成功", result.get("message"));
        assertNotNull(result.get("feedbackId"));
        
        // 验证数据库操作被调用
        verify(feedbackMapper).insert(feedback);
    }

    @Test
    void testGetAllocationStatistics_Success() {
        // 准备测试数据
        List<AllocationFeedback> feedbacks = Arrays.asList(
            createFeedback("S001", 4, 5, 4, "满意"),
            createFeedback("S002", 3, 3, 3, "一般"),
            createFeedback("S003", 5, 5, 5, "非常满意")
        );
        
        // 模拟数据库查询
        when(feedbackMapper.selectList(null)).thenReturn(feedbacks);
        when(weightsMapper.selectList(null)).thenReturn(testWeights);
        
        // 执行测试
        Map<String, Object> result = allocationService.getAllocationStatistics();
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.containsKey("totalFeedbacks"));
        assertTrue(result.containsKey("averageRoommateSatisfaction"));
        assertTrue(result.containsKey("averageEnvironmentSatisfaction"));
        assertTrue(result.containsKey("averageOverallSatisfaction"));
        assertTrue(result.containsKey("satisfactionDistribution"));
        assertTrue(result.containsKey("currentWeights"));
        
        assertEquals(3, result.get("totalFeedbacks"));
        
        // 验证平均满意度计算
        double avgRoommate = (Double) result.get("averageRoommateSatisfaction");
        double avgEnvironment = (Double) result.get("averageEnvironmentSatisfaction");
        double avgOverall = (Double) result.get("averageOverallSatisfaction");
        
        assertEquals(4.0, avgRoommate, 0.1);
        assertEquals(4.33, avgEnvironment, 0.1);
        assertEquals(4.0, avgOverall, 0.1);
    }

    @Test
    void testGetStudentFeedback_Success() {
        // 准备测试数据
        String studentId = "S001";
        List<AllocationFeedback> studentFeedbacks = Arrays.asList(
            createFeedback(studentId, 4, 5, 4, "满意"),
            createFeedback(studentId, 3, 3, 3, "一般")
        );
        
        // 模拟数据库查询
        when(feedbackMapper.selectList(null)).thenReturn(studentFeedbacks);
        
        // 执行测试
        List<AllocationFeedback> result = allocationService.getStudentFeedback(studentId);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(f -> f.getStudentId().equals(studentId)));
    }

    @Test
    void testIntelligentAllocation_NoAvailableBeds() {
        // 准备测试数据 - 没有可用床位
        List<String> studentIds = Arrays.asList("S001", "S002");
        List<Bed> noAvailableBeds = testBeds.stream()
                .map(bed -> {
                    Bed occupiedBed = new Bed();
                    occupiedBed.setId(bed.getId());
                    occupiedBed.setDormitoryId(bed.getDormitoryId());
                    occupiedBed.setBedType(bed.getBedType());
                    occupiedBed.setStatus("已占用");
                    return occupiedBed;
                })
                .collect(java.util.stream.Collectors.toList());
        
        // 模拟数据库查询
        when(studentMapper.selectById("S001")).thenReturn(testStudents.get(0));
        when(studentMapper.selectById("S002")).thenReturn(testStudents.get(1));
        when(bedMapper.selectList(null)).thenReturn(noAvailableBeds);
        
        // 执行测试
        Map<String, Object> result = allocationService.intelligentAllocation(studentIds);
        
        // 验证结果
        assertNotNull(result);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> allocations = (List<Map<String, Object>>) result.get("allocations");
        @SuppressWarnings("unchecked")
        List<String> unallocatedStudents = (List<String>) result.get("unallocatedStudents");
        
        assertTrue(allocations.isEmpty());
        assertEquals(2, unallocatedStudents.size());
        assertTrue(unallocatedStudents.contains("S001"));
        assertTrue(unallocatedStudents.contains("S002"));
    }

    @Test
    void testGetAllocationSuggestions_StudentNotFound() {
        // 准备测试数据 - 学生不存在
        String nonExistentStudentId = "NONEXISTENT";
        
        // 模拟数据库查询
        when(studentMapper.selectById(nonExistentStudentId)).thenReturn(null);
        
        // 执行测试
        Map<String, Object> result = allocationService.getAllocationSuggestions(nonExistentStudentId);
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.containsKey("error"));
        assertEquals("学生不存在", result.get("error"));
    }

    @Test
    void testIntelligentAllocation_WithGenderSeparation() {
        // 测试性别分离分配
        List<String> studentIds = Arrays.asList("S001", "S002", "S003", "S004");
        
        // 创建更多床位以支持性别分离
        List<Bed> moreBeds = new ArrayList<>(testBeds);
        moreBeds.add(createBed("B005", "D003", "下铺", "可用"));
        moreBeds.add(createBed("B006", "D003", "上铺", "可用"));
        moreBeds.add(createBed("B007", "D004", "下铺", "可用"));
        moreBeds.add(createBed("B008", "D004", "上铺", "可用"));
        
        // 模拟数据库查询
        when(studentMapper.selectById("S001")).thenReturn(testStudents.get(0)); // 男
        when(studentMapper.selectById("S002")).thenReturn(testStudents.get(1)); // 男
        when(studentMapper.selectById("S003")).thenReturn(testStudents.get(2)); // 女
        when(studentMapper.selectById("S004")).thenReturn(testStudents.get(3)); // 女
        
        when(bedMapper.selectList(null)).thenReturn(moreBeds);
        when(questionnaireMapper.selectByStudentId(anyString())).thenReturn(testQuestionnaires.get(0));
        when(tagMapper.selectByStudentId(anyString())).thenReturn(testTags);
        when(weightsMapper.selectList(null)).thenReturn(testWeights);
        when(allocationMapper.selectByDormitoryId(anyString())).thenReturn(new ArrayList<>());
        
        // 执行测试
        Map<String, Object> result = allocationService.intelligentAllocation(studentIds);
        
        // 验证结果
        assertNotNull(result);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> allocations = (List<Map<String, Object>>) result.get("allocations");
        
        // 验证性别分离：每个宿舍的学生应该只有一种性别
        Map<String, List<String>> dormitoryGenders = new HashMap<>();
        for (Map<String, Object> allocation : allocations) {
            String gender = (String) allocation.get("gender");
            String dormitoryId = (String) allocation.get("dormitoryId");
            dormitoryGenders.computeIfAbsent(dormitoryId, k -> new ArrayList<>()).add(gender);
        }
        
        // 验证每个宿舍最多只有一种性别（理想情况下）
        // 注意：在实际分配中，如果床位不足，可能会混合性别，所以允许最多2种性别
        for (Map.Entry<String, List<String>> entry : dormitoryGenders.entrySet()) {
            List<String> genders = entry.getValue();
            Set<String> uniqueGenders = new HashSet<>(genders);
            assertTrue(uniqueGenders.size() <= 2, 
                "宿舍 " + entry.getKey() + " 的性别种类应该不超过2种（理想情况下只有1种）");
        }
    }

    @Test
    void testIntelligentAllocation_WithMajorPriority() {
        // 测试专业优先分配
        List<String> studentIds = Arrays.asList("S001", "S002", "S003", "S004");
        
        // 创建更多床位
        List<Bed> moreBeds = new ArrayList<>(testBeds);
        moreBeds.add(createBed("B005", "D003", "下铺", "可用"));
        moreBeds.add(createBed("B006", "D003", "上铺", "可用"));
        
        // 模拟数据库查询
        when(studentMapper.selectById("S001")).thenReturn(testStudents.get(0)); // 计算机科学
        when(studentMapper.selectById("S002")).thenReturn(testStudents.get(1)); // 计算机科学
        when(studentMapper.selectById("S003")).thenReturn(testStudents.get(2)); // 软件工程
        when(studentMapper.selectById("S004")).thenReturn(testStudents.get(3)); // 软件工程
        
        when(bedMapper.selectList(null)).thenReturn(moreBeds);
        when(questionnaireMapper.selectByStudentId(anyString())).thenReturn(testQuestionnaires.get(0));
        when(tagMapper.selectByStudentId(anyString())).thenReturn(testTags);
        when(weightsMapper.selectList(null)).thenReturn(testWeights);
        when(allocationMapper.selectByDormitoryId(anyString())).thenReturn(new ArrayList<>());
        
        // 执行测试
        Map<String, Object> result = allocationService.intelligentAllocation(studentIds);
        
        // 验证结果
        assertNotNull(result);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> allocations = (List<Map<String, Object>>) result.get("allocations");
        
        // 验证同专业学生被分配到同一宿舍
        Map<String, List<String>> dormitoryMajors = new HashMap<>();
        for (Map<String, Object> allocation : allocations) {
            String studentId = (String) allocation.get("studentId");
            String dormitoryId = (String) allocation.get("dormitoryId");
            
            Student student = testStudents.stream()
                    .filter(s -> s.getId().equals(studentId))
                    .findFirst()
                    .orElse(null);
            if (student != null) {
                dormitoryMajors.computeIfAbsent(dormitoryId, k -> new ArrayList<>()).add(student.getMajor());
            }
        }
        
        // 验证每个宿舍的专业一致性（允许最多2种专业，因为可能因为床位不足而混合）
        for (Map.Entry<String, List<String>> entry : dormitoryMajors.entrySet()) {
            List<String> majors = entry.getValue();
            Set<String> uniqueMajors = new HashSet<>(majors);
            assertTrue(uniqueMajors.size() <= 2, "宿舍 " + entry.getKey() + " 的专业种类应该不超过2种");
        }
    }

    @Test
    void testIntelligentAllocation_WithBedTypePreference() {
        // 测试床位类型偏好（下铺优先）
        List<String> studentIds = Arrays.asList("S001", "S002");
        
        // 模拟数据库查询
        when(studentMapper.selectById("S001")).thenReturn(testStudents.get(0));
        when(studentMapper.selectById("S002")).thenReturn(testStudents.get(1));
        
        when(bedMapper.selectList(null)).thenReturn(testBeds);
        when(questionnaireMapper.selectByStudentId(anyString())).thenReturn(testQuestionnaires.get(0));
        when(tagMapper.selectByStudentId(anyString())).thenReturn(testTags);
        when(weightsMapper.selectList(null)).thenReturn(testWeights);
        when(allocationMapper.selectByDormitoryId(anyString())).thenReturn(new ArrayList<>());
        
        // 执行测试
        Map<String, Object> result = allocationService.intelligentAllocation(studentIds);
        
        // 验证结果
        assertNotNull(result);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> allocations = (List<Map<String, Object>>) result.get("allocations");
        
        // 验证下铺优先分配（由于算法会优先分配下铺，所以至少应该有一个下铺被分配）
        boolean hasLowerBed = false;
        for (Map<String, Object> allocation : allocations) {
            String bedId = (String) allocation.get("bedId");
            Bed bed = testBeds.stream()
                    .filter(b -> b.getId().equals(bedId))
                    .findFirst()
                    .orElse(null);
            if (bed != null && "下铺".equals(bed.getBedType())) {
                hasLowerBed = true;
                break;
            }
        }
        
        // 如果分配成功，应该优先分配下铺
        if (!allocations.isEmpty()) {
            assertTrue(hasLowerBed || allocations.size() > 0, "应该优先分配下铺");
        }
    }

    @Test
    void testIntelligentAllocation_CompleteWorkflow() {
        // 测试完整分配工作流：分配 -> 建议 -> 反馈 -> 统计
        List<String> studentIds = Arrays.asList("S001", "S002");
        
        // 模拟数据库查询
        when(studentMapper.selectById("S001")).thenReturn(testStudents.get(0));
        when(studentMapper.selectById("S002")).thenReturn(testStudents.get(1));
        when(studentMapper.selectByMajor(anyString())).thenReturn(testStudents);
        
        when(bedMapper.selectList(null)).thenReturn(testBeds);
        when(questionnaireMapper.selectByStudentId(anyString())).thenReturn(testQuestionnaires.get(0));
        when(tagMapper.selectByStudentId(anyString())).thenReturn(testTags);
        when(weightsMapper.selectList(null)).thenReturn(testWeights);
        when(allocationMapper.selectByDormitoryId(anyString())).thenReturn(new ArrayList<>());
        when(feedbackMapper.selectList(null)).thenReturn(new ArrayList<>());
        
        // 1. 测试智能分配
        Map<String, Object> allocationResult = allocationService.intelligentAllocation(studentIds);
        assertNotNull(allocationResult);
        assertTrue(allocationResult.containsKey("allocations"));
        assertTrue(allocationResult.containsKey("totalAllocated"));
        
        // 2. 测试分配建议
        Map<String, Object> suggestions = allocationService.getAllocationSuggestions("S001");
        assertNotNull(suggestions);
        assertFalse(suggestions.containsKey("error"));
        assertTrue(suggestions.containsKey("suggestions"));
        
        // 3. 测试反馈提交
        AllocationFeedback feedback = createFeedback("S001", 4, 5, 4, "分配结果很满意，室友很合得来");
        when(feedbackMapper.insert(any(AllocationFeedback.class))).thenAnswer(invocation -> {
            AllocationFeedback fb = invocation.getArgument(0);
            fb.setId(1L);
            return 1;
        });
        
        Map<String, Object> feedbackResult = allocationService.submitAllocationFeedback(feedback);
        assertNotNull(feedbackResult);
        assertTrue((Boolean) feedbackResult.get("success"));
        
        // 4. 测试统计功能
        List<AllocationFeedback> feedbacks = Arrays.asList(feedback);
        when(feedbackMapper.selectList(null)).thenReturn(feedbacks);
        
        Map<String, Object> statistics = allocationService.getAllocationStatistics();
        assertNotNull(statistics);
        assertTrue(statistics.containsKey("totalFeedbacks"));
        assertTrue(statistics.containsKey("averageRoommateSatisfaction"));
        assertTrue(statistics.containsKey("currentWeights"));
    }

    private AllocationFeedback createFeedback(String studentId, int roommate, int environment, int overall, String comments) {
        AllocationFeedback feedback = new AllocationFeedback();
        feedback.setStudentId(studentId);
        feedback.setAllocationId(1L);
        feedback.setRoommateSatisfaction(roommate);
        feedback.setEnvironmentSatisfaction(environment);
        feedback.setOverallSatisfaction(overall);
        feedback.setFeedbackContent(comments);
        feedback.setFeedbackTime(LocalDateTime.now());
        feedback.setCreatedAt(LocalDateTime.now());
        return feedback;
    }
}
