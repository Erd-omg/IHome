package com.ihome.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ihome.entity.RepairFeedback;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RepairFeedbackMapper extends BaseMapper<RepairFeedback> {

    /**
     * 根据维修订单ID查询反馈
     */
    @Select("SELECT * FROM repair_feedback WHERE repair_order_id = #{repairOrderId}")
    RepairFeedback selectByRepairOrderId(@Param("repairOrderId") Integer repairOrderId);

    /**
     * 根据学生ID查询反馈列表
     */
    @Select("SELECT * FROM repair_feedback WHERE student_id = #{studentId} ORDER BY created_at DESC")
    List<RepairFeedback> selectByStudentId(@Param("studentId") String studentId);

    /**
     * 查询所有反馈（支持筛选）
     */
    @Select("<script>" +
            "SELECT * FROM repair_feedback " +
            "<where>" +
            "<if test='repairOrderId != null and repairOrderId != \"\"'>" +
            "AND repair_order_id = #{repairOrderId}" +
            "</if>" +
            "<if test='studentId != null and studentId != \"\"'>" +
            "AND student_id = #{studentId}" +
            "</if>" +
            "<if test='rating != null'>" +
            "AND rating = #{rating}" +
            "</if>" +
            "</where>" +
            "ORDER BY created_at DESC " +
            "LIMIT #{offset}, #{size}" +
            "</script>")
    List<RepairFeedback> selectAllFeedback(@Param("page") int page, @Param("size") int size,
                                          @Param("repairOrderId") String repairOrderId,
                                          @Param("studentId") String studentId,
                                          @Param("rating") Integer rating);

    /**
     * 统计总反馈数
     */
    @Select("SELECT COUNT(*) FROM repair_feedback")
    int countTotalFeedbacks();

    /**
     * 获取平均评分
     */
    @Select("SELECT AVG(rating) FROM repair_feedback")
    Double getAverageRating();

    /**
     * 根据评分统计数量
     */
    @Select("SELECT COUNT(*) FROM repair_feedback WHERE rating = #{rating}")
    int countByRating(@Param("rating") int rating);

    /**
     * 统计有回复的反馈数
     */
    @Select("SELECT COUNT(*) FROM repair_feedback WHERE reply IS NOT NULL AND reply != ''")
    int countRepliedFeedbacks();
}

