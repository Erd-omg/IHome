package com.ihome.controller;

import com.ihome.common.ApiResponse;
import com.ihome.entity.Dormitory;
import com.ihome.entity.DormitoryAllocation;
import com.ihome.mapper.DormitoryMapper;
import com.ihome.mapper.DormitoryAllocationMapper;
import com.ihome.mapper.StudentMapper;
import com.ihome.mapper.BedMapper;
import com.ihome.service.DormitoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/dorms")
@Tag(name = "宿舍管理", description = "宿舍相关API接口")
public class DormitoryController {
    private final DormitoryMapper dormitoryMapper;
    private final DormitoryAllocationMapper allocationMapper;
    private final StudentMapper studentMapper;
    private final BedMapper bedMapper;
    private final DormitoryService dormitoryService;

    public DormitoryController(DormitoryMapper dormitoryMapper, 
                               DormitoryAllocationMapper allocationMapper,
                               StudentMapper studentMapper,
                               BedMapper bedMapper,
                               DormitoryService dormitoryService) {
        this.dormitoryMapper = dormitoryMapper;
        this.allocationMapper = allocationMapper;
        this.studentMapper = studentMapper;
        this.bedMapper = bedMapper;
        this.dormitoryService = dormitoryService;
    }

    @GetMapping
    @Operation(summary = "获取宿舍列表", description = "分页查询宿舍列表")
    public ApiResponse<?> listDorms(@RequestParam(required = false) String name,
                                   @RequestParam(required = false) String buildingId,
                                   @RequestParam(required = false) String status,
                                   @RequestParam(required = false) String startDate,
                                   @RequestParam(required = false) String endDate,
                                   @RequestParam(required = false) Integer page,
                                   @RequestParam(required = false) Integer size,
                                   @RequestParam(required = false) String sort) {
        try {
            var queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Dormitory>();
            
            // 模糊搜索宿舍号或房间号
            if (name != null && !name.isEmpty()) {
                queryWrapper.and(wrapper -> 
                    wrapper.like("id", name)
                           .or()
                           .like("room_number", name)
                );
            }
            
            // 筛选楼栋
            if (buildingId != null && !buildingId.isEmpty()) {
                queryWrapper.eq("building_id", buildingId);
            }
            
            // 筛选状态
            if (status != null && !status.isEmpty()) {
                queryWrapper.eq("status", status);
            }
            
            // 日期范围筛选
            if (startDate != null && !startDate.isEmpty()) {
                queryWrapper.ge("created_at", startDate);
            }
            if (endDate != null && !endDate.isEmpty()) {
                queryWrapper.le("created_at", endDate);
            }
            
            // 排序处理
            if (sort != null && !sort.isEmpty()) {
                String[] sortParts = sort.split(",");
                if (sortParts.length == 2) {
                    String sortField = sortParts[0];
                    String sortOrder = sortParts[1].toLowerCase();
                    
                    // 字段名映射
                    String dbField;
                    switch (sortField) {
                        case "id":
                            dbField = "id";
                            break;
                        case "buildingId":
                            dbField = "building_id";
                            break;
                        case "bedCount":
                            dbField = "bed_count";
                            break;
                        case "currentOccupancy":
                            dbField = "current_occupancy";
                            break;
                        default:
                            dbField = sortField;
                    }
                    
                    if ("asc".equals(sortOrder)) {
                        queryWrapper.orderByAsc(dbField);
                    } else {
                        queryWrapper.orderByDesc(dbField);
                    }
                }
            } else {
                // 默认按宿舍ID排序
                queryWrapper.orderByAsc("id");
            }
            
            List<Dormitory> dorms = dormitoryMapper.selectList(queryWrapper);
            return ApiResponse.ok(dorms);
        } catch (Exception e) {
            return ApiResponse.error("查询宿舍列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/{dormitoryId}/beds")
    @Operation(summary = "获取宿舍床位列表", description = "获取指定宿舍的所有床位")
    public ApiResponse<List<Map<String, Object>>> listBeds(@PathVariable String dormitoryId) {
        try {
            var beds = bedMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.ihome.entity.Bed>()
                    .eq("dormitory_id", dormitoryId)
            );
            
            List<Map<String, Object>> result = new ArrayList<>();
            
            for (var bed : beds) {
                Map<String, Object> bedInfo = new HashMap<>();
                bedInfo.put("id", bed.getId());
                bedInfo.put("dormitoryId", bed.getDormitoryId());
                bedInfo.put("bedNumber", bed.getBedNumber());
                bedInfo.put("bedType", bed.getBedType());
                bedInfo.put("status", bed.getStatus());
                
                // 查询该床位的入住学生
                var allocations = allocationMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<DormitoryAllocation>()
                        .eq("bed_id", bed.getId())
                        .eq("status", "在住")
                        .orderByDesc("check_in_date")
                        .last("LIMIT 1")
                );
                
                if (!allocations.isEmpty()) {
                    var allocation = allocations.get(0);
                    var student = studentMapper.selectById(allocation.getStudentId());
                    if (student != null) {
                        bedInfo.put("studentId", student.getId());
                        bedInfo.put("studentName", student.getName());
                        bedInfo.put("studentGender", student.getGender());
                        bedInfo.put("studentCollege", student.getCollege());
                    }
                }
                
                result.add(bedInfo);
            }
            
            return ApiResponse.ok(result);
        } catch (Exception e) {
            return ApiResponse.error("获取床位列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/{dormitoryId}/detail")
    @Operation(summary = "获取宿舍详细信息", description = "获取宿舍的完整信息，包括所有床位和入住学生")
    public ApiResponse<Map<String, Object>> getDormitoryDetail(@PathVariable String dormitoryId) {
        try {
            // 获取宿舍基本信息
            var dormitory = dormitoryMapper.selectById(dormitoryId);
            if (dormitory == null) {
                return ApiResponse.error("宿舍不存在");
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("id", dormitory.getId());
            result.put("buildingId", dormitory.getBuildingId());
            result.put("floorNumber", dormitory.getFloorNumber());
            result.put("roomNumber", dormitory.getRoomNumber());
            result.put("roomType", dormitory.getRoomType());
            result.put("bedCount", dormitory.getBedCount());
            result.put("currentOccupancy", dormitory.getCurrentOccupancy());
            result.put("status", dormitory.getStatus());
            
            // 获取所有床位及入住学生
            var beds = bedMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.ihome.entity.Bed>()
                    .eq("dormitory_id", dormitoryId)
            );
            
            List<Map<String, Object>> bedList = new ArrayList<>();
            for (var bed : beds) {
                Map<String, Object> bedInfo = new HashMap<>();
                bedInfo.put("id", bed.getId());
                bedInfo.put("bedNumber", bed.getBedNumber());
                bedInfo.put("bedType", bed.getBedType());
                bedInfo.put("status", bed.getStatus());
                
                // 查询该床位的入住学生
                var allocations = allocationMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<DormitoryAllocation>()
                        .eq("bed_id", bed.getId())
                        .eq("status", "在住")
                        .orderByDesc("check_in_date")
                        .last("LIMIT 1")
                );
                
                if (!allocations.isEmpty()) {
                    var allocation = allocations.get(0);
                    var student = studentMapper.selectById(allocation.getStudentId());
                    if (student != null) {
                        bedInfo.put("studentId", student.getId());
                        bedInfo.put("studentName", student.getName());
                        bedInfo.put("studentGender", student.getGender());
                        bedInfo.put("studentCollege", student.getCollege());
                    }
                }
                
                bedList.add(bedInfo);
            }
            
            result.put("beds", bedList);
            
            return ApiResponse.ok(result);
        } catch (Exception e) {
            return ApiResponse.error("获取宿舍详情失败: " + e.getMessage());
        }
    }

    @PostMapping("/checkout")
    @Operation(summary = "学生申请退宿", description = "学生申请退宿，释放床位并更新分配状态")
    public ApiResponse<Map<String, Object>> checkout(@RequestParam String studentId) {
        try {
            dormitoryService.checkout(studentId);
            return ApiResponse.ok(Map.of("message", "退宿申请已提交"));
        } catch (IllegalStateException e) {
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("申请退宿失败: " + e.getMessage());
        }
    }
}
