package com.ihome.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ihome.entity.ElectricityReminder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 电费提醒设置Mapper
 */
@Mapper
public interface ElectricityReminderMapper extends BaseMapper<ElectricityReminder> {
    
    /**
     * 根据学生ID查询提醒设置
     */
    @Select("SELECT * FROM electricity_reminders WHERE student_id = #{studentId}")
    ElectricityReminder selectByStudentId(@Param("studentId") String studentId);
    
    /**
     * 根据宿舍ID查询提醒设置
     */
    @Select("SELECT * FROM electricity_reminders WHERE dormitory_id = #{dormitoryId}")
    List<ElectricityReminder> selectByDormitoryId(@Param("dormitoryId") String dormitoryId);
    
    /**
     * 查询启用余额提醒的设置
     */
    @Select("SELECT * FROM electricity_reminders WHERE balance_reminder_enabled = true")
    List<ElectricityReminder> selectEnabledBalanceReminders();
    
    /**
     * 查询启用截止日期提醒的设置
     */
    @Select("SELECT * FROM electricity_reminders WHERE due_date_reminder_enabled = true")
    List<ElectricityReminder> selectEnabledDueDateReminders();
}

