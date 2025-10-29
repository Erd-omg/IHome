package com.ihome.controller;

import com.ihome.common.ApiResponse;
import com.ihome.common.JwtResponse;
import com.ihome.common.JwtUtils;
import com.ihome.common.OperationLog;
import com.ihome.entity.Student;
import com.ihome.entity.DormitoryAllocation;
import com.ihome.entity.Notification;
import com.ihome.entity.PaymentRecord;
import com.ihome.mapper.StudentMapper;
import com.ihome.mapper.DormitoryAllocationMapper;
import com.ihome.mapper.NotificationMapper;
import com.ihome.mapper.PaymentRecordMapper;
import com.ihome.mapper.BedMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/students")
@Tag(name = "学生管理", description = "学生相关API接口")
public class StudentController {
    private final StudentMapper studentMapper;
    private final DormitoryAllocationMapper allocationMapper;
    private final NotificationMapper notificationMapper;
    private final PaymentRecordMapper paymentRecordMapper;
    private final BedMapper bedMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public StudentController(StudentMapper studentMapper, DormitoryAllocationMapper allocationMapper, NotificationMapper notificationMapper, PaymentRecordMapper paymentRecordMapper, BedMapper bedMapper, BCryptPasswordEncoder passwordEncoder, JwtUtils jwtUtils) {
        this.studentMapper = studentMapper;
        this.allocationMapper = allocationMapper;
        this.notificationMapper = notificationMapper;
        this.paymentRecordMapper = paymentRecordMapper;
        this.bedMapper = bedMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }


    @PostMapping("/login")
    @Operation(summary = "学生登录", description = "学生使用学号和密码登录系统")
    public ApiResponse<JwtResponse> login(@RequestBody @Valid LoginRequest req) {
        try {
            System.out.println("Login attempt for student: " + req.getId());
            
            Student s = studentMapper.selectById(req.getId());
            if (s == null) {
                System.err.println("Student not found: " + req.getId());
                return ApiResponse.error("学号或密码错误");
            }
            
            System.out.println("Student found: " + s.getName());
            System.out.println("Password hash: " + s.getPassword().substring(0, Math.min(30, s.getPassword().length())) + "...");
            
            boolean passwordMatch = passwordEncoder.matches(req.getPassword(), s.getPassword());
            System.out.println("Password match: " + passwordMatch);
            
            if (!passwordMatch) {
                return ApiResponse.error("学号或密码错误");
            }
            
            // 生成JWT token
            String accessToken = jwtUtils.generateAccessToken(s.getId(), "student");
            String refreshToken = jwtUtils.generateRefreshToken(s.getId(), "student");
            
            // 清空密码信息
            s.setPassword(null);
            
            JwtResponse jwtResponse = new JwtResponse(accessToken, refreshToken, 600L, s);
            System.out.println("Login successful for student: " + s.getId());
            return ApiResponse.ok(jwtResponse);
        } catch (Exception e) {
            System.err.println("Login error: " + e.getMessage());
            e.printStackTrace();
            return ApiResponse.error("登录失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取学生信息", description = "根据学号获取学生详细信息")
    public ApiResponse<Student> profile(@PathVariable @Parameter(description = "学号") String id) {
        Student s = studentMapper.selectById(id);
        if (s == null) return ApiResponse.error("学生不存在");
        // 注意：这里的 s.setPassword(null) 需要 Student 类中有相应方法
        s.setPassword(null);
        return ApiResponse.ok(s);
    }

    @PutMapping("/{id}")
    public ApiResponse<?> update(@PathVariable String id, @RequestBody Student update) {
        Student s = studentMapper.selectById(id);
        if (s == null) return ApiResponse.error("学生不存在");
        // 注意：这里的 update.getName() 和 s.setName() 等方法需要 Student 类中有相应方法
        if (update.getName() != null) s.setName(update.getName());
        if (update.getPhoneNumber() != null) s.setPhoneNumber(update.getPhoneNumber());
        if (update.getEmail() != null) s.setEmail(update.getEmail());
        if (update.getCollege() != null) s.setCollege(update.getCollege());
        if (update.getMajor() != null) s.setMajor(update.getMajor());
        if (update.getGender() != null) s.setGender(update.getGender());
        studentMapper.updateById(s);
        return ApiResponse.ok();
    }

    @GetMapping("/{id}/allocations")
    public ApiResponse<List<DormitoryAllocation>> getStudentAllocations(@PathVariable String id) {
        try {
            List<DormitoryAllocation> allocations = allocationMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<DormitoryAllocation>()
                    .eq("student_id", id)
                    .orderByDesc("check_in_date")
            );
            return ApiResponse.ok(allocations);
        } catch (Exception e) {
            return ApiResponse.error("获取住宿记录失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/current-allocation")
    public ApiResponse<Map<String, Object>> getCurrentAllocation(@PathVariable String id) {
        try {
            List<DormitoryAllocation> allocations = allocationMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<DormitoryAllocation>()
                    .eq("student_id", id)
                    .eq("status", "在住")
                    .orderByDesc("check_in_date")
            );
            
            if (allocations.isEmpty()) {
                return ApiResponse.ok(null);
            }
            
            // 获取最新的在住记录
            DormitoryAllocation allocation = allocations.get(0);
            
            // 根据bedId获取床位信息，得到dormitoryId
            com.ihome.entity.Bed bed = bedMapper.selectById(allocation.getBedId());
            
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("studentId", allocation.getStudentId());
            result.put("bedId", allocation.getBedId());
            result.put("dormitoryId", bed != null ? bed.getDormitoryId() : null);
            result.put("bedNumber", bed != null ? bed.getBedNumber() : null);
            result.put("bedType", bed != null ? bed.getBedType() : null);
            result.put("checkInDate", allocation.getCheckInDate());
            result.put("status", allocation.getStatus());
            
            return ApiResponse.ok(result);
        } catch (Exception e) {
            return ApiResponse.error("获取当前住宿信息失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/notifications")
    public ApiResponse<List<Notification>> getStudentNotifications(@PathVariable String id) {
        try {
            // 获取所有通知（学生可以看到所有公开通知）
            List<Notification> notifications = notificationMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Notification>()
                    .orderByDesc("create_time")
            );
            return ApiResponse.ok(notifications);
        } catch (Exception e) {
            return ApiResponse.error("获取通知失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/dormitory")
    public ApiResponse<DormitoryAllocation> getStudentDormitory(@PathVariable String id) {
        try {
            // 获取学生当前的住宿信息
            List<DormitoryAllocation> allocations = allocationMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<DormitoryAllocation>()
                    .eq("student_id", id)
                    .eq("status", "在住")
                    .orderByDesc("check_in_date")
            );
            
            if (allocations.isEmpty()) {
                return ApiResponse.ok(null);
            }
            
            // 返回最新的在住记录
            return ApiResponse.ok(allocations.get(0));
        } catch (Exception e) {
            return ApiResponse.error("获取宿舍信息失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/payments")
    public ApiResponse<List<PaymentRecord>> getStudentPayments(@PathVariable String id) {
        try {
            // 获取学生的缴费记录
            List<PaymentRecord> payments = paymentRecordMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<PaymentRecord>()
                    .eq("student_id", id)
                    .orderByDesc("payment_time")
            );
            return ApiResponse.ok(payments);
        } catch (Exception e) {
            return ApiResponse.error("获取缴费记录失败: " + e.getMessage());
        }
    }


    // 删除了 @Data 注解，手动编写 getter/setter
    public static class LoginRequest {
        @NotBlank
        private String id;
        @NotBlank
        private String password;

        // 手动添加 Getter 方法
        public String getId() { return id; }
        public String getPassword() { return password; }

        // 手动添加 Setter 方法
        public void setId(String id) { this.id = id; }
        public void setPassword(String password) { this.password = password; }
    }
}