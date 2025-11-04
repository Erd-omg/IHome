package com.ihome.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ihome.common.ApiResponse;
import com.ihome.entity.DormitoryAllocation;
import com.ihome.mapper.DormitoryAllocationMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/allocations")
public class AllocationController {
    private final DormitoryAllocationMapper allocationMapper;

    public AllocationController(DormitoryAllocationMapper allocationMapper) {
        this.allocationMapper = allocationMapper;
    }

    @GetMapping("/student/{studentId}")
    public ApiResponse<DormitoryAllocation> getCurrentAllocation(@PathVariable String studentId) {
        DormitoryAllocation allocation = allocationMapper.selectOne(
                Wrappers.<DormitoryAllocation>lambdaQuery()
                        .eq(DormitoryAllocation::getStudentId, studentId)
                        .eq(DormitoryAllocation::getStatus, "在住")
        );
        if (allocation == null) {
            return ApiResponse.error("当前未在住");
        }
        return ApiResponse.ok(allocation);
    }

    @GetMapping("/student/{studentId}/history")
    public ApiResponse<List<DormitoryAllocation>> getAllocationHistory(@PathVariable String studentId) {
        List<DormitoryAllocation> allocations = allocationMapper.selectList(
                Wrappers.<DormitoryAllocation>lambdaQuery()
                        .eq(DormitoryAllocation::getStudentId, studentId)
                        .orderByDesc(DormitoryAllocation::getCheckInDate)
        );
        return ApiResponse.ok(allocations);
    }
}
