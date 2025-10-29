package com.ihome.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ihome.entity.RoommateTag;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface RoommateTagMapper extends BaseMapper<RoommateTag> {
    
    @Select("SELECT * FROM roommate_tags WHERE student_id = #{studentId}")
    List<RoommateTag> selectByStudentId(String studentId);

    @Delete("DELETE FROM roommate_tags WHERE student_id = #{studentId}")
    void deleteByStudentId(@Param("studentId") String studentId);

    @Delete("DELETE FROM roommate_tags WHERE student_id = #{studentId} AND tag_name = #{tagName}")
    void deleteByStudentIdAndTagName(@Param("studentId") String studentId, @Param("tagName") String tagName);

    @Select("SELECT COUNT(*) FROM roommate_tags")
    int countTotalTagUsage();

    @Select("SELECT tag_name, COUNT(*) as usage_count FROM roommate_tags GROUP BY tag_name ORDER BY usage_count DESC LIMIT #{limit}")
    List<Map<String, Object>> getPopularTags(@Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM roommate_tags WHERE tag_name = #{tagName}")
    int countByTagName(@Param("tagName") String tagName);
}