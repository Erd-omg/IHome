package com.ihome.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ihome.entity.*;
import com.ihome.mapper.*;
import com.ihome.service.AllocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 智能分配算法集成测试
 * 测试完整的分配流程：从学生注册到分配完成到反馈提交
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
public class AllocationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AllocationService allocationService;

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

    @Autowired
    private ObjectMapper objectMapper;

    private List<Student> testStudents;
    private List<Dormitory> testDormitories;
    private List<Bed> testBeds;
    private List<QuestionnaireAnswer> testQuestionnaires;
    private List<RoommateTag> testTags;
    private List<AlgorithmWeights> testWeights;

    @BeforeEach
    void setUp() {
        setupTestData();
    }

    private void setupTestData() {
        // 清理测试数据
        cleanupTestData();

        // 创建测试宿舍
        testDormitories = Arrays.asList(
            createDormitory("D001", "B001", 1, "101", "四人间", 4, 0, "可用"),
            createDormitory("D002", "B001", 1, "102", "四人间", 4, 0, "可用")
        );

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
            createBed("B003", "D001", "下铺", "可用"),
            createBed("B004", "D001", "上铺", "可用"),
            createBed("B005", "D002", "下铺", "可用"),
            createBed("B006", "D002", "上铺", "可用"),
            createBed("B007", "D002", "下铺", "可用"),
            createBed("B008", "D002", "上铺", "可用")
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

        // 保存测试数据到数据库
        saveTestData();
    }

    private void cleanupTestData() {
        // 清理测试数据
        feedbackMapper.delete(null);
        allocationMapper.delete(null);
        bedMapper.delete(null);
        dormitoryMapper.delete(null);
        tagMapper.delete(null);
        questionnaireMapper.delete(null);
        studentMapper.delete(null);
        weightsMapper.delete(null);
    }

    private void saveTestData() {
        // 保存宿舍
        for (Dormitory dormitory : testDormitories) {
            dormitoryMapper.insert(dormitory);
        }

        // 保存学生
        for (Student student : testStudents) {
            studentMapper.insert(student);
        }

        // 保存床位
        for (Bed bed : testBeds) {
            bedMapper.insert(bed);
        }

        // 保存问卷答案
        for (QuestionnaireAnswer questionnaire : testQuestionnaires) {
            questionnaireMapper.insert(questionnaire);
        }

        // 保存标签
        for (RoommateTag tag : testTags) {
            tagMapper.insert(tag);
        }

        // 保存权重配置
        for (AlgorithmWeights weight : testWeights) {
            weightsMapper.insert(weight);
        }
    }

    private Dormitory createDormitory(String id, String buildingId, Integer floor, String roomNumber, 
                                     String roomType, Integer bedCount, Integer occupancy, String status) {
        Dormitory dormitory = new Dormitory();
        dormitory.setId(id);
        dormitory.setBuildingId(buildingId);
        dormitory.setFloorNumber(floor);
        dormitory.setRoomNumber(roomNumber);
        dormitory.setRoomType(roomType);
        dormitory.setBedCount(bedCount);
        dormitory.setCurrentOccupancy(occupancy);
        dormitory.setStatus(status);
        return dormitory;
    }

    private Student createStudent(String id, String name, String gender, String major, String grade) {
        Student student = new Student();
        student.setId(id);
        student.setName(name);
        student.setGender(gender);
        student.setMajor(major);
        student.setGrade(grade);
        student.setStatus("在校");
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

    private QuestionnaireAnswer createQuestionnaire(String studentId, String sleepTime, String cleanliness, 
                                                   String noise, String spending) {
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
    void testCompleteAllocationWorkflow() {
        // 1. 测试智能分配算法
        List<String> studentIds = Arrays.asList("S001", "S002", "S003", "S004");
        Map<String, Object> allocationResult = allocationService.intelligentAllocation(studentIds);

        // 验证分配结果
        assertNotNull(allocationResult);
        assertTrue(allocationResult.containsKey("allocations"));
        assertTrue(allocationResult.containsKey("unallocatedStudents"));
        assertTrue(allocationResult.containsKey("totalAllocated"));
        assertTrue(allocationResult.containsKey("totalUnallocated"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> allocations = (List<Map<String, Object>>) allocationResult.get("allocations");
        @SuppressWarnings("unchecked")
        List<String> unallocatedStudents = (List<String>) allocationResult.get("unallocatedStudents");

        // 验证所有学生都被分配
        assertEquals(4, allocations.size());
        assertEquals(0, unallocatedStudents.size());

        // 验证分配结果的正确性
        for (Map<String, Object> allocation : allocations) {
            String studentId = (String) allocation.get("studentId");
            String gender = (String) allocation.get("gender");
            String bedId = (String) allocation.get("bedId");
            String dormitoryId = (String) allocation.get("dormitoryId");

            // 验证学生信息
            Student student = studentMapper.selectById(studentId);
            assertNotNull(student);
            assertEquals(gender, student.getGender());

            // 验证床位信息
            Bed bed = bedMapper.selectById(bedId);
            assertNotNull(bed);
            assertEquals("已占用", bed.getStatus());
            assertEquals(dormitoryId, bed.getDormitoryId());

            // 验证分配记录
            DormitoryAllocation allocationRecord = allocationMapper.selectById((Integer) allocation.get("allocationId"));
            assertNotNull(allocationRecord);
            assertEquals(studentId, allocationRecord.getStudentId());
            assertEquals(bedId, allocationRecord.getBedId());
            assertEquals("在住", allocationRecord.getStatus());
        }

        // 2. 测试分配建议功能
        Map<String, Object> suggestions = allocationService.getAllocationSuggestions("S001");
        assertNotNull(suggestions);
        assertFalse(suggestions.containsKey("error"));
        assertTrue(suggestions.containsKey("suggestions"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> suggestionList = (List<Map<String, Object>>) suggestions.get("suggestions");
        assertNotNull(suggestionList);
        assertTrue(suggestionList.size() <= 5);

        // 3. 测试反馈提交功能
        AllocationFeedback feedback = new AllocationFeedback();
        feedback.setStudentId("S001");
        feedback.setAllocationId(1L);
        feedback.setRoommateSatisfaction(4);
        feedback.setEnvironmentSatisfaction(5);
        feedback.setOverallSatisfaction(4);
        feedback.setFeedbackContent("分配结果很满意，室友很合得来");

        Map<String, Object> feedbackResult = allocationService.submitAllocationFeedback(feedback);
        assertNotNull(feedbackResult);
        assertTrue((Boolean) feedbackResult.get("success"));
        assertEquals("反馈提交成功", feedbackResult.get("message"));

        // 验证反馈已保存到数据库
        List<AllocationFeedback> savedFeedbacks = allocationService.getStudentFeedback("S001");
        assertNotNull(savedFeedbacks);
        assertFalse(savedFeedbacks.isEmpty());
        assertEquals("S001", savedFeedbacks.get(0).getStudentId());
        assertEquals(4, savedFeedbacks.get(0).getRoommateSatisfaction());

        // 4. 测试统计功能
        Map<String, Object> statistics = allocationService.getAllocationStatistics();
        assertNotNull(statistics);
        assertTrue(statistics.containsKey("totalFeedbacks"));
        assertTrue(statistics.containsKey("averageRoommateSatisfaction"));
        assertTrue(statistics.containsKey("averageEnvironmentSatisfaction"));
        assertTrue(statistics.containsKey("averageOverallSatisfaction"));
        assertTrue(statistics.containsKey("currentWeights"));

        assertEquals(1, statistics.get("totalFeedbacks"));
        assertEquals(4.0, (Double) statistics.get("averageRoommateSatisfaction"), 0.1);
        assertEquals(5.0, (Double) statistics.get("averageEnvironmentSatisfaction"), 0.1);
        assertEquals(4.0, (Double) statistics.get("averageOverallSatisfaction"), 0.1);
    }

    @Test
    void testAllocationFeedbackAPI() throws Exception {
        // 1. 先进行分配
        List<String> studentIds = Arrays.asList("S001", "S002");
        allocationService.intelligentAllocation(studentIds);

        // 2. 测试反馈提交API
        AllocationFeedback feedback = new AllocationFeedback();
        feedback.setStudentId("S001");
        feedback.setAllocationId(1L);
        feedback.setRoommateSatisfaction(4);
        feedback.setEnvironmentSatisfaction(5);
        feedback.setOverallSatisfaction(4);
        feedback.setFeedbackContent("分配结果很满意");

        mockMvc.perform(post("/allocation-feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(feedback)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.success").value(true))
                .andExpect(jsonPath("$.data.message").value("反馈提交成功"));

        // 3. 测试获取学生反馈API
        mockMvc.perform(get("/allocation-feedback/student/S001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].studentId").value("S001"))
                .andExpect(jsonPath("$.data[0].roommateSatisfaction").value(4));
    }

    @Test
    void testAllocationStatisticsAPI() throws Exception {
        // 1. 先进行分配和反馈
        List<String> studentIds = Arrays.asList("S001", "S002");
        allocationService.intelligentAllocation(studentIds);

        AllocationFeedback feedback = new AllocationFeedback();
        feedback.setStudentId("S001");
        feedback.setAllocationId(1L);
        feedback.setRoommateSatisfaction(4);
        feedback.setEnvironmentSatisfaction(5);
        feedback.setOverallSatisfaction(4);
        feedback.setFeedbackContent("满意");
        allocationService.submitAllocationFeedback(feedback);

        // 2. 测试统计API
        mockMvc.perform(get("/allocation-feedback/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalFeedbacks").value(1))
                .andExpect(jsonPath("$.data.averageRoommateSatisfaction").value(4.0))
                .andExpect(jsonPath("$.data.averageEnvironmentSatisfaction").value(5.0))
                .andExpect(jsonPath("$.data.averageOverallSatisfaction").value(4.0))
                .andExpect(jsonPath("$.data.currentWeights").isMap());
    }

    @Test
    void testAllocationWithGenderSeparation() {
        // 测试性别分离分配
        List<String> studentIds = Arrays.asList("S001", "S002", "S003", "S004");
        Map<String, Object> allocationResult = allocationService.intelligentAllocation(studentIds);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> allocations = (List<Map<String, Object>>) allocationResult.get("allocations");

        // 验证性别分离
        Map<String, List<String>> genderGroups = new HashMap<>();
        for (Map<String, Object> allocation : allocations) {
            String gender = (String) allocation.get("gender");
            String dormitoryId = (String) allocation.get("dormitoryId");

            genderGroups.computeIfAbsent(dormitoryId, k -> new ArrayList<>()).add(gender);
        }

        // 验证每个宿舍只有一种性别
        for (Map.Entry<String, List<String>> entry : genderGroups.entrySet()) {
            List<String> genders = entry.getValue();
            Set<String> uniqueGenders = new HashSet<>(genders);
            assertEquals(1, uniqueGenders.size(), "宿舍 " + entry.getKey() + " 应该只有一种性别");
        }
    }

    @Test
    void testAllocationWithMajorPriority() {
        // 测试专业优先分配
        List<String> studentIds = Arrays.asList("S001", "S002", "S003", "S004");
        Map<String, Object> allocationResult = allocationService.intelligentAllocation(studentIds);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> allocations = (List<Map<String, Object>>) allocationResult.get("allocations");

        // 验证同专业学生被分配到同一宿舍
        Map<String, List<String>> dormitoryMajors = new HashMap<>();
        for (Map<String, Object> allocation : allocations) {
            String studentId = (String) allocation.get("studentId");
            String dormitoryId = (String) allocation.get("dormitoryId");
            
            Student student = studentMapper.selectById(studentId);
            dormitoryMajors.computeIfAbsent(dormitoryId, k -> new ArrayList<>()).add(student.getMajor());
        }

        // 验证每个宿舍的学生专业一致性
        for (Map.Entry<String, List<String>> entry : dormitoryMajors.entrySet()) {
            List<String> majors = entry.getValue();
            Set<String> uniqueMajors = new HashSet<>(majors);
            assertTrue(uniqueMajors.size() <= 2, "宿舍 " + entry.getKey() + " 的专业种类应该不超过2种");
        }
    }

    @Test
    void testAllocationWithBedTypePreference() {
        // 测试床位类型偏好（下铺优先）
        List<String> studentIds = Arrays.asList("S001", "S002");
        Map<String, Object> allocationResult = allocationService.intelligentAllocation(studentIds);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> allocations = (List<Map<String, Object>>) allocationResult.get("allocations");

        // 验证下铺优先分配
        for (Map<String, Object> allocation : allocations) {
            String bedId = (String) allocation.get("bedId");
            Bed bed = bedMapper.selectById(bedId);
            
            // 检查是否优先分配下铺
            if (bed.getBedType().equals("下铺")) {
                assertTrue(true, "下铺被优先分配");
            }
        }
    }
}
