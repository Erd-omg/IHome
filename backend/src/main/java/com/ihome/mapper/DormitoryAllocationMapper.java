package com.ihome.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ihome.entity.DormitoryAllocation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DormitoryAllocationMapper extends BaseMapper<DormitoryAllocation> {
    
    @Select("SELECT da.* FROM dormitory_allocations da " +
            "JOIN beds b ON da.bed_id = b.id " +
            "WHERE b.dormitory_id = #{dormitoryId}")
    List<DormitoryAllocation> selectByDormitoryId(String dormitoryId);
    
    @Select("SELECT * FROM dormitory_allocations WHERE student_id = #{studentId} AND status = '在住' LIMIT 1")
    DormitoryAllocation selectByStudentId(String studentId);
}


