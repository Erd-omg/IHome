package com.ihome.util;

import com.ihome.entity.Student;
import com.ihome.entity.Dormitory;
import com.ihome.entity.Bed;
import com.ihome.entity.RepairOrder;
import com.ihome.entity.PaymentRecord;
import com.ihome.entity.DormitoryAllocation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 测试工具类
 */
public class TestUtils {

    /**
     * 创建测试学生
     */
    public static Student createTestStudent(String id, String name) {
        Student student = new Student();
        student.setId(id);
        student.setName(name);
        student.setPhoneNumber("13800138000");
        student.setEmail("test@example.com");
        student.setGender("男");
        student.setCollege("测试学院");
        student.setMajor("测试专业");
        student.setGrade("2024");
        student.setStatus("在校");
        return student;
    }

    /**
     * 创建测试宿舍
     */
    public static Dormitory createTestDormitory(String id, String buildingId, String roomNumber) {
        Dormitory dormitory = new Dormitory();
        dormitory.setId(id);
        dormitory.setBuildingId(buildingId);
        dormitory.setFloorNumber(1);
        dormitory.setRoomNumber(roomNumber);
        dormitory.setRoomType("四人间");
        dormitory.setBedCount(4);
        dormitory.setCurrentOccupancy(0);
        dormitory.setStatus("可用");
        return dormitory;
    }

    /**
     * 创建测试床位
     */
    public static Bed createTestBed(String id, String dormitoryId, String bedType) {
        Bed bed = new Bed();
        bed.setId(id);
        bed.setDormitoryId(dormitoryId);
        bed.setBedType(bedType);
        bed.setStatus("可用");
        return bed;
    }

    /**
     * 创建测试维修订单
     */
    public static RepairOrder createTestRepairOrder(String studentId, String dormitoryId) {
        RepairOrder order = new RepairOrder();
        order.setStudentId(studentId);
        order.setDormitoryId(dormitoryId);
        order.setDescription("测试维修描述");
        order.setRepairType("水电");
        order.setUrgencyLevel("一般");
        order.setStatus("待处理");
        order.setCreatedAt(LocalDateTime.now());
        return order;
    }

    /**
     * 创建测试缴费记录
     */
    public static PaymentRecord createTestPaymentRecord(String studentId) {
        PaymentRecord record = new PaymentRecord();
        record.setStudentId(studentId);
        record.setAmount(new BigDecimal("100.00"));
        record.setPaymentMethod("微信支付");
        record.setPaymentTime(LocalDateTime.now());
        return record;
    }

    /**
     * 创建测试分配记录
     */
    public static DormitoryAllocation createTestAllocation(String studentId, String bedId) {
        DormitoryAllocation allocation = new DormitoryAllocation();
        allocation.setStudentId(studentId);
        allocation.setBedId(bedId);
        allocation.setCheckInDate(LocalDate.now());
        allocation.setStatus("已入住");
        return allocation;
    }

    /**
     * 生成随机学号
     */
    public static String generateStudentId() {
        return "2024" + String.format("%03d", (int) (Math.random() * 1000));
    }

    /**
     * 生成随机宿舍ID
     */
    public static String generateDormitoryId() {
        return "D" + String.format("%02d", (int) (Math.random() * 100)) + "-" + 
               String.format("%03d", (int) (Math.random() * 1000));
    }

    /**
     * 生成随机床位ID
     */
    public static String generateBedId(String dormitoryId) {
        return dormitoryId + "-" + (int) (Math.random() * 4 + 1);
    }
}
