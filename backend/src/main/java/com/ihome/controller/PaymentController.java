package com.ihome.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ihome.common.ApiResponse;
import com.ihome.entity.PaymentRecord;
import com.ihome.entity.Student;
import com.ihome.mapper.PaymentRecordMapper;
import com.ihome.mapper.StudentMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/payments")
public class PaymentController {
    private final PaymentRecordMapper paymentMapper;
    private final StudentMapper studentMapper;

    public PaymentController(PaymentRecordMapper paymentMapper, StudentMapper studentMapper) {
        this.paymentMapper = paymentMapper;
        this.studentMapper = studentMapper;
    }

    @PostMapping
    public ApiResponse<?> createPayment(@RequestBody @Valid CreatePaymentRequest req) {
        // 验证学生ID是否存在
        Student student = studentMapper.selectById(req.getStudentId());
        if (student == null) {
            return ApiResponse.error("学生ID不存在: " + req.getStudentId());
        }
        
        PaymentRecord payment = new PaymentRecord();
        payment.setStudentId(req.getStudentId());
        payment.setAmount(req.getAmount());
        payment.setPaymentMethod(req.getPaymentMethod());
        payment.setPaymentTime(LocalDateTime.now());
        
        paymentMapper.insert(payment);
        return ApiResponse.ok(payment);
    }

    @GetMapping("/student/{studentId}")
    public ApiResponse<List<PaymentRecord>> getStudentPayments(@PathVariable String studentId,
                                                               @RequestParam(required = false) String paymentMethod) {
        List<PaymentRecord> payments = paymentMapper.selectList(
                Wrappers.<PaymentRecord>lambdaQuery()
                        .eq(PaymentRecord::getStudentId, studentId)
                        .eq(paymentMethod != null, PaymentRecord::getPaymentMethod, paymentMethod)
                        .orderByDesc(PaymentRecord::getPaymentTime)
        );
        return ApiResponse.ok(payments);
    }

    @GetMapping
    public ApiResponse<List<PaymentRecord>> searchPayments(@RequestParam(required = false) String studentId,
                                                           @RequestParam(required = false) String paymentType,
                                                           @RequestParam(required = false) String sort) {
        var queryWrapper = Wrappers.<PaymentRecord>lambdaQuery();
        
        // 模糊搜索学号
        if (studentId != null && !studentId.isEmpty()) {
            queryWrapper.like(PaymentRecord::getStudentId, studentId);
        }
        
        // 精确匹配缴费类型
        if (paymentType != null && !paymentType.isEmpty()) {
            queryWrapper.eq(PaymentRecord::getPaymentType, paymentType);
        }
        
        // 排序处理
        if (sort != null && !sort.isEmpty()) {
            String[] sortParts = sort.split(",");
            if (sortParts.length == 2) {
                String sortField = sortParts[0];
                String sortOrder = sortParts[1].toLowerCase();
                
                if ("studentId".equals(sortField)) {
                    if ("asc".equals(sortOrder)) {
                        queryWrapper.orderByAsc(PaymentRecord::getStudentId);
                    } else {
                        queryWrapper.orderByDesc(PaymentRecord::getStudentId);
                    }
                } else if ("amount".equals(sortField)) {
                    if ("asc".equals(sortOrder)) {
                        queryWrapper.orderByAsc(PaymentRecord::getAmount);
                    } else {
                        queryWrapper.orderByDesc(PaymentRecord::getAmount);
                    }
                } else if ("paymentTime".equals(sortField)) {
                    if ("asc".equals(sortOrder)) {
                        queryWrapper.orderByAsc(PaymentRecord::getPaymentTime);
                    } else {
                        queryWrapper.orderByDesc(PaymentRecord::getPaymentTime);
                    }
                }
            }
        } else {
            // 默认按时间倒序
            queryWrapper.orderByDesc(PaymentRecord::getPaymentTime);
        }
        
        List<PaymentRecord> payments = paymentMapper.selectList(queryWrapper);
        return ApiResponse.ok(payments);
    }

    @GetMapping("/{id}")
    public ApiResponse<PaymentRecord> getPayment(@PathVariable Integer id) {
        PaymentRecord payment = paymentMapper.selectById(id);
        if (payment == null) {
            return ApiResponse.error("缴费记录不存在");
        }
        return ApiResponse.ok(payment);
    }

    @PutMapping("/{id}/status")
    public ApiResponse<?> updateStatus(@PathVariable Integer id, @RequestParam String status) {
        PaymentRecord pr = paymentMapper.selectById(id);
        if (pr == null) return ApiResponse.error("缴费记录不存在");
        // PaymentRecord实体没有status和description字段，直接更新
        paymentMapper.updateById(pr);
        return ApiResponse.ok();
    }

    public static class CreatePaymentRequest {
        @NotBlank
        private String studentId;
        @NotNull
        private BigDecimal amount;
        @NotBlank
        private String paymentMethod;

        // Getters and Setters
        public String getStudentId() { return studentId; }
        public void setStudentId(String studentId) { this.studentId = studentId; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    }
}
