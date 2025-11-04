-- =====================================================
-- IHome 宿舍管理系统 - 完整数据库初始化脚本（含测试数据）
-- =====================================================
-- 数据库名称: ihome
-- 字符集: utf8mb4
-- 排序规则: utf8mb4_unicode_ci
-- =====================================================
-- 此脚本创建完整的数据库结构并插入测试数据
-- 适用于开发和测试环境
-- =====================================================

-- 删除现有数据库（如果存在）
DROP DATABASE IF EXISTS ihome;

-- 创建数据库
CREATE DATABASE ihome CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE ihome;

-- =====================================================
-- 创建表结构（引用主初始化脚本中的结构）
-- =====================================================

-- 1. 建筑表
CREATE TABLE IF NOT EXISTS buildings (
    id VARCHAR(50) PRIMARY KEY COMMENT '建筑ID',
    building_number VARCHAR(20) NOT NULL COMMENT '建筑编号',
    building_name VARCHAR(100) NOT NULL COMMENT '建筑名称',
    gender_type ENUM('M', 'F', 'MIXED') NOT NULL COMMENT '性别类型',
    floor_count INT NOT NULL DEFAULT 6 COMMENT '楼层数',
    room_count INT NOT NULL DEFAULT 120 COMMENT '房间数',
    description TEXT COMMENT '描述',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_gender_type (gender_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='建筑表';

-- 2. 宿舍表
CREATE TABLE IF NOT EXISTS dormitories (
    id VARCHAR(50) PRIMARY KEY COMMENT '宿舍ID',
    building_id VARCHAR(50) NOT NULL COMMENT '建筑ID',
    floor_number INT NOT NULL COMMENT '楼层号',
    room_number VARCHAR(20) NOT NULL COMMENT '房间号',
    room_type VARCHAR(50) NOT NULL DEFAULT '4人间' COMMENT '房间类型',
    bed_count INT NOT NULL DEFAULT 4 COMMENT '床位数',
    current_occupancy INT NOT NULL DEFAULT 0 COMMENT '当前入住人数',
    status ENUM('可用', '已满', '维修中', '停用') NOT NULL DEFAULT '可用' COMMENT '状态',
    description TEXT COMMENT '描述',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (building_id) REFERENCES buildings(id) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_building_floor (building_id, floor_number),
    INDEX idx_status (status),
    INDEX idx_room_number (room_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='宿舍表';

-- 3. 床位表
CREATE TABLE IF NOT EXISTS beds (
    id VARCHAR(50) PRIMARY KEY COMMENT '床位ID',
    dormitory_id VARCHAR(50) NOT NULL COMMENT '宿舍ID',
    bed_number VARCHAR(20) NOT NULL COMMENT '床位号',
    bed_type ENUM('上铺', '下铺') NOT NULL COMMENT '床位类型',
    status ENUM('可用', '已占用', '维修中', '停用') NOT NULL DEFAULT '可用' COMMENT '状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (dormitory_id) REFERENCES dormitories(id) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_dormitory (dormitory_id),
    INDEX idx_status (status),
    INDEX idx_bed_number (bed_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='床位表';

-- 4. 学生表
CREATE TABLE IF NOT EXISTS students (
    id VARCHAR(50) PRIMARY KEY COMMENT '学号',
    name VARCHAR(100) NOT NULL COMMENT '姓名',
    phone_number VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    password VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    gender ENUM('男', '女') NOT NULL COMMENT '性别',
    college VARCHAR(100) COMMENT '学院',
    major VARCHAR(100) COMMENT '专业',
    status VARCHAR(50) DEFAULT '在校' COMMENT '状态',
    grade VARCHAR(50) COMMENT '年级',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_college_major (college, major),
    INDEX idx_gender (gender),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生表';

-- 5. 管理员表
CREATE TABLE IF NOT EXISTS admins (
    id VARCHAR(50) PRIMARY KEY COMMENT '管理员ID',
    name VARCHAR(100) NOT NULL COMMENT '姓名',
    phone_number VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    password VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    admin_type VARCHAR(50) NOT NULL DEFAULT '宿管员' COMMENT '管理员类型',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_admin_type (admin_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员表';

-- 6. 问卷答案表
CREATE TABLE IF NOT EXISTS questionnaire_answers (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    student_id VARCHAR(50) NOT NULL COMMENT '学生ID',
    sleep_time_preference VARCHAR(50) COMMENT '睡眠时间偏好',
    cleanliness_level VARCHAR(50) COMMENT '清洁程度',
    noise_tolerance VARCHAR(50) COMMENT '噪音容忍度',
    eating_in_room VARCHAR(50) COMMENT '房间内用餐',
    collective_spending_habit VARCHAR(50) COMMENT '集体消费习惯',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE ON UPDATE CASCADE,
    UNIQUE KEY uk_student_id (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问卷答案表';

-- 7. 室友标签表
CREATE TABLE IF NOT EXISTS roommate_tags (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    student_id VARCHAR(50) NOT NULL COMMENT '学生ID',
    tag_name VARCHAR(50) NOT NULL COMMENT '标签名称',
    tag_type VARCHAR(50) NOT NULL DEFAULT 'QUESTIONNAIRE_AUTO' COMMENT '标签类型(QUESTIONNAIRE_AUTO/MANUAL_POSITIVE/MANUAL_NEUTRAL/MANUAL_NEGATIVE)',
    source VARCHAR(50) NOT NULL DEFAULT 'QUESTIONNAIRE' COMMENT '来源(QUESTIONNAIRE/MANUAL)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_student_id (student_id),
    INDEX idx_tag_name (tag_name),
    INDEX idx_tag_type (tag_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='室友标签表';

-- 8. 缴费记录表
CREATE TABLE IF NOT EXISTS payment_records (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    student_id VARCHAR(50) NOT NULL COMMENT '学生ID',
    payment_type VARCHAR(50) NOT NULL DEFAULT '其他' COMMENT '缴费类型（住宿费/电费/其他）',
    amount DECIMAL(10,2) NOT NULL COMMENT '金额',
    payment_method VARCHAR(50) NOT NULL COMMENT '支付方式',
    payment_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '支付时间',
    status ENUM('待支付', '已支付', '已退款') NOT NULL DEFAULT '已支付' COMMENT '状态',
    description VARCHAR(255) COMMENT '描述',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_student_id (student_id),
    INDEX idx_payment_time (payment_time),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='缴费记录表';

-- 9. 维修工单表
CREATE TABLE IF NOT EXISTS repair_orders (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    student_id VARCHAR(50) NOT NULL COMMENT '学生ID',
    dormitory_id VARCHAR(50) NOT NULL COMMENT '宿舍ID',
    description TEXT NOT NULL COMMENT '维修描述',
    repair_type VARCHAR(50) NOT NULL COMMENT '维修类型',
    urgency_level ENUM('一般', '紧急', '非常紧急') NOT NULL DEFAULT '一般' COMMENT '紧急程度',
    status ENUM('待处理', '处理中', '已完成', '已取消') NOT NULL DEFAULT '待处理' COMMENT '状态',
    repair_images TEXT COMMENT '维修图片路径，多个用逗号分隔',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (dormitory_id) REFERENCES dormitories(id) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_student_id (student_id),
    INDEX idx_dormitory_id (dormitory_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='维修工单表';

-- 10. 维修反馈表
CREATE TABLE IF NOT EXISTS repair_feedback (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '反馈ID',
    repair_order_id INT NOT NULL COMMENT '维修工单ID',
    student_id VARCHAR(50) NOT NULL COMMENT '学生ID',
    rating INT NOT NULL COMMENT '评分(1-5分)',
    content TEXT COMMENT '反馈内容',
    reply TEXT COMMENT '管理员回复',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (repair_order_id) REFERENCES repair_orders(id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_repair_order_id (repair_order_id),
    INDEX idx_student_id (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='维修反馈表';

-- 11. 宿舍分配表
CREATE TABLE IF NOT EXISTS dormitory_allocations (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    student_id VARCHAR(50) NOT NULL COMMENT '学生ID',
    dormitory_id VARCHAR(50) COMMENT '宿舍ID',
    bed_id VARCHAR(50) NOT NULL COMMENT '床位ID',
    check_in_date DATE COMMENT '入住日期',
    check_out_date DATE COMMENT '退宿日期',
    status VARCHAR(50) NOT NULL DEFAULT '在住' COMMENT '状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (bed_id) REFERENCES beds(id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (dormitory_id) REFERENCES dormitories(id) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_student_id (student_id),
    INDEX idx_bed_id (bed_id),
    INDEX idx_dormitory_id (dormitory_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='宿舍分配表';

-- 12. 宿舍调换表
CREATE TABLE IF NOT EXISTS dormitory_switches (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    applicant_id VARCHAR(50) NOT NULL COMMENT '申请人ID',
    target_student_id VARCHAR(50) COMMENT '目标学生ID',
    current_bed_id VARCHAR(50) NOT NULL COMMENT '当前床位ID',
    target_bed_id VARCHAR(50) COMMENT '目标床位ID',
    reason TEXT NOT NULL COMMENT '调换原因',
    status ENUM('待审核', '已通过', '已拒绝', '已完成') NOT NULL DEFAULT '待审核' COMMENT '状态',
    apply_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    review_time TIMESTAMP NULL COMMENT '审核时间',
    reviewer_id VARCHAR(50) COMMENT '审核人ID',
    review_comment TEXT COMMENT '审核意见',
    complete_time TIMESTAMP NULL COMMENT '完成时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (applicant_id) REFERENCES students(id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (target_student_id) REFERENCES students(id) ON DELETE SET NULL ON UPDATE CASCADE,
    FOREIGN KEY (current_bed_id) REFERENCES beds(id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (target_bed_id) REFERENCES beds(id) ON DELETE SET NULL ON UPDATE CASCADE,
    FOREIGN KEY (reviewer_id) REFERENCES admins(id) ON DELETE SET NULL ON UPDATE CASCADE,
    INDEX idx_applicant_id (applicant_id),
    INDEX idx_status (status),
    INDEX idx_apply_time (apply_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='宿舍调换表';

-- 13. 消息通知表
CREATE TABLE IF NOT EXISTS notifications (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '通知ID',
    receiver_id VARCHAR(50) NOT NULL COMMENT '接收者ID',
    receiver_type VARCHAR(20) NOT NULL COMMENT '接收者类型(student/admin)',
    title VARCHAR(200) NOT NULL COMMENT '通知标题',
    content TEXT NOT NULL COMMENT '通知内容',
    type VARCHAR(50) NOT NULL COMMENT '通知类型(system/repair/payment/switch/allocation)',
    priority VARCHAR(20) DEFAULT 'normal' COMMENT '通知优先级(low/normal/high/urgent)',
    is_read BOOLEAN DEFAULT FALSE COMMENT '是否已读',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    read_time TIMESTAMP NULL COMMENT '阅读时间',
    expire_time TIMESTAMP NULL COMMENT '过期时间',
    related_id VARCHAR(50) NULL COMMENT '相关业务ID',
    related_type VARCHAR(50) NULL COMMENT '相关业务类型',
    sender_id VARCHAR(50) NULL COMMENT '发送者ID',
    sender_type VARCHAR(20) NULL COMMENT '发送者类型',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_receiver (receiver_id, receiver_type),
    INDEX idx_type (type),
    INDEX idx_priority (priority),
    INDEX idx_is_read (is_read),
    INDEX idx_create_time (create_time),
    INDEX idx_expire_time (expire_time),
    INDEX idx_related (related_id, related_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息通知表';

-- 14. 操作日志表
CREATE TABLE IF NOT EXISTS operation_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    user_id VARCHAR(50) COMMENT '操作用户ID',
    user_type ENUM('STUDENT', 'ADMIN') COMMENT '操作用户类型',
    user_name VARCHAR(100) COMMENT '操作用户名称',
    module VARCHAR(100) NOT NULL COMMENT '操作模块',
    operation_type ENUM('CREATE', 'UPDATE', 'DELETE', 'QUERY') NOT NULL COMMENT '操作类型',
    description VARCHAR(500) COMMENT '操作描述',
    method VARCHAR(10) COMMENT '请求方法',
    request_url VARCHAR(500) COMMENT '请求URL',
    request_params TEXT COMMENT '请求参数',
    response_result TEXT COMMENT '响应结果',
    ip_address VARCHAR(50) COMMENT '操作IP',
    user_agent VARCHAR(500) COMMENT '用户代理',
    status ENUM('SUCCESS', 'FAILURE') NOT NULL COMMENT '操作状态',
    error_message TEXT COMMENT '错误信息',
    execution_time BIGINT COMMENT '执行时间(毫秒)',
    operation_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user (user_id, user_type),
    INDEX idx_module (module),
    INDEX idx_operation_type (operation_type),
    INDEX idx_status (status),
    INDEX idx_operation_time (operation_time),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- 15. 分配反馈表
CREATE TABLE IF NOT EXISTS allocation_feedback (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '反馈ID',
    student_id VARCHAR(50) NOT NULL COMMENT '学生ID',
    allocation_id BIGINT COMMENT '分配ID',
    roommate_satisfaction INT NOT NULL COMMENT '室友满意度(1-5分)',
    environment_satisfaction INT NOT NULL COMMENT '宿舍环境满意度(1-5分)',
    overall_satisfaction INT NOT NULL COMMENT '整体满意度(1-5分)',
    feedback_content TEXT COMMENT '反馈内容',
    suggestions TEXT COMMENT '建议改进',
    willing_to_switch BOOLEAN DEFAULT FALSE COMMENT '是否愿意调换',
    feedback_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '反馈时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_student (student_id),
    INDEX idx_allocation (allocation_id),
    INDEX idx_feedback_time (feedback_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分配反馈表';

-- 16. 算法权重配置表
CREATE TABLE IF NOT EXISTS algorithm_weights (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '配置ID',
    weight_type VARCHAR(50) NOT NULL COMMENT '权重类型',
    weight_value DECIMAL(3,2) NOT NULL COMMENT '权重值(0.0-1.0)',
    description VARCHAR(200) COMMENT '权重描述',
    enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_weight_type (weight_type),
    INDEX idx_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='算法权重配置表';

-- 17. 电费账单表
CREATE TABLE IF NOT EXISTS electricity_bills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '账单ID',
    dormitory_id VARCHAR(50) NOT NULL COMMENT '宿舍ID',
    bill_month VARCHAR(7) NOT NULL COMMENT '账单月份 (YYYY-MM)',
    electricity_usage DECIMAL(10,2) NOT NULL COMMENT '用电量 (度)',
    unit_price DECIMAL(8,4) NOT NULL COMMENT '电费单价 (元/度)',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '总电费金额',
    current_balance DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '当前余额',
    status ENUM('未缴费', '已缴费', '逾期') NOT NULL DEFAULT '未缴费' COMMENT '账单状态',
    due_date TIMESTAMP NOT NULL COMMENT '缴费截止日期',
    payment_time TIMESTAMP NULL COMMENT '缴费时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (dormitory_id) REFERENCES dormitories(id) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_dormitory_month (dormitory_id, bill_month),
    INDEX idx_status (status),
    INDEX idx_due_date (due_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='电费账单表';

-- 18. 电费提醒设置表
CREATE TABLE IF NOT EXISTS electricity_reminders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '提醒ID',
    student_id VARCHAR(50) NOT NULL COMMENT '学生ID',
    dormitory_id VARCHAR(50) NOT NULL COMMENT '宿舍ID',
    balance_threshold DECIMAL(10,2) NOT NULL DEFAULT 50.00 COMMENT '余额提醒阈值 (元)',
    balance_reminder_enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否启用余额提醒',
    due_date_reminder_days INT NOT NULL DEFAULT 3 COMMENT '缴费截止提醒天数',
    due_date_reminder_enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否启用截止日期提醒',
    reminder_method ENUM('站内信', '短信', '邮件', '全部') NOT NULL DEFAULT '站内信' COMMENT '提醒方式',
    last_reminder_time TIMESTAMP NULL COMMENT '最后提醒时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (dormitory_id) REFERENCES dormitories(id) ON DELETE CASCADE ON UPDATE CASCADE,
    UNIQUE KEY uk_student_reminder (student_id),
    INDEX idx_dormitory (dormitory_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='电费提醒设置表';

-- 19. 电费缴费记录表
CREATE TABLE IF NOT EXISTS electricity_payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '缴费记录ID',
    bill_id BIGINT NOT NULL COMMENT '电费账单ID',
    student_id VARCHAR(50) NOT NULL COMMENT '学生ID',
    dormitory_id VARCHAR(50) NOT NULL COMMENT '宿舍ID',
    payment_amount DECIMAL(10,2) NOT NULL COMMENT '缴费金额',
    payment_method ENUM('支付宝', '微信', '银行卡', '现金') NOT NULL COMMENT '缴费方式',
    payment_status ENUM('待支付', '已支付', '已退款') NOT NULL DEFAULT '待支付' COMMENT '缴费状态',
    transaction_id VARCHAR(100) UNIQUE COMMENT '第三方交易号',
    payment_time TIMESTAMP NULL COMMENT '缴费时间',
    remarks TEXT COMMENT '备注',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (bill_id) REFERENCES electricity_bills(id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (dormitory_id) REFERENCES dormitories(id) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_student (student_id),
    INDEX idx_dormitory (dormitory_id),
    INDEX idx_payment_time (payment_time),
    INDEX idx_payment_status (payment_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='电费缴费记录表';

-- =====================================================
-- 插入测试数据
-- =====================================================

-- 插入建筑数据
INSERT INTO buildings (id, building_number, building_name, gender_type, floor_count, room_count, description) VALUES
('B001', '1', '一号男生公寓', 'M', 6, 120, '男生宿舍'),
('B002', '2', '二号女生公寓', 'F', 6, 120, '女生宿舍');

-- 插入宿舍数据
INSERT INTO dormitories (id, building_id, floor_number, room_number, room_type, bed_count, current_occupancy, status) VALUES
('D01-101', 'B001', 1, '101', '4人间', 4, 0, '可用'),
('D01-102', 'B001', 1, '102', '4人间', 4, 0, '可用'),
('D01-201', 'B001', 2, '201', '4人间', 4, 0, '可用'),
('D01-202', 'B001', 2, '202', '4人间', 4, 0, '可用'),
('D02-101', 'B002', 1, '101', '4人间', 4, 0, '可用'),
('D02-102', 'B002', 1, '102', '4人间', 4, 0, '可用');

-- 插入床位数据
INSERT INTO beds (id, dormitory_id, bed_number, bed_type, status) VALUES
-- D01-101 宿舍
('B01-101-1', 'D01-101', '1', '上铺', '可用'),
('B01-101-2', 'D01-101', '2', '下铺', '可用'),
('B01-101-3', 'D01-101', '3', '上铺', '可用'),
('B01-101-4', 'D01-101', '4', '下铺', '可用'),
-- D01-102 宿舍
('B01-102-1', 'D01-102', '1', '上铺', '可用'),
('B01-102-2', 'D01-102', '2', '下铺', '可用'),
('B01-102-3', 'D01-102', '3', '上铺', '可用'),
('B01-102-4', 'D01-102', '4', '下铺', '可用'),
-- D02-101 宿舍
('B02-101-1', 'D02-101', '1', '上铺', '可用'),
('B02-101-2', 'D02-101', '2', '下铺', '可用'),
('B02-101-3', 'D02-101', '3', '上铺', '可用'),
('B02-101-4', 'D02-101', '4', '下铺', '可用'),
-- D02-102 宿舍
('B02-102-1', 'D02-102', '1', '上铺', '可用'),
('B02-102-2', 'D02-102', '2', '下铺', '可用'),
('B02-102-3', 'D02-102', '3', '上铺', '可用'),
('B02-102-4', 'D02-102', '4', '下铺', '可用');

-- 插入学生数据（密码：password，BCrypt加密）
-- BCrypt散列: $2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi
INSERT INTO students (id, name, phone_number, email, password, gender, college, major, status, grade) VALUES
('2024001', '张三', '13800138001', 'zhangsan@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '男', '计算机学院', '软件工程', '在校', '2024级'),
('2024002', '李四', '13800138002', 'lisi@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '男', '计算机学院', '网络工程', '在校', '2024级'),
('2024003', '王五', '13800138003', 'wangwu@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '男', '计算机学院', '信息安全', '在校', '2024级'),
('2024004', '赵六', '13800138004', 'zhaoliu@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '女', '计算机学院', '软件工程', '在校', '2024级'),
('2024005', '钱七', '13800138005', 'qianqi@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '女', '计算机学院', '网络工程', '在校', '2024级'),
('2024006', '孙八', '13800138006', 'sunba@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '男', '计算机学院', '数据科学', '在校', '2024级'),
('2024007', '周九', '13800138007', 'zhoujiu@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '女', '计算机学院', '人工智能', '在校', '2024级'),
('2024008', '吴十', '13800138008', 'wushi@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '男', '计算机学院', '软件工程', '在校', '2024级'),
('2024009', '郑十一', '13800138009', 'zheng11@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '女', '计算机学院', '网络工程', '在校', '2024级'),
('2024010', '冯十二', '13800138010', 'feng12@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '男', '计算机学院', '信息安全', '在校', '2024级');

-- 插入管理员数据（密码：password，BCrypt加密）
INSERT INTO admins (id, name, phone_number, email, password, admin_type) VALUES
('admin001', '系统管理员', '13800139001', 'admin@ihome.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '系统管理员'),
('admin002', '宿管阿姨', '13800139002', 'suguan@ihome.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '宿管员'),
('admin003', '宿管叔叔', '13800139003', 'suguan2@ihome.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '宿管员');

-- 插入问卷答案数据
INSERT INTO questionnaire_answers (student_id, sleep_time_preference, cleanliness_level, noise_tolerance, eating_in_room, collective_spending_habit) VALUES
('2024001', '早睡早起', '爱整洁', '安静', '从不', '愿意'),
('2024002', '晚睡晚起', '一般', '能接受一点噪音', '偶尔', '一般'),
('2024003', '不固定', '爱整洁', '安静', '经常', '愿意'),
('2024004', '早睡早起', '爱整洁', '安静', '从不', '愿意'),
('2024005', '晚睡晚起', '随意', '随意', '偶尔', '不愿意');

-- 插入室友标签数据
INSERT INTO roommate_tags (student_id, tag_name, tag_type, source) VALUES
('2024001', '整洁', 'QUESTIONNAIRE_AUTO', 'QUESTIONNAIRE'),
('2024001', '早睡', 'QUESTIONNAIRE_AUTO', 'QUESTIONNAIRE'),
('2024001', '安静', 'QUESTIONNAIRE_AUTO', 'QUESTIONNAIRE'),
('2024001', '不在宿舍用餐', 'QUESTIONNAIRE_AUTO', 'QUESTIONNAIRE'),
('2024001', '集体消费', 'QUESTIONNAIRE_AUTO', 'QUESTIONNAIRE'),
('2024002', '晚睡', 'QUESTIONNAIRE_AUTO', 'QUESTIONNAIRE'),
('2024003', '整洁', 'QUESTIONNAIRE_AUTO', 'QUESTIONNAIRE'),
('2024003', '安静', 'QUESTIONNAIRE_AUTO', 'QUESTIONNAIRE'),
('2024003', '集体消费', 'QUESTIONNAIRE_AUTO', 'QUESTIONNAIRE'),
('2024004', '整洁', 'QUESTIONNAIRE_AUTO', 'QUESTIONNAIRE'),
('2024004', '早睡', 'QUESTIONNAIRE_AUTO', 'QUESTIONNAIRE'),
('2024004', '安静', 'QUESTIONNAIRE_AUTO', 'QUESTIONNAIRE'),
('2024004', '不在宿舍用餐', 'QUESTIONNAIRE_AUTO', 'QUESTIONNAIRE'),
('2024004', '集体消费', 'QUESTIONNAIRE_AUTO', 'QUESTIONNAIRE');

-- 插入分配数据（将部分学生分配床位，注意：需要先创建床位）
INSERT INTO dormitory_allocations (student_id, dormitory_id, bed_id, check_in_date, status) VALUES
('2024001', 'D01-101', 'B01-101-1', '2024-09-01', '在住'),
('2024002', 'D01-101', 'B01-101-2', '2024-09-01', '在住'),
('2024003', 'D01-102', 'B01-102-1', '2024-09-01', '在住'),
('2024004', 'D02-101', 'B02-101-1', '2024-09-01', '在住'),
('2024005', 'D02-102', 'B02-102-1', '2024-09-01', '在住');

-- 更新床位状态为已占用
UPDATE beds SET status = '已占用' WHERE id IN ('B01-101-1', 'B01-101-2', 'B01-102-1', 'B02-101-1', 'B02-102-1');

-- 更新宿舍入住人数
UPDATE dormitories SET current_occupancy = 2, status = '可用' WHERE id = 'D01-101';
UPDATE dormitories SET current_occupancy = 1, status = '可用' WHERE id = 'D01-102';
UPDATE dormitories SET current_occupancy = 1, status = '可用' WHERE id = 'D02-101';
UPDATE dormitories SET current_occupancy = 1, status = '可用' WHERE id = 'D02-102';

-- 插入缴费记录数据
INSERT INTO payment_records (student_id, payment_type, amount, payment_method, status, description) VALUES
('2024001', '住宿费', 1200.00, '在线支付', '已支付', '住宿费'),
('2024001', '电费', 100.00, '在线支付', '已支付', '电费'),
('2024002', '住宿费', 1200.00, '在线支付', '已支付', '住宿费'),
('2024002', '电费', 150.00, '在线支付', '已支付', '电费'),
('2024003', '住宿费', 1200.00, '在线支付', '已支付', '住宿费'),
('2024004', '住宿费', 1200.00, '在线支付', '已支付', '住宿费'),
('2024005', '住宿费', 1200.00, '在线支付', '已支付', '住宿费');

-- 插入算法权重配置
INSERT INTO algorithm_weights (weight_type, weight_value, description, enabled) VALUES
('QUESTIONNAIRE', 0.50, '问卷匹配权重', TRUE),
('TAG', 0.40, '标签匹配权重', TRUE),
('MAJOR', 0.30, '专业匹配权重', TRUE),
('GENDER', 0.20, '性别匹配权重', TRUE),
('BED_TYPE', 0.10, '床位类型权重', TRUE);

-- 插入通知数据（系统公告）
INSERT INTO notifications (receiver_id, receiver_type, title, content, type, priority, is_read, related_type) VALUES
('ALL', 'ALL', '【重要】开学宿舍入住通知', '亲爱的同学们，新学期即将开始，请及时查看宿舍分配情况，按时入住。如有疑问，请联系宿管老师。', 'system', 'high', FALSE, 'allocation'),
('ALL', 'ALL', '【公告】宿舍管理规定', '为保证良好的住宿环境，请同学们遵守宿舍管理规定：1. 保持宿舍整洁卫生；2. 禁止使用大功率电器；3. 按时作息，保持安静。', 'system', 'normal', FALSE, 'rule'),
('ALL', 'ALL', '【通知】电费缴费提醒', '本学期电费缴费工作已经开始，请同学们及时查看宿舍电费余额并按时缴费，避免停电影响正常学习生活。', 'system', 'high', FALSE, 'payment'),
('ALL', 'ALL', '【温馨提示】宿舍维修服务', '宿舍维修服务已开通，如有宿舍设施损坏，请及时通过系统提交维修申请，我们会尽快安排维修。', 'system', 'normal', FALSE, 'repair'),
('ALL', 'ALL', '【公告】消防安全注意事项', '为确保宿舍安全，请同学们注意消防安全：不得在宿舍内使用明火，不得私拉乱接电线，发现火情及时报警。', 'system', 'urgent', FALSE, 'safety'),
('ALL', 'ALL', '【通知】开展宿舍评比活动', '为营造良好的住宿环境，学校将开展"文明宿舍"评比活动，希望各宿舍积极参与，共同创建文明和谐的住宿环境。', 'system', 'normal', FALSE, 'activity'),
('ALL', 'ALL', '【公告】床位调换申请开放', '本学期床位调换申请通道已开放，如需调换宿舍或床位，请在系统中提交申请，经过审核后统一安排。', 'system', 'normal', FALSE, 'switch'),
('ALL', 'ALL', '【重要】期末宿舍安全检查', '期末考试即将来临，为确保安全，学校将在近期开展宿舍安全检查，请同学们提前整理好个人物品，配合检查。', 'system', 'high', FALSE, 'safety'),
('2024001', 'student', '欢迎入住', '欢迎您入住一号男生公寓101宿舍！祝您住宿愉快。', 'system', 'normal', FALSE, 'allocation'),
('2024002', 'student', '欢迎入住', '欢迎您入住一号男生公寓101宿舍！祝您住宿愉快。', 'system', 'normal', FALSE, 'allocation'),
('2024003', 'student', '欢迎入住', '欢迎您入住一号男生公寓102宿舍！祝您住宿愉快。', 'system', 'normal', FALSE, 'allocation'),
('2024004', 'student', '欢迎入住', '欢迎您入住二号女生公寓101宿舍！祝您住宿愉快。', 'system', 'normal', FALSE, 'allocation'),
('2024005', 'student', '欢迎入住', '欢迎您入住二号女生公寓102宿舍！祝您住宿愉快。', 'system', 'normal', FALSE, 'allocation'),
('2024001', 'student', '电费余额提醒', '您的宿舍电费余额已低于50元，请及时缴费。', 'payment', 'high', FALSE, 'electricity'),
('2024002', 'student', '维修服务提醒', '您的维修申请已受理，预计3日内完成维修，请注意接听维修人员电话。', 'repair', 'normal', FALSE, 'repair_order');

-- =====================================================
-- 完成信息
-- =====================================================
SELECT 
    '数据库初始化完成！' as message,
    '数据库名称: ihome' as database_name,
    '测试账号: 2024001-2024010 (密码: password)' as student_accounts,
    '管理员账号: admin001-003 (密码: password)' as admin_accounts;

