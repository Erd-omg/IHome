package com.ihome.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ihome.entity.QuestionnaireAnswer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface QuestionnaireAnswerMapper extends BaseMapper<QuestionnaireAnswer> {
    
    @Select("SELECT * FROM questionnaire_answers WHERE student_id = #{studentId}")
    QuestionnaireAnswer selectByStudentId(String studentId);
}