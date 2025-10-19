package com.ihome.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ihome.entity.RoommateTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RoommateTagMapper extends BaseMapper<RoommateTag> {
    
    @Select("SELECT * FROM roommate_tags WHERE student_id = #{studentId}")
    List<RoommateTag> selectByStudentId(String studentId);
}