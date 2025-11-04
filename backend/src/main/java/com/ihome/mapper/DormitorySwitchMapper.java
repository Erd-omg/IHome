package com.ihome.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ihome.entity.DormitorySwitch;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DormitorySwitchMapper extends BaseMapper<DormitorySwitch> {
    
    /**
     * 根据申请人ID查询调换申请
     */
    @Select("SELECT * FROM dormitory_switches WHERE applicant_id = #{applicantId} ORDER BY apply_time DESC")
    List<DormitorySwitch> selectByApplicantId(String applicantId);
    
    /**
     * 根据状态查询调换申请
     */
    @Select("SELECT * FROM dormitory_switches WHERE status = #{status} ORDER BY apply_time DESC")
    List<DormitorySwitch> selectByStatus(String status);
    
    /**
     * 查询待审核的调换申请
     */
    @Select("SELECT * FROM dormitory_switches WHERE status = '待审核' ORDER BY apply_time ASC")
    List<DormitorySwitch> selectPendingSwitches();
    
    /**
     * 根据目标学生ID查询相关调换申请
     */
    @Select("SELECT * FROM dormitory_switches WHERE target_student_id = #{targetStudentId} AND status = '待审核'")
    List<DormitorySwitch> selectByTargetStudentId(String targetStudentId);
}

