-- H2 兼容的测试数据库表结构（从 database-init.sql 转换）
-- 移除 MySQL 特定语法，转换为 H2 兼容语法

-- 1. 建筑表
CREATE TABLE IF NOT EXISTS buildings (
    id VARCHAR(50) PRIMARY KEY,
    building_number VARCHAR(20) NOT NULL,
    building_name VARCHAR(100) NOT NULL,
    gender_type VARCHAR(10) NOT NULL,
    floor_count INT NOT NULL DEFAULT 6,
    room_count INT NOT NULL DEFAULT 120,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. 宿舍表
CREATE TABLE IF NOT EXISTS dormitories (
    id VARCHAR(50) PRIMARY KEY,
    building_id VARCHAR(50) NOT NULL,
    floor_number INT NOT NULL,
    room_number VARCHAR(20) NOT NULL,
    room_type VARCHAR(50) NOT NULL DEFAULT '4人间',
    bed_count INT NOT NULL DEFAULT 4,
    current_occupancy INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT '可用',
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. 床位表
CREATE TABLE IF NOT EXISTS beds (
    id VARCHAR(50) PRIMARY KEY,
    dormitory_id VARCHAR(50) NOT NULL,
    bed_number VARCHAR(20) NOT NULL,
    bed_type VARCHAR(10) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT '可用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. 学生表
CREATE TABLE IF NOT EXISTS students (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20),
    email VARCHAR(100),
    password VARCHAR(255) NOT NULL,
    gender VARCHAR(10) NOT NULL,
    college VARCHAR(100),
    major VARCHAR(100),
    status VARCHAR(50) DEFAULT '在校',
    grade VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 5. 管理员表
CREATE TABLE IF NOT EXISTS admins (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20),
    email VARCHAR(100),
    password VARCHAR(255) NOT NULL,
    admin_type VARCHAR(50) NOT NULL DEFAULT '宿管员',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 6. 问卷答案表
CREATE TABLE IF NOT EXISTS questionnaire_answers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(50) NOT NULL,
    sleep_time_preference VARCHAR(50),
    cleanliness_level VARCHAR(50),
    noise_tolerance VARCHAR(50),
    eating_in_room VARCHAR(50),
    collective_spending_habit VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 7. 室友标签表
CREATE TABLE IF NOT EXISTS roommate_tags (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(50) NOT NULL,
    tag_name VARCHAR(50) NOT NULL,
    tag_type VARCHAR(50) NOT NULL DEFAULT 'QUESTIONNAIRE_AUTO',
    source VARCHAR(50) NOT NULL DEFAULT 'QUESTIONNAIRE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 8. 缴费记录表
CREATE TABLE IF NOT EXISTS payment_records (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(50) NOT NULL,
    payment_type VARCHAR(50) NOT NULL DEFAULT '其他',
    amount DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    payment_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT '已支付',
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 9. 维修工单表
CREATE TABLE IF NOT EXISTS repair_orders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(50) NOT NULL,
    dormitory_id VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    repair_type VARCHAR(50) NOT NULL,
    urgency_level VARCHAR(20) NOT NULL DEFAULT '一般',
    status VARCHAR(20) NOT NULL DEFAULT '待处理',
    repair_images TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 10. 维修反馈表
CREATE TABLE IF NOT EXISTS repair_feedback (
    id INT AUTO_INCREMENT PRIMARY KEY,
    repair_order_id INT NOT NULL,
    student_id VARCHAR(50) NOT NULL,
    rating INT NOT NULL,
    content TEXT,
    reply TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 11. 宿舍分配表
CREATE TABLE IF NOT EXISTS dormitory_allocations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(50) NOT NULL,
    dormitory_id VARCHAR(50),
    bed_id VARCHAR(50) NOT NULL,
    check_in_date DATE,
    check_out_date DATE,
    status VARCHAR(50) NOT NULL DEFAULT '在住',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 12. 宿舍调换表
CREATE TABLE IF NOT EXISTS dormitory_switches (
    id INT AUTO_INCREMENT PRIMARY KEY,
    applicant_id VARCHAR(50) NOT NULL,
    target_student_id VARCHAR(50),
    current_bed_id VARCHAR(50) NOT NULL,
    target_bed_id VARCHAR(50),
    reason TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT '待审核',
    apply_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    review_time TIMESTAMP,
    reviewer_id VARCHAR(50),
    review_comment TEXT,
    complete_time TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 13. 消息通知表
CREATE TABLE IF NOT EXISTS notifications (
    id INT AUTO_INCREMENT PRIMARY KEY,
    receiver_id VARCHAR(50) NOT NULL,
    receiver_type VARCHAR(20) NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,
    priority VARCHAR(20) DEFAULT 'normal',
    is_read BOOLEAN DEFAULT FALSE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    read_time TIMESTAMP,
    expire_time TIMESTAMP,
    related_id VARCHAR(50),
    related_type VARCHAR(50),
    sender_id VARCHAR(50),
    sender_type VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 14. 操作日志表
CREATE TABLE IF NOT EXISTS operation_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(50),
    user_type VARCHAR(20),
    user_name VARCHAR(100),
    module VARCHAR(100) NOT NULL,
    operation_type VARCHAR(20) NOT NULL,
    description VARCHAR(500),
    method VARCHAR(10),
    request_url VARCHAR(500),
    request_params TEXT,
    response_result TEXT,
    ip_address VARCHAR(50),
    user_agent VARCHAR(500),
    status VARCHAR(20) NOT NULL,
    error_message TEXT,
    execution_time BIGINT,
    operation_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 15. 分配反馈表
CREATE TABLE IF NOT EXISTS allocation_feedback (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(50) NOT NULL,
    allocation_id BIGINT,
    roommate_satisfaction INT NOT NULL,
    environment_satisfaction INT NOT NULL,
    overall_satisfaction INT NOT NULL,
    feedback_content TEXT,
    suggestions TEXT,
    willing_to_switch BOOLEAN DEFAULT FALSE,
    feedback_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 16. 算法权重配置表
CREATE TABLE IF NOT EXISTS algorithm_weights (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    weight_type VARCHAR(50) NOT NULL,
    weight_value DECIMAL(3,2) NOT NULL,
    description VARCHAR(200),
    enabled BOOLEAN DEFAULT TRUE,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

