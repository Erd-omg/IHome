package com.ihome.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ihome.entity.ElectricityBill;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 电费账单Mapper
 */
@Mapper
public interface ElectricityBillMapper extends BaseMapper<ElectricityBill> {
    
    /**
     * 根据宿舍ID查询电费账单
     */
    @Select("SELECT * FROM electricity_bills WHERE dormitory_id = #{dormitoryId} ORDER BY bill_month DESC")
    List<ElectricityBill> selectByDormitoryId(@Param("dormitoryId") String dormitoryId);
    
    /**
     * 查询即将到期的账单
     */
    @Select("SELECT * FROM electricity_bills WHERE status = '未缴费' AND due_date <= DATE_ADD(NOW(), INTERVAL #{days} DAY)")
    List<ElectricityBill> selectExpiringBills(@Param("days") Integer days);
    
    /**
     * 查询余额不足的账单
     */
    @Select("SELECT * FROM electricity_bills WHERE current_balance <= #{threshold}")
    List<ElectricityBill> selectLowBalanceBills(@Param("threshold") Double threshold);
    
    /**
     * 根据月份查询账单
     */
    @Select("SELECT * FROM electricity_bills WHERE bill_month = #{billMonth}")
    List<ElectricityBill> selectByBillMonth(@Param("billMonth") String billMonth);
}

