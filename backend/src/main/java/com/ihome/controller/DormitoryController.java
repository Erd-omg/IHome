package com.ihome.controller;

import com.ihome.common.ApiResponse;
import com.ihome.entity.Bed;
import com.ihome.entity.Dormitory;
import com.ihome.service.DormitoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dorms")
public class DormitoryController {
    private final DormitoryService dormitoryService;

    public DormitoryController(DormitoryService dormitoryService) {
        this.dormitoryService = dormitoryService;
    }

    @GetMapping
    public ApiResponse<List<Dormitory>> list(@RequestParam(required = false) String buildingId,
                                             @RequestParam(required = false) String status) {
        return ApiResponse.ok(dormitoryService.listDormitories(buildingId, status));
    }

    @GetMapping("/{dormitoryId}/beds")
    public ApiResponse<List<Bed>> listBeds(@PathVariable String dormitoryId,
                                           @RequestParam(required = false) String status) {
        return ApiResponse.ok(dormitoryService.listBeds(dormitoryId, status));
    }

    @PostMapping("/choose-bed")
    public ApiResponse<Void> chooseBed(@RequestParam String studentId, @RequestParam String bedId) {
        dormitoryService.chooseBed(studentId, bedId);
        return ApiResponse.ok();
    }

    @PostMapping("/checkout")
    public ApiResponse<Void> checkout(@RequestParam String studentId) {
        dormitoryService.checkout(studentId);
        return ApiResponse.ok();
    }
}


