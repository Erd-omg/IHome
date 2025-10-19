package com.ihome.controller;

import com.ihome.common.ApiResponse;
import com.ihome.common.JwtResponse;
import com.ihome.common.JwtUtils;
import com.ihome.common.OperationLog;
import com.ihome.entity.Student;
import com.ihome.entity.DormitoryAllocation;
import com.ihome.entity.Notification;
import com.ihome.mapper.StudentMapper;
import com.ihome.mapper.DormitoryAllocationMapper;
import com.ihome.mapper.NotificationMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/students")
public class StudentController {
    private final StudentMapper studentMapper;
    private final DormitoryAllocationMapper allocationMapper;
    private final NotificationMapper notificationMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public StudentController(StudentMapper studentMapper, DormitoryAllocationMapper allocationMapper, NotificationMapper notificationMapper, BCryptPasswordEncoder passwordEncoder, JwtUtils jwtUtils) {
        this.studentMapper = studentMapper;
        this.allocationMapper = allocationMapper;
        this.notificationMapper = notificationMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/register")
    @OperationLog(module = "学生管理", operationType = "CREATE", description = "学生注册")
    public ApiResponse<?> register(@RequestBody @Valid RegisterRequest req) {
        if (studentMapper.selectById(req.getId()) != null) {
            return ApiResponse.error("学号已存在");
        }
        Student s = new Student();
        s.setId(req.getId());
        s.setName(req.getName());
        s.setPhoneNumber(req.getPhoneNumber());
        s.setEmail(req.getEmail());
        s.setGender(req.getGender());
        s.setCollege(req.getCollege());
        s.setMajor(req.getMajor());
        s.setPassword(passwordEncoder.encode(req.getPassword()));
        studentMapper.insert(s);
        return ApiResponse.ok();
    }

    @PostMapping("/login")
    @OperationLog(module = "学生管理", operationType = "QUERY", description = "学生登录")
    public ApiResponse<JwtResponse> login(@RequestBody @Valid LoginRequest req) {
        Student s = studentMapper.selectById(req.getId());
        if (s == null || !passwordEncoder.matches(req.getPassword(), s.getPassword())) {
            return ApiResponse.error("学号或密码错误");
        }
        
        // 生成JWT token
        String accessToken = jwtUtils.generateAccessToken(s.getId(), "student");
        String refreshToken = jwtUtils.generateRefreshToken(s.getId(), "student");
        
        // 清空密码信息
        s.setPassword(null);
        
        JwtResponse jwtResponse = new JwtResponse(accessToken, refreshToken, 600L, s);
        return ApiResponse.ok(jwtResponse);
    }

    @GetMapping("/{id}")
    public ApiResponse<Student> profile(@PathVariable String id) {
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
    public ApiResponse<DormitoryAllocation> getCurrentAllocation(@PathVariable String id) {
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
            
            // 返回最新的在住记录
            return ApiResponse.ok(allocations.get(0));
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

    // 删除了 @Data 注解，手动编写 getter/setter
    public static class RegisterRequest {
        @NotBlank
        private String id;
        @NotBlank
        private String name;
        private String phoneNumber;
        private String email;
        @NotBlank
        private String password;
        @NotBlank
        private String gender;
        @NotBlank
        private String college;
        @NotBlank
        private String major;

        // 手动添加 Getter 方法
        public String getId() { return id; }
        public String getName() { return name; }
        public String getPhoneNumber() { return phoneNumber; }
        public String getEmail() { return email; }
        public String getPassword() { return password; }
        public String getGender() { return gender; }
        public String getCollege() { return college; }
        public String getMajor() { return major; }

        // 手动添加 Setter 方法
        public void setId(String id) { this.id = id; }
        public void setName(String name) { this.name = name; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        public void setEmail(String email) { this.email = email; }
        public void setPassword(String password) { this.password = password; }
        public void setGender(String gender) { this.gender = gender; }
        public void setCollege(String college) { this.college = college; }
        public void setMajor(String major) { this.major = major; }
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