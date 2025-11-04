package com.ihome.service;

import com.ihome.entity.*;
import com.ihome.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 电费管理服务
 */
@Service
public class ElectricityService {

    @Autowired
    private ElectricityBillMapper billMapper;
    
    @Autowired
    private ElectricityReminderMapper reminderMapper;
    
    @Autowired
    private ElectricityPaymentMapper paymentMapper;
    
    @Autowired
    private StudentMapper studentMapper;
    
    @Autowired
    private DormitoryAllocationMapper allocationMapper;
    
    @Autowired
    private NotificationService notificationService;

    /**
     * 创建电费账单
     */
    @Transactional
    public Map<String, Object> createElectricityBill(String dormitoryId, String billMonth, 
                                                    BigDecimal electricityUsage, BigDecimal unitPrice) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 检查是否已存在该月份的账单
            List<ElectricityBill> existingBills = billMapper.selectByBillMonth(billMonth);
            boolean billExists = existingBills.stream()
                    .anyMatch(bill -> bill.getDormitoryId().equals(dormitoryId));
            
            if (billExists) {
                result.put("success", false);
                result.put("message", "该月份账单已存在");
                return result;
            }
            
            // 创建新账单
            ElectricityBill bill = new ElectricityBill();
            bill.setDormitoryId(dormitoryId);
            bill.setBillMonth(billMonth);
            bill.setElectricityUsage(electricityUsage);
            bill.setUnitPrice(unitPrice);
            bill.setTotalAmount(electricityUsage.multiply(unitPrice));
            bill.setCurrentBalance(BigDecimal.ZERO);
            bill.setStatus("未缴费");
            bill.setDueDate(LocalDateTime.now().plusDays(30)); // 30天后到期
            bill.setCreatedAt(LocalDateTime.now());
            bill.setUpdatedAt(LocalDateTime.now());
            
            billMapper.insert(bill);
            
            result.put("success", true);
            result.put("message", "电费账单创建成功");
            result.put("billId", bill.getId());
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "创建电费账单失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 获取宿舍电费账单
     */
    public List<ElectricityBill> getDormitoryBills(String dormitoryId) {
        return billMapper.selectByDormitoryId(dormitoryId);
    }

    /**
     * 获取学生电费账单
     */
    public List<ElectricityBill> getStudentBills(String studentId) {
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            return new ArrayList<>();
        }
        
        // 获取学生当前分配的宿舍
        DormitoryAllocation allocation = getCurrentAllocation(studentId);
        if (allocation == null) {
            return new ArrayList<>();
        }
        
        // 从床位ID获取宿舍ID
        String dormitoryId = allocation.getBedId().substring(0, allocation.getBedId().lastIndexOf("-"));
        return billMapper.selectByDormitoryId(dormitoryId);
    }

    /**
     * 设置电费提醒
     */
    @Transactional
    public Map<String, Object> setElectricityReminder(String studentId, BigDecimal balanceThreshold, 
                                                     Integer dueDateReminderDays, String reminderMethod) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取学生当前宿舍
            DormitoryAllocation allocation = getCurrentAllocation(studentId);
            if (allocation == null) {
                result.put("success", false);
                result.put("message", "学生未分配宿舍");
                return result;
            }
            
            // 检查是否已存在提醒设置
            ElectricityReminder existingReminder = reminderMapper.selectByStudentId(studentId);
            
            ElectricityReminder reminder;
            if (existingReminder != null) {
                reminder = existingReminder;
            } else {
                reminder = new ElectricityReminder();
                reminder.setStudentId(studentId);
                // 从床位ID获取宿舍ID
                String dormitoryId = allocation.getBedId().substring(0, allocation.getBedId().lastIndexOf("-"));
                reminder.setDormitoryId(dormitoryId);
                reminder.setCreatedAt(LocalDateTime.now());
            }
            
            // 更新提醒设置
            reminder.setBalanceThreshold(balanceThreshold);
            reminder.setBalanceReminderEnabled(balanceThreshold.compareTo(BigDecimal.ZERO) > 0);
            reminder.setDueDateReminderDays(dueDateReminderDays);
            reminder.setDueDateReminderEnabled(dueDateReminderDays > 0);
            reminder.setReminderMethod(reminderMethod);
            reminder.setUpdatedAt(LocalDateTime.now());
            
            if (existingReminder != null) {
                reminderMapper.updateById(reminder);
            } else {
                reminderMapper.insert(reminder);
            }
            
            result.put("success", true);
            result.put("message", "电费提醒设置成功");
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "设置电费提醒失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 获取学生提醒设置
     */
    public ElectricityReminder getStudentReminderSettings(String studentId) {
        return reminderMapper.selectByStudentId(studentId);
    }

    /**
     * 电费缴费
     */
    @Transactional
    public Map<String, Object> payElectricityBill(Long billId, String studentId, 
                                                 BigDecimal paymentAmount, String paymentMethod) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取账单信息
            ElectricityBill bill = billMapper.selectById(billId);
            if (bill == null) {
                result.put("success", false);
                result.put("message", "账单不存在");
                return result;
            }
            
            // 检查缴费金额
            if (paymentAmount.compareTo(bill.getTotalAmount()) < 0) {
                result.put("success", false);
                result.put("message", "缴费金额不足");
                return result;
            }
            
            // 创建缴费记录
            ElectricityPayment payment = new ElectricityPayment();
            payment.setBillId(billId);
            payment.setStudentId(studentId);
            payment.setDormitoryId(bill.getDormitoryId());
            payment.setPaymentAmount(paymentAmount);
            payment.setPaymentMethod(paymentMethod);
            payment.setPaymentStatus("已支付");
            payment.setTransactionId(generateTransactionId());
            payment.setPaymentTime(LocalDateTime.now());
            payment.setCreatedAt(LocalDateTime.now());
            payment.setUpdatedAt(LocalDateTime.now());
            
            paymentMapper.insert(payment);
            
            // 更新账单状态
            bill.setStatus("已缴费");
            bill.setPaymentTime(LocalDateTime.now());
            bill.setCurrentBalance(bill.getCurrentBalance().add(paymentAmount));
            bill.setUpdatedAt(LocalDateTime.now());
            
            billMapper.updateById(bill);
            
            result.put("success", true);
            result.put("message", "缴费成功");
            result.put("paymentId", payment.getId());
            result.put("transactionId", payment.getTransactionId());
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "缴费失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 获取缴费记录
     */
    public List<ElectricityPayment> getPaymentHistory(String studentId) {
        return paymentMapper.selectByStudentId(studentId);
    }

    /**
     * 定时检查电费余额提醒
     */
    @Scheduled(cron = "0 0 9 * * ?") // 每天上午9点执行
    public void checkBalanceReminders() {
        List<ElectricityReminder> enabledReminders = reminderMapper.selectEnabledBalanceReminders();
        
        for (ElectricityReminder reminder : enabledReminders) {
            try {
                // 获取宿舍最新账单
                List<ElectricityBill> bills = billMapper.selectByDormitoryId(reminder.getDormitoryId());
                if (bills.isEmpty()) {
                    continue;
                }
                
                ElectricityBill latestBill = bills.get(0);
                
                // 检查余额是否低于阈值
                if (latestBill.getCurrentBalance().compareTo(reminder.getBalanceThreshold()) <= 0) {
                    // 发送提醒通知
                    sendBalanceReminder(reminder, latestBill);
                }
                
            } catch (Exception e) {
                // 记录错误日志
                System.err.println("检查余额提醒失败: " + e.getMessage());
            }
        }
    }

    /**
     * 定时检查缴费截止日期提醒
     */
    @Scheduled(cron = "0 0 10 * * ?") // 每天上午10点执行
    public void checkDueDateReminders() {
        List<ElectricityReminder> enabledReminders = reminderMapper.selectEnabledDueDateReminders();
        
        for (ElectricityReminder reminder : enabledReminders) {
            try {
                // 获取宿舍未缴费账单
                List<ElectricityBill> bills = billMapper.selectByDormitoryId(reminder.getDormitoryId());
                List<ElectricityBill> unpaidBills = bills.stream()
                        .filter(bill -> "未缴费".equals(bill.getStatus()))
                        .collect(Collectors.toList());
                
                for (ElectricityBill bill : unpaidBills) {
                    // 检查是否在提醒天数内到期
                    LocalDateTime reminderDate = bill.getDueDate().minusDays(reminder.getDueDateReminderDays());
                    if (LocalDateTime.now().isAfter(reminderDate) && 
                        LocalDateTime.now().isBefore(bill.getDueDate())) {
                        
                        // 检查是否已经提醒过
                        if (reminder.getLastReminderTime() == null || 
                            reminder.getLastReminderTime().isBefore(LocalDateTime.now().minusDays(1))) {
                            
                            // 发送提醒通知
                            sendDueDateReminder(reminder, bill);
                            
                            // 更新最后提醒时间
                            reminder.setLastReminderTime(LocalDateTime.now());
                            reminderMapper.updateById(reminder);
                        }
                    }
                }
                
            } catch (Exception e) {
                // 记录错误日志
                System.err.println("检查截止日期提醒失败: " + e.getMessage());
            }
        }
    }

    /**
     * 获取电费统计信息
     */
    public Map<String, Object> getElectricityStatistics() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取所有账单
            List<ElectricityBill> allBills = billMapper.selectList(null);
            
            // 统计未缴费账单数量
            long unpaidCount = allBills.stream()
                    .filter(bill -> "未缴费".equals(bill.getStatus()))
                    .count();
            
            // 统计逾期账单数量
            long overdueCount = allBills.stream()
                    .filter(bill -> "未缴费".equals(bill.getStatus()) && 
                                   bill.getDueDate().isBefore(LocalDateTime.now()))
                    .count();
            
            // 统计本月总电费
            String currentMonth = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
            BigDecimal monthlyTotal = allBills.stream()
                    .filter(bill -> currentMonth.equals(bill.getBillMonth()))
                    .map(ElectricityBill::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // 统计本月已缴费金额
            BigDecimal monthlyPaid = allBills.stream()
                    .filter(bill -> currentMonth.equals(bill.getBillMonth()) && 
                                   "已缴费".equals(bill.getStatus()))
                    .map(ElectricityBill::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            result.put("totalBills", allBills.size());
            result.put("unpaidBills", unpaidCount);
            result.put("overdueBills", overdueCount);
            result.put("monthlyTotal", monthlyTotal);
            result.put("monthlyPaid", monthlyPaid);
            result.put("paymentRate", (allBills.isEmpty() || monthlyTotal.compareTo(BigDecimal.ZERO) == 0) ? 0 : 
                    monthlyPaid.divide(monthlyTotal, 4, java.math.RoundingMode.HALF_UP).multiply(new BigDecimal("100")));
            
        } catch (Exception e) {
            result.put("error", "获取统计信息失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 发送余额提醒
     */
    private void sendBalanceReminder(ElectricityReminder reminder, ElectricityBill bill) {
        String title = "电费余额不足提醒";
        String content = String.format("您的宿舍 %s 电费余额为 %.2f 元，低于设置的提醒阈值 %.2f 元，请及时缴费。",
                bill.getDormitoryId(), bill.getCurrentBalance(), reminder.getBalanceThreshold());
        
        // 创建通知
        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType("电费提醒");
        notification.setReceiverType("student");
        notification.setReceiverId(reminder.getStudentId());
        notification.setIsRead(false);
        notification.setCreateTime(LocalDateTime.now());
        
        notificationService.createNotification(notification);
    }

    /**
     * 发送截止日期提醒
     */
    private void sendDueDateReminder(ElectricityReminder reminder, ElectricityBill bill) {
        String title = "电费缴费截止提醒";
        String content = String.format("您的宿舍 %s %s 月份电费账单将于 %s 到期，请及时缴费。",
                bill.getDormitoryId(), bill.getBillMonth(), 
                bill.getDueDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        
        // 创建通知
        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType("电费提醒");
        notification.setReceiverType("student");
        notification.setReceiverId(reminder.getStudentId());
        notification.setIsRead(false);
        notification.setCreateTime(LocalDateTime.now());
        
        notificationService.createNotification(notification);
    }

    /**
     * 获取学生当前分配
     */
    private DormitoryAllocation getCurrentAllocation(String studentId) {
        // 查询学生当前的宿舍分配（状态为"在住"的分配）
        return allocationMapper.selectByStudentId(studentId);
    }

    /**
     * 生成交易号
     */
    private String generateTransactionId() {
        return "ELEC" + System.currentTimeMillis() + (int)(Math.random() * 1000);
    }
}
