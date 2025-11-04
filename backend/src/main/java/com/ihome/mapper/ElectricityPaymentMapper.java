package com.ihome.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ihome.entity.ElectricityPayment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 电费缴费记录Mapper
 */
@Mapper
public interface ElectricityPaymentMapper extends BaseMapper<ElectricityPayment> {
    
    /**
     * 根据学生ID查询缴费记录
     */
    @Select("SELECT * FROM electricity_payments WHERE student_id = #{studentId} ORDER BY payment_time DESC")
    List<ElectricityPayment> selectByStudentId(@Param("studentId") String studentId);
    
    /**
     * 根据宿舍ID查询缴费记录
     */
    @Select("SELECT * FROM electricity_payments WHERE dormitory_id = #{dormitoryId} ORDER BY payment_time DESC")
    List<ElectricityPayment> selectByDormitoryId(@Param("dormitoryId") String dormitoryId);
    
    /**
     * 根据账单ID查询缴费记录
     */
    @Select("SELECT * FROM electricity_payments WHERE bill_id = #{billId}")
    List<ElectricityPayment> selectByBillId(@Param("billId") Long billId);
    
    /**
     * 根据交易号查询缴费记录
     */
    @Select("SELECT * FROM electricity_payments WHERE transaction_id = #{transactionId}")
    ElectricityPayment selectByTransactionId(@Param("transactionId") String transactionId);
}

