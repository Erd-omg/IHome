# 数据库初始化与准备指南

本指南详细介绍 IHome 项目的数据库初始化、测试数据准备、常见问题与验证步骤。建议在本地开发与端到端测试（Playwright）前完整执行本指南。

## 1. 数据库脚本与位置

- 结构脚本: `backend/src/main/resources/database-init.sql`
- 结构+测试数据脚本: `backend/src/main/resources/database-init-with-data.sql`
- 测试环境结构脚本（供后端单元测试参考）: `backend/src/main/resources/schema-test.sql`

## 2. 初始化方式

### 方式 A：MySQL 命令行（推荐）

```bash
# 在项目根目录下执行
mysql -u root -p < backend/src/main/resources/database-init-with-data.sql

# 仅结构（生产初始化使用）
mysql -u root -p < backend/src/main/resources/database-init.sql
```

### 方式 B：进入 MySQL 客户端执行

```bash
mysql -u root -p
-- 登录后执行
source backend/src/main/resources/database-init-with-data.sql;
```

### 方式 C：图形化工具（Workbench 等）

1. 打开工具并连接到本地 MySQL
2. 打开 `database-init-with-data.sql`
3. 执行脚本

### 方式 D：Docker（可选）

如果使用 `docker compose`，首次启动会自动挂载并执行初始化脚本：

```bash
docker compose up -d
```

## 3. 默认数据库配置

- 数据库名: `ihome`
- 字符集/排序: `utf8mb4 / utf8mb4_unicode_ci`
- 引擎: `InnoDB`

连接示例（`backend/src/main/resources/application.yml`）：

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/ihome?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=utf8&autoReconnect=true
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
```

> 重要：提交到仓库的 `application.yml` 使用占位符 `your_password`，请在本地替换为真实密码；含敏感信息的 `application-dev.yml` 已被 `.gitignore` 忽略。

## 4. 测试数据概览

- 管理员：`admin001`、`admin002`（密码：`password`）
- 学生：`2024001` ~ `2024010`（密码：`password`）
- 典型业务数据：宿舍/床位、分配记录、缴费记录、维修工单、系统通知等

密码使用 BCrypt 哈希：

```
$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi
```

## 5. 验证初始化

进入 MySQL 验证：

```sql
USE ihome;
SHOW TABLES;

-- 验证账号
SELECT id, name FROM students WHERE id IN ('2024001', '2024002');
SELECT id, name FROM admins WHERE id = 'admin001';
```

后端登录接口验证（示例）：

```bash
curl -X POST http://localhost:8080/api/students/login \
  -H "Content-Type: application/json" \
  -d '{"id":"2024001","password":"password"}'
```

## 6. 常见问题（FAQ）

### Q1: 登录失败：`登录失败: token为null` / `学号或密码错误`

- 数据库未初始化或测试账号密码哈希不一致
- 使用 `database-init-with-data.sql` 重新初始化
- 确认密码哈希为上文所示的 BCrypt 值

### Q2: 表格或页面加载超时（前端 E2E 测试）

- 后端或前端未启动；请确保：
  - 前端: `http://localhost:5173`
  - 后端: `http://localhost:8080/api`
- API 响应慢：稍增大测试中的超时时间

### Q3: WebKit 在 Windows 上视频录制失败

- 已在 `playwright.config.ts` 为 WebKit 禁用视频录制，不影响功能性测试

## 7. 重置数据库

```bash
# 备份（可选）
mysqldump -u root -p ihome > backup.sql

# 删除并重建（MySQL 客户端中执行）
DROP DATABASE IF EXISTS ihome;
source backend/src/main/resources/database-init.sql;
```

## 8. 表结构重点（摘录）

- 学生/管理员：`students` / `admins`
- 宿舍：`buildings` / `dormitories` / `beds`
- 业务：`payment_records` / `repair_orders` / `repair_feedback` / `notifications`
- 算法：`questionnaire_answers` / `roommate_tags` / `allocation_feedback` / `algorithm_weights`
- 电费：`electricity_bills` / `electricity_reminders` / `electricity_payments`

## 9. 与测试的关系

- 前端 E2E 测试使用真实后端 API，依赖 `with-data` 脚本生成的测试数据
- 运行顺序：初始化数据库 -> 启动后端 -> 启动前端 -> 运行 Playwright


