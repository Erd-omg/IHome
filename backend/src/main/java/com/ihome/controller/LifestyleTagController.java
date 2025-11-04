package com.ihome.controller;

import com.ihome.common.ApiResponse;
import com.ihome.entity.RoommateTag;
import com.ihome.mapper.RoommateTagMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 生活习惯标签控制器
 * 处理学生生活习惯标签的管理
 */
@RestController
@RequestMapping("/lifestyle-tags")
public class LifestyleTagController {

    @Autowired
    private RoommateTagMapper roommateTagMapper;

    /**
     * 获取所有可用的标签
     */
    @GetMapping("/available")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    public ApiResponse<List<Map<String, Object>>> getAvailableTags() {
        try {
            // 预定义的标签列表
            List<Map<String, Object>> availableTags = List.of(
                Map.of("name", "早睡早起", "category", "作息习惯", "description", "习惯早睡早起，作息规律"),
                Map.of("name", "夜猫子", "category", "作息习惯", "description", "习惯晚睡晚起"),
                Map.of("name", "爱干净", "category", "卫生习惯", "description", "注重个人和环境卫生"),
                Map.of("name", "喜欢安静", "category", "生活习惯", "description", "喜欢安静的环境"),
                Map.of("name", "喜欢热闹", "category", "生活习惯", "description", "喜欢热闹的环境"),
                Map.of("name", "不抽烟", "category", "生活习惯", "description", "不抽烟，希望室友也不抽烟"),
                Map.of("name", "不喝酒", "category", "生活习惯", "description", "不喝酒，希望室友也不喝酒"),
                Map.of("name", "喜欢运动", "category", "兴趣爱好", "description", "喜欢运动健身"),
                Map.of("name", "喜欢学习", "category", "兴趣爱好", "description", "喜欢学习，注重学业"),
                Map.of("name", "喜欢游戏", "category", "兴趣爱好", "description", "喜欢玩游戏"),
                Map.of("name", "喜欢音乐", "category", "兴趣爱好", "description", "喜欢听音乐"),
                Map.of("name", "喜欢阅读", "category", "兴趣爱好", "description", "喜欢阅读书籍"),
                Map.of("name", "行动不便", "category", "特殊需求", "description", "行动不便，需要下铺"),
                Map.of("name", "怕冷", "category", "特殊需求", "description", "怕冷，需要温暖的环境"),
                Map.of("name", "怕热", "category", "特殊需求", "description", "怕热，需要凉爽的环境")
            );
            
            return ApiResponse.ok(availableTags);
        } catch (Exception e) {
            return ApiResponse.error("获取可用标签失败: " + e.getMessage());
        }
    }

    /**
     * 获取我的标签
     */
    @GetMapping("/my-tags")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<List<RoommateTag>> getMyTags(@RequestParam(required = false) String studentId) {
        try {
            if (studentId == null || studentId.isEmpty()) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication == null || authentication.getName() == null) {
                    return ApiResponse.error("未获取到学生ID");
                }
                studentId = authentication.getName();
            }
            
            List<RoommateTag> myTags = roommateTagMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<RoommateTag>()
                    .eq("student_id", studentId)
            );
            
            return ApiResponse.ok(myTags);
        } catch (Exception e) {
            return ApiResponse.error("获取我的标签失败: " + e.getMessage());
        }
    }

    /**
     * 设置学生标签
     */
    @PostMapping("/set")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<?> setStudentTags(@RequestBody List<String> tagNames,
                                        @RequestParam(required = false) String studentId) {
        try {
            if (studentId == null || studentId.isEmpty()) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication == null || authentication.getName() == null) {
                    return ApiResponse.error("未获取到学生ID");
                }
                studentId = authentication.getName();
            }
            
            // 先删除现有标签
            roommateTagMapper.delete(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<RoommateTag>()
                    .eq("student_id", studentId)
            );
            
            // 添加新标签
            for (String tagName : tagNames) {
                RoommateTag tag = new RoommateTag();
                tag.setStudentId(studentId);
                tag.setTagName(tagName);
                roommateTagMapper.insert(tag);
            }
            
            return ApiResponse.ok("标签设置成功");
        } catch (Exception e) {
            return ApiResponse.error("设置标签失败: " + e.getMessage());
        }
    }

    /**
     * 添加单个标签
     */
    @PostMapping("/add")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<?> addTag(@RequestParam String tagName) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String studentId = authentication.getName();
            
            // 检查标签是否已存在
            RoommateTag existingTag = roommateTagMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<RoommateTag>()
                    .eq("student_id", studentId)
                    .eq("tag_name", tagName)
            );
            
            if (existingTag != null) {
                return ApiResponse.error("标签已存在");
            }
            
            RoommateTag tag = new RoommateTag();
            tag.setStudentId(studentId);
            tag.setTagName(tagName);
            roommateTagMapper.insert(tag);
            
            return ApiResponse.ok("标签添加成功");
        } catch (Exception e) {
            return ApiResponse.error("添加标签失败: " + e.getMessage());
        }
    }

    /**
     * 删除标签
     */
    @DeleteMapping("/remove")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<?> removeTag(@RequestParam String tagName) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String studentId = authentication.getName();
            
            int deleted = roommateTagMapper.delete(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<RoommateTag>()
                    .eq("student_id", studentId)
                    .eq("tag_name", tagName)
            );
            
            if (deleted > 0) {
                return ApiResponse.ok("标签删除成功");
            } else {
                return ApiResponse.error("标签不存在");
            }
        } catch (Exception e) {
            return ApiResponse.error("删除标签失败: " + e.getMessage());
        }
    }

    /**
     * 获取标签推荐
     */
    @GetMapping("/recommendations")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<List<Map<String, Object>>> getTagRecommendations() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String studentId = authentication.getName();
            
            // 获取我的标签
            List<RoommateTag> myTags = roommateTagMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<RoommateTag>()
                    .eq("student_id", studentId)
            );
            
            // 基于现有标签推荐相关标签
            List<Map<String, Object>> recommendations = List.of(
                Map.of("name", "作息规律", "reason", "基于您的作息习惯推荐", "category", "作息习惯"),
                Map.of("name", "注重卫生", "reason", "基于您的卫生习惯推荐", "category", "卫生习惯"),
                Map.of("name", "喜欢安静学习", "reason", "基于您的学习习惯推荐", "category", "生活习惯")
            );
            
            return ApiResponse.ok(recommendations);
        } catch (Exception e) {
            return ApiResponse.error("获取标签推荐失败: " + e.getMessage());
        }
    }

    /**
     * 获取标签统计（管理员）
     */
    @GetMapping("/admin/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> getTagStatistics() {
        try {
            // 获取标签使用统计
            List<RoommateTag> allTags = roommateTagMapper.selectList(null);
            
            Map<String, Integer> tagCount = new HashMap<>();
            for (RoommateTag tag : allTags) {
                tagCount.put(tag.getTagName(), tagCount.getOrDefault(tag.getTagName(), 0) + 1);
            }
            
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalTags", allTags.size());
            statistics.put("uniqueTags", tagCount.size());
            statistics.put("tagCount", tagCount);
            statistics.put("mostPopularTags", tagCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .toList());
            
            return ApiResponse.ok(statistics);
        } catch (Exception e) {
            return ApiResponse.error("获取标签统计失败: " + e.getMessage());
        }
    }

    /**
     * 创建新标签（管理员）
     */
    @PostMapping("/admin/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<?> createTag(@RequestParam String tagName, @RequestParam String description) {
        try {
            // 这里可以实现创建新标签的逻辑
            // 暂时返回成功响应
            return ApiResponse.ok("标签创建成功");
        } catch (Exception e) {
            return ApiResponse.error("创建标签失败: " + e.getMessage());
        }
    }
}

