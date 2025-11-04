package com.ihome.controller;

import com.ihome.common.ApiResponse;
import com.ihome.entity.Bed;
import com.ihome.entity.RoommateTag;
import com.ihome.mapper.BedMapper;
import com.ihome.mapper.DormitoryAllocationMapper;
import com.ihome.mapper.RoommateTagMapper;
import com.ihome.mapper.StudentMapper;
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
 * 床位管理控制器
 * 处理床位选择、推荐等相关接口
 */
@RestController
@RequestMapping("/beds")
@Tag(name = "床位管理", description = "床位选择和管理相关API接口")
public class BedController {

    @Autowired
    private BedMapper bedMapper;

    @Autowired
    private DormitoryAllocationMapper allocationMapper;

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private RoommateTagMapper roommateTagMapper;

    /**
     * 获取学生当前床位信息
     */
    @GetMapping("/current")
    @Operation(summary = "获取当前床位", description = "获取学生当前分配的床位信息")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<Map<String, Object>> getCurrentBed() {
        try {
            String studentId = SecurityContextHolder.getContext().getAuthentication().getName();
            
            // 查找当前分配记录
            var allocations = allocationMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.ihome.entity.DormitoryAllocation>()
                    .eq("student_id", studentId)
                    .eq("status", "在住")
                    .orderByDesc("check_in_date")
                    .last("LIMIT 1")
            );

            if (allocations.isEmpty()) {
                return ApiResponse.ok(null);
            }

            var allocation = allocations.get(0);
            Bed bed = bedMapper.selectById(allocation.getBedId());
            
            if (bed == null) {
                return ApiResponse.ok(null);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("bedId", bed.getId());
            result.put("bedType", bed.getBedType());
            result.put("roomNumber", bed.getDormitoryId());
            result.put("buildingId", bed.getDormitoryId().split("-")[0]);
            result.put("checkInDate", allocation.getCheckInDate());
            result.put("status", "已入住");

            return ApiResponse.ok(result);
        } catch (Exception e) {
            return ApiResponse.error("获取当前床位失败: " + e.getMessage());
        }
    }

    /**
     * 获取推荐床位
     */
    @GetMapping("/recommended")
    @Operation(summary = "获取推荐床位", description = "获取基于特殊标签（如行动不便）推荐的床位列表")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<List<Map<String, Object>>> getRecommendedBeds() {
        try {
            String studentId = SecurityContextHolder.getContext().getAuthentication().getName();
            
            // 获取学生信息
            var student = studentMapper.selectById(studentId);
            if (student == null) {
                return ApiResponse.error("学生不存在");
            }

            // 获取学生的特殊标签
            List<RoommateTag> tags = roommateTagMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<RoommateTag>()
                    .eq("student_id", studentId)
            );

            List<String> tagNames = tags.stream()
                .map(RoommateTag::getTagName)
                .collect(Collectors.toList());

            // 如果学生有"行动不便"标签，优先推荐下铺
            boolean needLowerBed = tagNames.contains("行动不便");

            // 获取可用床位
            List<Bed> availableBeds = bedMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Bed>()
                    .eq("status", "可用")
            );

            List<Map<String, Object>> recommendedBeds = new ArrayList<>();

            for (Bed bed : availableBeds) {
                Map<String, Object> bedInfo = new HashMap<>();
                bedInfo.put("id", bed.getId());
                bedInfo.put("bedType", bed.getBedType());
                bedInfo.put("dormitoryId", bed.getDormitoryId());
                bedInfo.put("status", bed.getStatus());

                // 如果学生需要下铺，优先推荐下铺
                if (needLowerBed && "下铺".equals(bed.getBedType())) {
                    recommendedBeds.add(0, bedInfo); // 添加到开头
                } else {
                    recommendedBeds.add(bedInfo);
                }
            }

            // 限制推荐数量为10个
            if (recommendedBeds.size() > 10) {
                recommendedBeds = recommendedBeds.subList(0, 10);
            }

            return ApiResponse.ok(recommendedBeds);
        } catch (Exception e) {
            return ApiResponse.error("获取推荐床位失败: " + e.getMessage());
        }
    }

    /**
     * 获取可用床位列表
     */
    @GetMapping("/available")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<List<Map<String, Object>>> getAvailableBeds(
            @RequestParam(required = false) String dormitoryId) {
        try {
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Bed> queryWrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Bed>()
                    .eq("status", "可用");

            if (dormitoryId != null && !dormitoryId.isEmpty()) {
                queryWrapper.eq("dormitory_id", dormitoryId);
            }

            List<Bed> beds = bedMapper.selectList(queryWrapper);

            List<Map<String, Object>> bedList = beds.stream()
                .map(bed -> {
                    Map<String, Object> bedInfo = new HashMap<>();
                    bedInfo.put("id", bed.getId());
                    bedInfo.put("bedType", bed.getBedType());
                    bedInfo.put("dormitoryId", bed.getDormitoryId());
                    bedInfo.put("status", bed.getStatus());
                    return bedInfo;
                })
                .collect(Collectors.toList());

            return ApiResponse.ok(bedList);
        } catch (Exception e) {
            return ApiResponse.error("获取可用床位失败: " + e.getMessage());
        }
    }

    /**
     * 搜索可用床位
     */
    @GetMapping("/search")
    @Operation(summary = "搜索床位", description = "根据宿舍号或床位号搜索可用床位")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<List<Map<String, Object>>> searchBeds(@RequestParam(required = false) @Parameter(description = "搜索关键词") String keyword) {
        try {
            List<Bed> beds;
            
            if (keyword != null && !keyword.trim().isEmpty()) {
                // 根据关键词搜索床位
                beds = bedMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Bed>()
                        .eq("status", "可用")
                        .and(wrapper -> wrapper
                            .like("id", keyword)
                            .or()
                            .like("dormitory_id", keyword)
                            .or()
                            .like("bed_number", keyword)
                        )
                );
            } else {
                // 获取所有可用床位
                beds = bedMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Bed>()
                        .eq("status", "可用")
                );
            }
            
            List<Map<String, Object>> result = beds.stream()
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

    /**
     * 选择床位
     */
    @PostMapping("/select")
    @Operation(summary = "选择床位", description = "学生选择心仪的床位")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<?> selectBed(@RequestParam @Parameter(description = "床位ID") String bedId) {
        try {
            String studentId = SecurityContextHolder.getContext().getAuthentication().getName();

            // 检查床位是否存在且可用
            Bed bed = bedMapper.selectById(bedId);
            if (bed == null) {
                return ApiResponse.error("床位不存在");
            }

            if (!"可用".equals(bed.getStatus())) {
                return ApiResponse.error("床位不可用");
            }

            // 检查学生是否已有床位
            var existingAllocations = allocationMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.ihome.entity.DormitoryAllocation>()
                    .eq("student_id", studentId)
                    .eq("status", "在住")
            );

            if (!existingAllocations.isEmpty()) {
                return ApiResponse.error("您已经有床位了");
            }

            // 创建分配记录
            com.ihome.entity.DormitoryAllocation allocation = new com.ihome.entity.DormitoryAllocation();
            allocation.setStudentId(studentId);
            allocation.setBedId(bedId);
            allocation.setStatus("在住");
            allocation.setCheckInDate(java.time.LocalDate.now());

            allocationMapper.insert(allocation);

            // 更新床位状态
            bed.setStatus("已占用");
            bedMapper.updateById(bed);

            return ApiResponse.ok("床位选择成功");
        } catch (Exception e) {
            return ApiResponse.error("选择床位失败: " + e.getMessage());
        }
    }
}

