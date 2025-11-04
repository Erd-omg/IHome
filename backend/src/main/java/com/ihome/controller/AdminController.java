package com.ihome.controller;

import com.ihome.common.ApiResponse;
import com.ihome.common.JwtResponse;
import com.ihome.common.JwtUtils;
import com.ihome.common.OperationLog;
import com.ihome.entity.Admin;
import com.ihome.entity.Student;
import com.ihome.mapper.*;
import com.ihome.service.StatisticsService;
import com.ihome.service.StudentImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@Tag(name = "管理员管理", description = "管理员相关API接口")
public class AdminController {
    private final AdminMapper adminMapper;
    private final StudentMapper studentMapper;
    private final DormitoryMapper dormitoryMapper;
    private final BedMapper bedMapper;
    private final DormitoryAllocationMapper allocationMapper;
    private final NotificationMapper notificationMapper;
    private final RepairOrderMapper repairOrderMapper;
    private final PaymentRecordMapper paymentRecordMapper;
    private final DormitorySwitchMapper switchMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final StatisticsService statisticsService;
    private final JdbcTemplate jdbcTemplate;
    private final StudentImportService studentImportService;

    public AdminController(AdminMapper adminMapper, StudentMapper studentMapper, DormitoryMapper dormitoryMapper,
                          BedMapper bedMapper, DormitoryAllocationMapper allocationMapper, NotificationMapper notificationMapper,
                          RepairOrderMapper repairOrderMapper, PaymentRecordMapper paymentRecordMapper,
                          DormitorySwitchMapper switchMapper, BCryptPasswordEncoder passwordEncoder, 
                          JwtUtils jwtUtils, StatisticsService statisticsService, JdbcTemplate jdbcTemplate,
                          StudentImportService studentImportService) {
        this.adminMapper = adminMapper;
        this.studentMapper = studentMapper;
        this.dormitoryMapper = dormitoryMapper;
        this.bedMapper = bedMapper;
        this.allocationMapper = allocationMapper;
        this.notificationMapper = notificationMapper;
        this.repairOrderMapper = repairOrderMapper;
        this.paymentRecordMapper = paymentRecordMapper;
        this.switchMapper = switchMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.statisticsService = statisticsService;
        this.jdbcTemplate = jdbcTemplate;
        this.studentImportService = studentImportService;
    }

    @PostMapping("/login")
    @Operation(summary = "管理员登录", description = "管理员使用账号和密码登录系统")
    @OperationLog(module = "管理员管理", operationType = "QUERY", description = "管理员登录")
    public ApiResponse<JwtResponse> login(@RequestBody @Valid LoginRequest req) {
        Admin admin = adminMapper.selectById(req.getId());
        if (admin == null || !passwordEncoder.matches(req.getPassword(), admin.getPassword())) {
            return ApiResponse.error("管理员账号或密码错误");
        }
        
        // 生成JWT token
        String accessToken = jwtUtils.generateAccessToken(admin.getId(), "admin");
        String refreshToken = jwtUtils.generateRefreshToken(admin.getId(), "admin");
        
        // 清空密码信息
        admin.setPassword(null);
        
        JwtResponse jwtResponse = new JwtResponse(accessToken, refreshToken, 600L, admin);
        return ApiResponse.ok(jwtResponse);
    }

    @GetMapping("/dashboard")
    @Operation(summary = "获取仪表盘统计", description = "获取系统关键统计数据和图表数据")
    public ApiResponse<Map<String, Object>> dashboard() {
        try {
            Map<String, Object> statistics = statisticsService.getDashboardStatistics();
            
            // 获取最近活动
            java.util.List<Map<String, Object>> recentActivities = getRecentActivities();
            statistics.put("recentActivities", recentActivities);
            
            return ApiResponse.ok(statistics);
        } catch (Exception e) {
            return ApiResponse.error("获取仪表盘统计失败: " + e.getMessage());
        }
    }
    
    private java.util.List<Map<String, Object>> getRecentActivities() {
        java.util.List<Map<String, Object>> activities = new java.util.ArrayList<>();
        
        try {
            // 获取最近的分配记录
            var allocations = allocationMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.ihome.entity.DormitoryAllocation>()
                    .orderByDesc("check_in_date")
                    .last("LIMIT 5")
            );
            for (var allocation : allocations) {
                var student = studentMapper.selectById(allocation.getStudentId());
                if (student != null) {
                    Map<String, Object> activity = new java.util.HashMap<>();
                    activity.put("type", "allocation");
                    activity.put("title", "学生入住");
                    activity.put("description", String.format("学生 %s (学号: %s) 完成入住登记", student.getName(), student.getId()));
                    activity.put("timestamp", allocation.getCheckInDate());
                    activities.add(activity);
                }
            }
            
            // 获取最近的维修记录
            var repairs = repairOrderMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.ihome.entity.RepairOrder>()
                    .orderByDesc("created_at")
                    .last("LIMIT 5")
            );
            for (var repair : repairs) {
                var student = studentMapper.selectById(repair.getStudentId());
                if (student != null && ("已完成".equals(repair.getStatus()) || "已处理".equals(repair.getStatus()))) {
                    Map<String, Object> activity = new java.util.HashMap<>();
                    activity.put("type", "repair");
                    activity.put("title", "维修完成");
                    activity.put("description", String.format("宿舍 %s 的 %s 维修已完成", repair.getDormitoryId(), repair.getRepairType()));
                    activity.put("timestamp", repair.getCreatedAt());
                    activities.add(activity);
                }
            }
            
            // 获取最近的调换申请
            var switches = switchMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.ihome.entity.DormitorySwitch>()
                    .orderByDesc("apply_time")
                    .last("LIMIT 5")
            );
            for (var switchRecord : switches) {
                var student = studentMapper.selectById(switchRecord.getApplicantId());
                if (student != null && "待审核".equals(switchRecord.getStatus())) {
                    Map<String, Object> activity = new java.util.HashMap<>();
                    activity.put("type", "switch");
                    activity.put("title", "调换申请");
                    activity.put("description", String.format("学生 %s 申请调换宿舍", student.getName()));
                    activity.put("timestamp", switchRecord.getApplyTime());
                    activities.add(activity);
                }
            }
            
            // 获取最近的缴费记录
            var payments = paymentRecordMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.ihome.entity.PaymentRecord>()
                    .orderByDesc("payment_time")
                    .last("LIMIT 5")
            );
            for (var payment : payments) {
                var student = studentMapper.selectById(payment.getStudentId());
                if (student != null) {
                    Map<String, Object> activity = new java.util.HashMap<>();
                    activity.put("type", "payment");
                    activity.put("title", "缴费完成");
                    activity.put("description", String.format("学生 %s 完成缴费 ¥%.2f", student.getName(), payment.getAmount()));
                    activity.put("timestamp", payment.getPaymentTime());
                    activities.add(activity);
                }
            }
            
        } catch (Exception e) {
            System.err.println("获取最近活动失败: " + e.getMessage());
        }
        
        // 按时间倒序排序，取最近10条
        activities.sort((a, b) -> {
            try {
                var t1 = (java.time.LocalDateTime) a.get("timestamp");
                var t2 = (java.time.LocalDateTime) b.get("timestamp");
                return t2.compareTo(t1);
            } catch (Exception e) {
                return 0;
            }
        });
        
        return activities.size() > 10 ? activities.subList(0, 10) : activities;
    }

    @GetMapping("/students")
    public ApiResponse<Map<String, Object>> getStudents(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String college,
            @RequestParam(required = false) String sort) {
        try {
            var students = studentMapper.selectList(null);
            
            // 应用搜索过滤
            if (id != null && !id.isEmpty()) {
                students = students.stream()
                    .filter(s -> s.getId().contains(id))
                    .collect(java.util.stream.Collectors.toList());
            }
            if (name != null && !name.isEmpty()) {
                students = students.stream()
                    .filter(s -> s.getName().contains(name))
                    .collect(java.util.stream.Collectors.toList());
            }
            if (college != null && !college.isEmpty()) {
                students = students.stream()
                    .filter(s -> s.getCollege().contains(college))
                    .collect(java.util.stream.Collectors.toList());
            }
            
            // 应用排序
            if (sort != null && !sort.isEmpty()) {
                String[] sortParts = sort.split(",");
                if (sortParts.length == 2) {
                    String sortField = sortParts[0];
                    String sortOrder = sortParts[1];
                    
                    students = students.stream()
                        .sorted((s1, s2) -> {
                            int result = 0;
                            switch (sortField) {
                                case "id":
                                    result = s1.getId().compareTo(s2.getId());
                                    break;
                                case "name":
                                    result = s1.getName().compareTo(s2.getName());
                                    break;
                                case "college":
                                    result = s1.getCollege().compareTo(s2.getCollege());
                                    break;
                                case "major":
                                    result = s1.getMajor().compareTo(s2.getMajor());
                                    break;
                                default:
                                    return 0;
                            }
                            return "desc".equals(sortOrder) ? -result : result;
                        })
                        .collect(java.util.stream.Collectors.toList());
                }
            }
            
            // 应用分页
            int start = Math.max(0, (page - 1) * size);
            int end = Math.min(start + size, students.size());
            var pagedStudents = students.subList(start, end);
            
            Map<String, Object> result = Map.of(
                "content", pagedStudents,
                "totalElements", students.size(),
                "totalPages", (int) Math.ceil((double) students.size() / size),
                "size", size,
                "number", page - 1,
                "first", page == 1,
                "last", page >= Math.ceil((double) students.size() / size)
            );
            
            return ApiResponse.ok(result);
        } catch (Exception e) {
            return ApiResponse.error("获取学生列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/dormitories")
    public ApiResponse<Map<String, Object>> getDormitories(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String buildingId,
            @RequestParam(required = false) String roomType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String id,
            @RequestParam(required = false) String sort) {
        try {
            var dormitories = dormitoryMapper.selectList(null);
            
            // 应用搜索过滤
            if (buildingId != null && !buildingId.isEmpty()) {
                dormitories = dormitories.stream()
                    .filter(d -> d.getBuildingId().contains(buildingId))
                    .collect(java.util.stream.Collectors.toList());
            }
            if (id != null && !id.isEmpty()) {
                String searchId = id.toLowerCase();
                dormitories = dormitories.stream()
                    .filter(d -> d.getId().toLowerCase().contains(searchId) || 
                                (d.getRoomNumber() != null && d.getRoomNumber().toLowerCase().contains(searchId)))
                    .collect(java.util.stream.Collectors.toList());
            }
            if (roomType != null && !roomType.isEmpty()) {
                dormitories = dormitories.stream()
                    .filter(d -> d.getRoomType().contains(roomType))
                    .collect(java.util.stream.Collectors.toList());
            }
            if (status != null && !status.isEmpty()) {
                dormitories = dormitories.stream()
                    .filter(d -> d.getStatus().contains(status))
                    .collect(java.util.stream.Collectors.toList());
            }
            
            // 应用排序
            if (sort != null && !sort.isEmpty()) {
                String[] sortParts = sort.split(",");
                if (sortParts.length == 2) {
                    String sortField = sortParts[0];
                    String sortOrder = sortParts[1];
                    
                    dormitories = dormitories.stream()
                        .sorted((d1, d2) -> {
                            int result = 0;
                            switch (sortField) {
                                case "buildingId":
                                    result = d1.getBuildingId().compareTo(d2.getBuildingId());
                                    break;
                                case "roomNumber":
                                    result = d1.getRoomNumber().compareTo(d2.getRoomNumber());
                                    break;
                                case "roomType":
                                    result = d1.getRoomType().compareTo(d2.getRoomType());
                                    break;
                                case "currentOccupancy":
                                    result = Integer.compare(d1.getCurrentOccupancy(), d2.getCurrentOccupancy());
                                    break;
                                case "status":
                                    result = d1.getStatus().compareTo(d2.getStatus());
                                    break;
                                default:
                                    return 0;
                            }
                            return "desc".equals(sortOrder) ? -result : result;
                        })
                        .collect(java.util.stream.Collectors.toList());
                }
            }
            
            // 应用分页
            int start = (page - 1) * size;
            int end = Math.min(start + size, dormitories.size());
            var pagedDormitories = dormitories.subList(start, end);
            
            Map<String, Object> result = Map.of(
                "content", pagedDormitories,
                "totalElements", dormitories.size(),
                "totalPages", (int) Math.ceil((double) dormitories.size() / size),
                "size", size,
                "number", page - 1,
                "first", page == 1,
                "last", page >= Math.ceil((double) dormitories.size() / size)
            );
            return ApiResponse.ok(result);
        } catch (Exception e) {
            return ApiResponse.error("获取宿舍列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/allocations")
    public ApiResponse<Map<String, Object>> getAllocations(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String studentId,
            @RequestParam(required = false) String bedId,
            @RequestParam(required = false) String dormitoryId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String sort) {
        try {
            var allocations = allocationMapper.selectList(null);
            
            // 应用搜索过滤
            if (studentId != null && !studentId.isEmpty()) {
                allocations = allocations.stream()
                    .filter(a -> a.getStudentId().contains(studentId))
                    .collect(java.util.stream.Collectors.toList());
            }
            if (bedId != null && !bedId.isEmpty()) {
                allocations = allocations.stream()
                    .filter(a -> a.getBedId() != null && a.getBedId().contains(bedId))
                    .collect(java.util.stream.Collectors.toList());
            }
            if (dormitoryId != null && !dormitoryId.isEmpty()) {
                allocations = allocations.stream()
                    .filter(a -> a.getBedId() != null && a.getBedId().contains(dormitoryId))
                    .collect(java.util.stream.Collectors.toList());
            }
            if (status != null && !status.isEmpty()) {
                allocations = allocations.stream()
                    .filter(a -> a.getStatus().contains(status))
                    .collect(java.util.stream.Collectors.toList());
            }
            // 入住时间筛选
            if (startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty()) {
                final java.time.LocalDate start = java.time.LocalDate.parse(startDate);
                final java.time.LocalDate end = java.time.LocalDate.parse(endDate);
                allocations = allocations.stream()
                    .filter(a -> {
                        if (a.getCheckInDate() == null) return false;
                        return !a.getCheckInDate().isBefore(start) && !a.getCheckInDate().isAfter(end);
                    })
                    .collect(java.util.stream.Collectors.toList());
            }
            
            // 应用排序
            if (sort != null && !sort.isEmpty()) {
                String[] sortParts = sort.split(",");
                if (sortParts.length == 2) {
                    String sortField = sortParts[0];
                    String sortOrder = sortParts[1];
                    
                    allocations = allocations.stream()
                        .sorted((a1, a2) -> {
                            int result = 0;
                            switch (sortField) {
                                case "studentId":
                                    result = a1.getStudentId().compareTo(a2.getStudentId());
                                    break;
                                case "bedId":
                                    String d1 = a1.getBedId() != null ? a1.getBedId() : "";
                                    String d2 = a2.getBedId() != null ? a2.getBedId() : "";
                                    result = d1.compareTo(d2);
                                    break;
                                case "checkInDate":
                                    if (a1.getCheckInDate() != null && a2.getCheckInDate() != null) {
                                        result = a1.getCheckInDate().compareTo(a2.getCheckInDate());
                                    }
                                    break;
                                case "status":
                                    result = a1.getStatus().compareTo(a2.getStatus());
                                    break;
                                default:
                                    return 0;
                            }
                            return "desc".equals(sortOrder) ? -result : result;
                        })
                        .collect(java.util.stream.Collectors.toList());
                }
            }
            
            // 应用分页
            int start = (page - 1) * size;
            int end = Math.min(start + size, allocations.size());
            var pagedAllocations = allocations.subList(start, end);
            
            Map<String, Object> result = Map.of(
                "content", pagedAllocations,
                "totalElements", allocations.size(),
                "totalPages", (int) Math.ceil((double) allocations.size() / size),
                "size", size,
                "number", page - 1,
                "first", page == 1,
                "last", page >= Math.ceil((double) allocations.size() / size)
            );
            return ApiResponse.ok(result);
        } catch (Exception e) {
            return ApiResponse.error("获取分配记录失败: " + e.getMessage());
        }
    }

    @GetMapping("/students/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<java.util.List<Map<String, Object>>> searchStudents(@RequestParam(required = false) String keyword) {
        try {
            java.util.List<Student> students;
            
            if (keyword != null && !keyword.trim().isEmpty()) {
                // 根据关键词搜索学生
                students = studentMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Student>()
                        .and(wrapper -> wrapper
                            .like("id", keyword)
                            .or()
                            .like("name", keyword)
                        )
                );
            } else {
                // 获取所有学生
                students = studentMapper.selectList(null);
            }
            
            java.util.List<Map<String, Object>> result = students.stream()
                .map(student -> {
                    Map<String, Object> studentInfo = new java.util.HashMap<>();
                    studentInfo.put("id", student.getId());
                    studentInfo.put("name", student.getName());
                    studentInfo.put("major", student.getMajor());
                    studentInfo.put("status", student.getStatus());
                    return studentInfo;
                })
                .collect(java.util.stream.Collectors.toList());
            
            return ApiResponse.ok(result);
        } catch (Exception e) {
            return ApiResponse.error("搜索学生失败: " + e.getMessage());
        }
    }

    @GetMapping("/beds/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<java.util.List<Map<String, Object>>> searchBeds(@RequestParam(required = false) String keyword) {
        try {
            java.util.List<com.ihome.entity.Bed> beds;
            
            if (keyword != null && !keyword.trim().isEmpty()) {
                // 根据关键词搜索床位
                beds = bedMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.ihome.entity.Bed>()
                        .and(wrapper -> wrapper
                            .like("id", keyword)
                            .or()
                            .like("dormitory_id", keyword)
                            .or()
                            .like("bed_number", keyword)
                        )
                );
            } else {
                // 获取所有床位
                beds = bedMapper.selectList(null);
            }
            
            java.util.List<Map<String, Object>> result = beds.stream()
                .map(bed -> {
                    Map<String, Object> bedInfo = new java.util.HashMap<>();
                    bedInfo.put("id", bed.getId());
                    bedInfo.put("dormitoryId", bed.getDormitoryId());
                    bedInfo.put("bedNumber", bed.getBedNumber());
                    bedInfo.put("bedType", bed.getBedType());
                    bedInfo.put("status", bed.getStatus());
                    return bedInfo;
                })
                .collect(java.util.stream.Collectors.toList());
            
            return ApiResponse.ok(result);
        } catch (Exception e) {
            return ApiResponse.error("搜索床位失败: " + e.getMessage());
        }
    }

    @PostMapping("/allocations")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> allocateDormitory(@RequestBody Map<String, Object> request) {
        try {
            String studentId = (String) request.get("studentId");
            String bedId = (String) request.get("bedId");
            String checkInDate = (String) request.get("checkInDate");
            
            // 验证必填字段
            if (studentId == null || bedId == null || checkInDate == null) {
                return ApiResponse.error("学生ID、床位ID和入住时间不能为空");
            }
            
            // 检查学生是否存在
            var student = studentMapper.selectById(studentId);
            if (student == null) {
                return ApiResponse.error("学生不存在");
            }
            
            // 检查床位是否存在且可用
            com.ihome.entity.Bed bed = bedMapper.selectById(bedId);
            if (bed == null) {
                return ApiResponse.error("床位不存在");
            }
            if (!"可用".equals(bed.getStatus())) {
                return ApiResponse.error("床位不可用");
            }
            
            // 检查学生是否已有任何状态的住宿分配（防止唯一约束冲突）
            var existingAllocations = allocationMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.ihome.entity.DormitoryAllocation>()
                    .eq("student_id", studentId)
            );
            // 如果有"在住"状态的分配，直接拒绝
            boolean hasActiveAllocation = existingAllocations.stream()
                .anyMatch(a -> "在住".equals(a.getStatus()));
            if (hasActiveAllocation) {
                return ApiResponse.error("该学生已有有效的住宿分配（在住状态），请先办理退宿");
            }
            // 如果有其他状态的分配记录，需要先删除旧记录（避免唯一约束冲突）
            if (!existingAllocations.isEmpty()) {
                for (var oldAllocation : existingAllocations) {
                    allocationMapper.deleteById(oldAllocation.getId());
                }
            }
            
            // 创建住宿分配记录
            com.ihome.entity.DormitoryAllocation allocation = new com.ihome.entity.DormitoryAllocation();
            allocation.setStudentId(studentId);
            allocation.setBedId(bedId);
            allocation.setCheckInDate(java.time.LocalDate.parse(checkInDate));
            allocation.setStatus("在住");
            
            int result = allocationMapper.insert(allocation);
            
            if (result > 0) {
                // 更新床位状态为已占用
                bed.setStatus("已占用");
                bedMapper.updateById(bed);
                
                return ApiResponse.ok(Map.of("message", "宿舍分配成功", "allocationId", allocation.getId()));
            } else {
                return ApiResponse.error("宿舍分配失败");
            }
        } catch (Exception e) {
            return ApiResponse.error("分配宿舍失败: " + e.getMessage());
        }
    }

    @GetMapping("/notifications")
    public ApiResponse<Map<String, Object>> getNotifications(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String receiverType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sort) {
        try {
            var notifications = notificationMapper.selectList(null);
            
            // 应用搜索过滤
            if (title != null && !title.isEmpty()) {
                notifications = notifications.stream()
                    .filter(n -> n.getTitle() != null && n.getTitle().contains(title))
                    .collect(java.util.stream.Collectors.toList());
            }
            if (type != null && !type.isEmpty()) {
                notifications = notifications.stream()
                    .filter(n -> n.getType() != null && n.getType().contains(type))
                    .collect(java.util.stream.Collectors.toList());
            }
            if (priority != null && !priority.isEmpty()) {
                notifications = notifications.stream()
                    .filter(n -> n.getPriority() != null && n.getPriority().contains(priority))
                    .collect(java.util.stream.Collectors.toList());
            }
            if (receiverType != null && !receiverType.isEmpty()) {
                String finalReceiverType = receiverType;
                notifications = notifications.stream()
                    .filter(n -> {
                        // 如果筛选条件为"all"，则筛选接收者ID为ALL或空的系统通知
                        if ("all".equals(finalReceiverType)) {
                            return n.getReceiverId() != null && "ALL".equalsIgnoreCase(n.getReceiverId());
                        }
                        // 如果是student或admin，筛选对应的receiverType
                        return n.getReceiverType() != null && n.getReceiverType().equals(finalReceiverType);
                    })
                    .collect(java.util.stream.Collectors.toList());
            }
            if (status != null && !status.isEmpty()) {
                notifications = notifications.stream()
                    .filter(n -> n.getIsRead() != null && n.getIsRead().toString().contains(status))
                    .collect(java.util.stream.Collectors.toList());
            }
            
            // 应用排序
            if (sort != null && !sort.isEmpty()) {
                String[] sortParts = sort.split(",");
                if (sortParts.length == 2) {
                    String sortField = sortParts[0];
                    String sortOrder = sortParts[1];
                    
                    notifications = notifications.stream()
                        .sorted((n1, n2) -> {
                            int result = 0;
                            switch (sortField) {
                                case "title":
                                    result = n1.getTitle().compareTo(n2.getTitle());
                                    break;
                                case "type":
                                    result = n1.getType().compareTo(n2.getType());
                                    break;
                                case "isRead":
                                    Boolean r1 = n1.getIsRead() != null ? n1.getIsRead() : false;
                                    Boolean r2 = n2.getIsRead() != null ? n2.getIsRead() : false;
                                    result = r1.compareTo(r2);
                                    break;
                                case "createTime":
                                    if (n1.getCreateTime() != null && n2.getCreateTime() != null) {
                                        result = n1.getCreateTime().compareTo(n2.getCreateTime());
                                    }
                                    break;
                                default:
                                    return 0;
                            }
                            return "desc".equals(sortOrder) ? -result : result;
                        })
                        .collect(java.util.stream.Collectors.toList());
                }
            }
            
            // 应用分页
            int start = (page - 1) * size;
            int end = Math.min(start + size, notifications.size());
            var pagedNotifications = notifications.subList(start, end);
            
            Map<String, Object> result = Map.of(
                "content", pagedNotifications,
                "totalElements", notifications.size(),
                "totalPages", (int) Math.ceil((double) notifications.size() / size),
                "size", size,
                "number", page - 1,
                "first", page == 1,
                "last", page >= Math.ceil((double) notifications.size() / size)
            );
            return ApiResponse.ok(result);
        } catch (Exception e) {
            return ApiResponse.error("获取通知列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/repairs")
    public ApiResponse<Map<String, Object>> getRepairs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String studentId,
            @RequestParam(required = false) String dormitoryId,
            @RequestParam(required = false) String repairType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sort) {
        try {
            var repairs = repairOrderMapper.selectList(null);
            
            // 应用搜索过滤
            if (studentId != null && !studentId.isEmpty()) {
                repairs = repairs.stream()
                    .filter(r -> r.getStudentId().contains(studentId))
                    .collect(java.util.stream.Collectors.toList());
            }
            if (dormitoryId != null && !dormitoryId.isEmpty()) {
                String searchDorm = dormitoryId.toLowerCase();
                repairs = repairs.stream()
                    .filter(r -> r.getDormitoryId() != null && r.getDormitoryId().toLowerCase().contains(searchDorm))
                    .collect(java.util.stream.Collectors.toList());
            }
            if (repairType != null && !repairType.isEmpty()) {
                repairs = repairs.stream()
                    .filter(r -> r.getRepairType().contains(repairType))
                    .collect(java.util.stream.Collectors.toList());
            }
            if (status != null && !status.isEmpty()) {
                repairs = repairs.stream()
                    .filter(r -> r.getStatus().contains(status))
                    .collect(java.util.stream.Collectors.toList());
            }
            
            // 应用排序
            if (sort != null && !sort.isEmpty()) {
                String[] sortParts = sort.split(",");
                if (sortParts.length == 2) {
                    String sortField = sortParts[0];
                    String sortOrder = sortParts[1];
                    
                    repairs = repairs.stream()
                        .sorted((r1, r2) -> {
                            int result = 0;
                            switch (sortField) {
                                case "studentId":
                                    result = r1.getStudentId().compareTo(r2.getStudentId());
                                    break;
                                case "dormitoryId":
                                    result = r1.getDormitoryId().compareTo(r2.getDormitoryId());
                                    break;
                                case "repairType":
                                    result = r1.getRepairType().compareTo(r2.getRepairType());
                                    break;
                                case "urgencyLevel":
                                    result = r1.getUrgencyLevel().compareTo(r2.getUrgencyLevel());
                                    break;
                                case "status":
                                    result = r1.getStatus().compareTo(r2.getStatus());
                                    break;
                                case "createdAt":
                                    if (r1.getCreatedAt() != null && r2.getCreatedAt() != null) {
                                        result = r1.getCreatedAt().compareTo(r2.getCreatedAt());
                                    }
                                    break;
                                default:
                                    return 0;
                            }
                            return "desc".equals(sortOrder) ? -result : result;
                        })
                        .collect(java.util.stream.Collectors.toList());
                }
            }
            
            // 应用分页
            int start = (page - 1) * size;
            int end = Math.min(start + size, repairs.size());
            var pagedRepairs = repairs.subList(start, end);
            
            Map<String, Object> result = Map.of(
                "content", pagedRepairs,
                "totalElements", repairs.size(),
                "totalPages", (int) Math.ceil((double) repairs.size() / size),
                "size", size,
                "number", page - 1,
                "first", page == 1,
                "last", page >= Math.ceil((double) repairs.size() / size)
            );
            return ApiResponse.ok(result);
        } catch (Exception e) {
            return ApiResponse.error("获取维修记录失败: " + e.getMessage());
        }
    }

    @GetMapping("/payments")
    public ApiResponse<Map<String, Object>> getPayments(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String studentId,
            @RequestParam(required = false) String paymentType,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) String sort) {
        try {
            var payments = paymentRecordMapper.selectList(null);
            
            // 应用搜索过滤
            if (studentId != null && !studentId.isEmpty()) {
                payments = payments.stream()
                    .filter(p -> p.getStudentId().contains(studentId))
                    .collect(java.util.stream.Collectors.toList());
            }
            if (paymentType != null && !paymentType.isEmpty()) {
                payments = payments.stream()
                    .filter(p -> p.getPaymentType() != null && p.getPaymentType().contains(paymentType))
                    .collect(java.util.stream.Collectors.toList());
            }
            if (paymentMethod != null && !paymentMethod.isEmpty()) {
                payments = payments.stream()
                    .filter(p -> p.getPaymentMethod() != null && p.getPaymentMethod().contains(paymentMethod))
                    .collect(java.util.stream.Collectors.toList());
            }
            
            // 应用排序
            if (sort != null && !sort.isEmpty()) {
                String[] sortParts = sort.split(",");
                if (sortParts.length == 2) {
                    String sortField = sortParts[0];
                    String sortOrder = sortParts[1];
                    
                    payments = payments.stream()
                        .sorted((p1, p2) -> {
                            int result = 0;
                            switch (sortField) {
                                case "studentId":
                                    result = p1.getStudentId().compareTo(p2.getStudentId());
                                    break;
                                case "amount":
                                    result = p1.getAmount().compareTo(p2.getAmount());
                                    break;
                                case "paymentMethod":
                                    result = p1.getPaymentMethod().compareTo(p2.getPaymentMethod());
                                    break;
                                case "paymentTime":
                                    if (p1.getPaymentTime() != null && p2.getPaymentTime() != null) {
                                        result = p1.getPaymentTime().compareTo(p2.getPaymentTime());
                                    }
                                    break;
                                default:
                                    return 0;
                            }
                            return "desc".equals(sortOrder) ? -result : result;
                        })
                        .collect(java.util.stream.Collectors.toList());
                }
            }
            
            // 应用分页
            int start = (page - 1) * size;
            int end = Math.min(start + size, payments.size());
            var pagedPayments = payments.subList(start, end);
            
            Map<String, Object> result = Map.of(
                "content", pagedPayments,
                "totalElements", payments.size(),
                "totalPages", (int) Math.ceil((double) payments.size() / size),
                "size", size,
                "number", page - 1,
                "first", page == 1,
                "last", page >= Math.ceil((double) payments.size() / size)
            );
            return ApiResponse.ok(result);
        } catch (Exception e) {
            return ApiResponse.error("获取缴费记录失败: " + e.getMessage());
        }
    }

    @GetMapping("/exchanges")
    public ApiResponse<Map<String, Object>> getExchanges(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String studentId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sort) {
        try {
            var exchanges = switchMapper.selectList(null);
            
            // 应用搜索过滤
            if (studentId != null && !studentId.isEmpty()) {
                exchanges = exchanges.stream()
                    .filter(e -> e.getApplicantId().contains(studentId))
                    .collect(java.util.stream.Collectors.toList());
            }
            if (status != null && !status.isEmpty()) {
                exchanges = exchanges.stream()
                    .filter(e -> e.getStatus().contains(status))
                    .collect(java.util.stream.Collectors.toList());
            }
            
            // 应用排序
            if (sort != null && !sort.isEmpty()) {
                String[] sortParts = sort.split(",");
                if (sortParts.length == 2) {
                    String sortField = sortParts[0];
                    String sortOrder = sortParts[1];
                    
                    exchanges = exchanges.stream()
                        .sorted((e1, e2) -> {
                            int result = 0;
                            switch (sortField) {
                                case "applicantId":
                                    result = e1.getApplicantId().compareTo(e2.getApplicantId());
                                    break;
                                case "status":
                                    result = e1.getStatus().compareTo(e2.getStatus());
                                    break;
                                case "applyTime":
                                    if (e1.getApplyTime() != null && e2.getApplyTime() != null) {
                                        result = e1.getApplyTime().compareTo(e2.getApplyTime());
                                    }
                                    break;
                                default:
                                    return 0;
                            }
                            return "desc".equals(sortOrder) ? -result : result;
                        })
                        .collect(java.util.stream.Collectors.toList());
                }
            }
            
            // 应用分页
            int start = (page - 1) * size;
            int end = Math.min(start + size, exchanges.size());
            var pagedExchanges = exchanges.subList(start, end);
            
            Map<String, Object> result = Map.of(
                "content", pagedExchanges,
                "totalElements", exchanges.size(),
                "totalPages", (int) Math.ceil((double) exchanges.size() / size),
                "size", size,
                "number", page - 1,
                "first", page == 1,
                "last", page >= Math.ceil((double) exchanges.size() / size)
            );
            return ApiResponse.ok(result);
        } catch (Exception e) {
            return ApiResponse.error("获取调换申请失败: " + e.getMessage());
        }
    }

    // ==================== 学生管理 CRUD 操作 ====================
    
    @PostMapping("/students")
    public ApiResponse<Map<String, Object>> createStudent(@RequestBody Student student) {
        try {
            // 检查学号是否已存在
            if (studentMapper.selectById(student.getId()) != null) {
                return ApiResponse.error("学号已存在");
            }
            
            // 密码加密
            student.setPassword(passwordEncoder.encode(student.getPassword()));
            int result = studentMapper.insert(student);
            if (result > 0) {
                return ApiResponse.ok(Map.of("message", "学生创建成功", "id", student.getId()));
            } else {
                return ApiResponse.error("学生创建失败");
            }
        } catch (Exception e) {
            return ApiResponse.error("创建学生失败: " + e.getMessage());
        }
    }

    @GetMapping("/students/{id}")
    public ApiResponse<Student> getStudent(@PathVariable String id) {
        try {
            Student student = studentMapper.selectById(id);
            if (student == null) {
                return ApiResponse.error("学生不存在");
            }
            return ApiResponse.ok(student);
        } catch (Exception e) {
            return ApiResponse.error("获取学生信息失败: " + e.getMessage());
        }
    }

    @PutMapping("/allocations/{id}/checkout")
    public ApiResponse<Map<String, Object>> checkoutAllocation(
            @PathVariable Integer id,
            @RequestBody Map<String, String> request) {
        try {
            var allocation = allocationMapper.selectById(id);
            if (allocation == null) {
                return ApiResponse.error("分配记录不存在");
            }
            
            if (!"在住".equals(allocation.getStatus())) {
                return ApiResponse.error("只能办理在住状态的退宿");
            }
            
            // 更新分配状态为已退宿
            allocation.setStatus("已退宿");
            String checkOutDate = request.get("checkOutDate");
            if (checkOutDate != null && !checkOutDate.isEmpty()) {
                allocation.setCheckOutDate(java.time.LocalDate.parse(checkOutDate));
            } else {
                allocation.setCheckOutDate(java.time.LocalDate.now());
            }
            
            int result = allocationMapper.updateById(allocation);
            
            if (result > 0) {
                // 更新床位状态为可用
                com.ihome.entity.Bed bed = bedMapper.selectById(allocation.getBedId());
                if (bed != null) {
                    bed.setStatus("可用");
                    bedMapper.updateById(bed);
                }
                
                return ApiResponse.ok(Map.of("message", "退宿办理成功"));
            } else {
                return ApiResponse.error("退宿办理失败");
            }
        } catch (Exception e) {
            return ApiResponse.error("办理退宿失败: " + e.getMessage());
        }
    }

    @PutMapping("/students/{id}")
    public ApiResponse<Map<String, Object>> updateStudent(@PathVariable String id, @RequestBody Student student) {
        try {
            student.setId(id);
            // 如果密码不为空，则加密
            if (student.getPassword() != null && !student.getPassword().isEmpty()) {
                student.setPassword(passwordEncoder.encode(student.getPassword()));
            }
            int result = studentMapper.updateById(student);
            if (result > 0) {
                return ApiResponse.ok(Map.of("message", "学生信息更新成功"));
            } else {
                return ApiResponse.error("学生信息更新失败");
            }
        } catch (Exception e) {
            return ApiResponse.error("更新学生信息失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/students/{id}")
    public ApiResponse<Map<String, Object>> deleteStudent(@PathVariable String id) {
        try {
            // 先删除相关的宿舍分配记录
            var allocations = allocationMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.ihome.entity.DormitoryAllocation>()
                    .eq("student_id", id)
            );
            for (var allocation : allocations) {
                allocationMapper.deleteById(allocation.getId());
            }
            
            // 删除相关的维修记录
            var repairs = repairOrderMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.ihome.entity.RepairOrder>()
                    .eq("student_id", id)
            );
            for (var repair : repairs) {
                repairOrderMapper.deleteById(repair.getId());
            }
            
            // 删除相关的缴费记录
            var payments = paymentRecordMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.ihome.entity.PaymentRecord>()
                    .eq("student_id", id)
            );
            for (var payment : payments) {
                paymentRecordMapper.deleteById(payment.getId());
            }
            
            // 删除相关的调换申请记录
            var switches = switchMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.ihome.entity.DormitorySwitch>()
                    .eq("applicant_id", id)
            );
            for (var switchRecord : switches) {
                switchMapper.deleteById(switchRecord.getId());
            }
            
            // 删除相关的问卷答案记录
            try {
                // 使用JdbcTemplate删除问卷答案
                jdbcTemplate.update("DELETE FROM questionnaire_answers WHERE student_id = ?", id);
            } catch (Exception e) {
                // 如果删除问卷答案失败，记录日志但不影响主流程
                System.err.println("删除问卷答案失败: " + e.getMessage());
            }
            
            // 删除相关的室友标签记录
            try {
                // 使用JdbcTemplate删除室友标签
                jdbcTemplate.update("DELETE FROM roommate_tags WHERE student_id = ?", id);
            } catch (Exception e) {
                // 如果删除室友标签失败，记录日志但不影响主流程
                System.err.println("删除室友标签失败: " + e.getMessage());
            }
            
            // 最后删除学生记录
            int result = studentMapper.deleteById(id);
            if (result > 0) {
                return ApiResponse.ok(Map.of("message", "学生删除成功"));
            } else {
                return ApiResponse.error("学生删除失败");
            }
        } catch (Exception e) {
            return ApiResponse.error("删除学生失败: " + e.getMessage());
        }
    }

    // ==================== 宿舍管理 CRUD 操作 ====================
    
    @PostMapping("/dormitories")
    public ApiResponse<Map<String, Object>> createDormitory(@RequestBody com.ihome.entity.Dormitory dormitory) {
        try {
            // 检查宿舍ID是否已存在
            var existingDormitory = dormitoryMapper.selectById(dormitory.getId());
            if (existingDormitory != null) {
                return ApiResponse.error("宿舍ID已存在: " + dormitory.getId());
            }
            
            // 从房间号提取楼层数（如果房间号格式为XXX，第一位为楼层）
            if (dormitory.getRoomNumber() != null && !dormitory.getRoomNumber().isEmpty()) {
                try {
                    String roomNum = dormitory.getRoomNumber();
                    // 尝试提取楼层号
                    if (roomNum.matches("\\d+")) {
                        int floor = roomNum.charAt(0) - '0';
                        dormitory.setFloorNumber(floor);
                    } else {
                        // 默认设为1楼
                        dormitory.setFloorNumber(1);
                    }
                } catch (Exception e) {
                    dormitory.setFloorNumber(1);
                }
            }
            
            int result = dormitoryMapper.insert(dormitory);
            if (result > 0) {
                // 创建床位，ID格式：根据buildingId生成B前缀，如B01-101-1
                int bedCount = dormitory.getBedCount() != null ? dormitory.getBedCount() : 4;
                String dormitoryId = dormitory.getId(); // 例如：D01-101
                String buildingId = dormitory.getBuildingId(); // 例如：B001
                
                // 从buildingId提取前缀B，从dormitoryId提取后续部分
                // buildingId: B001，dormitoryId: D01-101 -> 床位ID: B01-101-1
                // 将D01-101中的D替换为B
                String bedIdPrefix = dormitoryId.replaceFirst("^[DB]", buildingId.substring(0, 1));
                
                for (int i = 1; i <= bedCount; i++) {
                    com.ihome.entity.Bed bed = new com.ihome.entity.Bed();
                    // 床位ID格式：B01-101-1
                    bed.setId(bedIdPrefix + "-" + i);
                    bed.setDormitoryId(dormitoryId);
                    bed.setBedNumber(String.valueOf(i));
                    // bed_type必须是ENUM值：上铺或下铺
                    bed.setBedType(i % 2 == 0 ? "下铺" : "上铺");
                    bed.setStatus("可用");
                    bedMapper.insert(bed);
                }
                
                return ApiResponse.ok(Map.of("message", "宿舍创建成功", "id", dormitory.getId()));
            } else {
                return ApiResponse.error("宿舍创建失败");
            }
        } catch (Exception e) {
            return ApiResponse.error("创建宿舍失败: " + e.getMessage());
        }
    }

    @PutMapping("/dormitories/{id}")
    public ApiResponse<Map<String, Object>> updateDormitory(@PathVariable String id, @RequestBody com.ihome.entity.Dormitory dormitory) {
        try {
            dormitory.setId(id);
            int result = dormitoryMapper.updateById(dormitory);
            if (result > 0) {
                return ApiResponse.ok(Map.of("message", "宿舍信息更新成功"));
            } else {
                return ApiResponse.error("宿舍信息更新失败");
            }
        } catch (Exception e) {
            return ApiResponse.error("更新宿舍信息失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/dormitories/{id}")
    public ApiResponse<Map<String, Object>> deleteDormitory(@PathVariable String id) {
        try {
            // 检查宿舍是否有人入住
            var beds = bedMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.ihome.entity.Bed>()
                    .eq("dormitory_id", id)
            );
            
            // 检查是否有床位被占用
            for (var bed : beds) {
                if ("已占用".equals(bed.getStatus())) {
                    return ApiResponse.error("该宿舍有人正在入住，无法删除");
                }
            }
            
            // 删除相关床位
            for (var bed : beds) {
                bedMapper.deleteById(bed.getId());
            }
            
            int result = dormitoryMapper.deleteById(id);
            if (result > 0) {
                return ApiResponse.ok(Map.of("message", "宿舍删除成功"));
            } else {
                return ApiResponse.error("宿舍删除失败");
            }
        } catch (Exception e) {
            return ApiResponse.error("删除宿舍失败: " + e.getMessage());
        }
    }

    // ==================== 调换申请管理操作 ====================
    
    @PutMapping("/exchanges/{id}/status")
    public ApiResponse<Map<String, Object>> updateExchangeStatus(
            @PathVariable Integer id, 
            @RequestParam String status,
            @RequestParam(required = false) String reviewComment) {
        try {
            var exchange = switchMapper.selectById(id);
            if (exchange == null) {
                return ApiResponse.error("调换申请不存在");
            }
            
            exchange.setStatus(status);
            exchange.setReviewTime(java.time.LocalDateTime.now());
            // 从JWT认证上下文获取当前管理员ID
            String currentAdminId = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getName();
            exchange.setReviewerId(currentAdminId);
            if (reviewComment != null) {
                exchange.setReviewComment(reviewComment);
            }
            
            int result = switchMapper.updateById(exchange);
            if (result > 0) {
                return ApiResponse.ok(Map.of("message", "调换申请状态更新成功"));
            } else {
                return ApiResponse.error("调换申请状态更新失败");
            }
        } catch (Exception e) {
            return ApiResponse.error("更新调换申请状态失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/exchanges/{id}")
    public ApiResponse<Map<String, Object>> deleteExchange(@PathVariable Integer id) {
        try {
            int result = switchMapper.deleteById(id);
            if (result > 0) {
                return ApiResponse.ok(Map.of("message", "调换申请删除成功"));
            } else {
                return ApiResponse.error("调换申请删除失败");
            }
        } catch (Exception e) {
            return ApiResponse.error("删除调换申请失败: " + e.getMessage());
        }
    }

    // ==================== 维修管理操作 ====================
    
    @PutMapping("/repairs/{id}/status")
    public ApiResponse<Map<String, Object>> updateRepairStatus(
            @PathVariable Integer id, 
            @RequestParam String status) {
        try {
            var repair = repairOrderMapper.selectById(id);
            if (repair == null) {
                return ApiResponse.error("维修工单不存在");
            }
            
            repair.setStatus(status);
            int result = repairOrderMapper.updateById(repair);
            if (result > 0) {
                return ApiResponse.ok(Map.of("message", "维修工单状态更新成功"));
            } else {
                return ApiResponse.error("维修工单状态更新失败");
            }
        } catch (Exception e) {
            return ApiResponse.error("更新维修工单状态失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/repairs/{id}")
    public ApiResponse<Map<String, Object>> deleteRepair(@PathVariable Integer id) {
        try {
            int result = repairOrderMapper.deleteById(id);
            if (result > 0) {
                return ApiResponse.ok(Map.of("message", "维修工单删除成功"));
            } else {
                return ApiResponse.error("维修工单删除失败");
            }
        } catch (Exception e) {
            return ApiResponse.error("删除维修工单失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/notifications/{id}")
    public ApiResponse<Map<String, Object>> deleteNotification(@PathVariable Integer id) {
        try {
            var notification = notificationMapper.selectById(id);
            if (notification == null) {
                return ApiResponse.error("通知不存在");
            }
            
            int result = notificationMapper.deleteById(id);
            if (result > 0) {
                return ApiResponse.ok(Map.of("message", "通知删除成功"));
            } else {
                return ApiResponse.error("通知删除失败");
            }
        } catch (Exception e) {
            return ApiResponse.error("删除通知失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/notifications/batch")
    public ApiResponse<Map<String, Object>> batchDeleteNotifications(@RequestBody List<Integer> ids) {
        try {
            if (ids == null || ids.isEmpty()) {
                return ApiResponse.error("请选择要删除的通知");
            }
            
            int successCount = 0;
            int failCount = 0;
            List<String> errors = new ArrayList<>();
            
            for (Integer id : ids) {
                try {
                    var notification = notificationMapper.selectById(id);
                    if (notification == null) {
                        failCount++;
                        errors.add("通知ID " + id + " 不存在");
                        continue;
                    }
                    
                    int result = notificationMapper.deleteById(id);
                    if (result > 0) {
                        successCount++;
                    } else {
                        failCount++;
                        errors.add("通知ID " + id + " 删除失败");
                    }
                } catch (Exception e) {
                    failCount++;
                    errors.add("通知ID " + id + " 删除异常: " + e.getMessage());
                }
            }
            
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("success", failCount == 0);
            result.put("successCount", successCount);
            result.put("failCount", failCount);
            result.put("total", ids.size());
            result.put("message", String.format("成功删除 %d 条通知，失败 %d 条", successCount, failCount));
            if (!errors.isEmpty()) {
                result.put("errors", errors);
            }
            
            return ApiResponse.ok(result);
        } catch (Exception e) {
            return ApiResponse.error("批量删除通知失败: " + e.getMessage());
        }
    }

    @GetMapping("/profile/{id}")
    public ApiResponse<Admin> profile(@PathVariable String id) {
        Admin admin = adminMapper.selectById(id);
        if (admin == null) return ApiResponse.error("管理员不存在");
        admin.setPassword(null);
        return ApiResponse.ok(admin);
    }

    // ==================== 学生批量导入功能 ====================
    
    /**
     * 下载学生导入模板
     */
    @GetMapping("/students/import/template")
    @Operation(summary = "下载学生导入模板", description = "下载Excel格式的学生信息导入模板")
    public ResponseEntity<Resource> downloadTemplate() {
        try {
            byte[] template = studentImportService.generateTemplate();
            ByteArrayResource resource = new ByteArrayResource(template);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=学生信息导入模板.xlsx")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 验证学生导入数据
     */
    @PostMapping("/students/import/validate")
    @Operation(summary = "验证学生导入数据", description = "验证上传的Excel文件中的数据格式和内容")
    public ApiResponse<Map<String, Object>> validateImportData(@RequestParam("file") @Parameter(description = "Excel文件") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ApiResponse.error("文件不能为空");
            }
            
            Map<String, Object> result = studentImportService.validateFile(file);
            return ApiResponse.ok(result);
        } catch (Exception e) {
            return ApiResponse.error("验证文件失败: " + e.getMessage());
        }
    }

    /**
     * 导入学生数据
     */
    @PostMapping("/students/import")
    @Operation(summary = "导入学生数据", description = "导入Excel文件中的学生数据到系统")
    public ApiResponse<Map<String, Object>> importStudents(@RequestParam("file") @Parameter(description = "Excel文件") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ApiResponse.error("文件不能为空");
            }
            
            Map<String, Object> result = studentImportService.importStudents(file);
            return ApiResponse.ok(result);
        } catch (Exception e) {
            return ApiResponse.error("导入数据失败: " + e.getMessage());
        }
    }

    public static class LoginRequest {
        @NotBlank
        private String id;
        @NotBlank
        private String password;

        public String getId() { return id; }
        public String getPassword() { return password; }
        public void setId(String id) { this.id = id; }
        public void setPassword(String password) { this.password = password; }
    }
}