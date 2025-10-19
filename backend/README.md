# IHome 宿舍管理系统

> 摘要：本项目后端实现遵循标准的Spring Boot开发规范，包括实体-表字段一一对应、统一返回结构、参数校验、事务边界、接口契约等。系统采用现代化的技术架构，提供完整的宿舍管理功能。

## 项目概述

IHome 是一款用于大学宿舍管理的综合性系统，旨在为学生和管理员提供高效、便捷的宿舍管理服务。系统包含学生端和管理员端，涵盖宿舍分配、缴费、维修、个人信息管理等核心功能，并引入了智能分配算法，以提升用户体验和管理效率。

## 技术栈

- **后端框架**: Spring Boot 3.3.2
- **数据库**: MySQL 8.0
- **ORM框架**: MyBatis Plus 3.5.7
- **安全认证**: Spring Security + JWT
- **API文档**: Swagger/OpenAPI 3
- **构建工具**: Maven
- **Java版本**: 17

## 项目结构

```
IHome/
├── backend/                          # 后端项目
│   ├── src/main/java/com/ihome/
│   │   ├── common/                   # 公共类
│   │   │   └── ApiResponse.java      # 统一响应格式
│   │   ├── config/                   # 配置类
│   │   │   ├── MybatisPlusConfig.java
│   │   │   └── SecurityConfig.java
│   │   ├── controller/               # 控制器
│   │   │   ├── RootController.java
│   │   │   └── StudentController.java
│   │   ├── entity/                   # 实体类
│   │   │   ├── Student.java
│   │   │   ├── Admin.java
│   │   │   ├── Building.java
│   │   │   ├── Dormitory.java
│   │   │   ├── Bed.java
│   │   │   ├── DormitoryAllocation.java
│   │   │   ├── QuestionnaireAnswer.java
│   │   │   ├── RoommateTag.java
│   │   │   ├── PaymentRecord.java
│   │   │   └── RepairOrder.java
│   │   ├── mapper/                   # 数据访问层
│   │   │   ├── StudentMapper.java
│   │   │   ├── AdminMapper.java
│   │   │   └── ... (其他Mapper)
│   │   └── IhomeApplication.java     # 启动类
│   ├── src/main/resources/
│   │   └── application.yml           # 配置文件
│   └── pom.xml                       # Maven配置
└── README.md
```

## 数据库设计

系统包含以下10张核心表：

1. **students** - 学生表
2. **admins** - 管理员表
3. **buildings** - 宿舍楼表
4. **dormitories** - 宿舍房间表
5. **beds** - 床位表
6. **dormitory_allocations** - 住宿分配表
7. **questionnaire_answers** - 问卷答案表
8. **roommate_tags** - 室友匹配标签表
9. **payment_records** - 缴费记录表
10. **repair_orders** - 维修工单表

## 环境要求

- Java 17+
- MySQL 8.0+
- Maven 3.6+

## 快速开始

### 1. 数据库配置

确保MySQL服务已启动，创建数据库：

```sql
CREATE DATABASE ihome CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 修改配置

编辑 `backend/src/main/resources/application.yml`，确认数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/ihome?useUnicode=true&characterEncoding=utf8mb4&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: your_password_here
```

### 3. 启动项目

推荐 jar 方式（日志更完整）：

```bash
cd backend
mvn clean package -DskipTests
java -jar target/ihome-0.0.1-SNAPSHOT.jar
```

项目启动后访问：
- 健康检查: http://localhost:8080/api/health
- 根路径: http://localhost:8080/api/
- API文档: http://localhost:8080/api/swagger-ui.html

## API接口文档

### 基础信息

- **Base URL**: `http://localhost:8080/api`
- **Content-Type**: `application/json`
- **响应格式**: 统一使用 `ApiResponse` 格式

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

### 已实现API接口

#### 1. 学生管理 (StudentController)
- `POST /students/register` - 学生注册
- `POST /students/login` - 学生登录
- `GET /students/{id}` - 获取学生信息
- `PUT /students/{id}` - 更新学生信息

#### 2. 宿舍管理 (DormitoryController)
- `GET /dorms` - 获取宿舍列表（支持按楼栋、状态筛选）
- `GET /dorms/{dormitoryId}/beds` - 获取宿舍床位列表
- `POST /dorms/choose-bed` - 选择床位
- `POST /dorms/checkout` - 退宿

#### 3. 住宿分配 (AllocationController)
- `GET /allocations/student/{studentId}` - 获取学生当前住宿分配
- `GET /allocations/student/{studentId}/history` - 获取学生住宿历史

#### 4. 缴费管理 (PaymentController)
- `POST /payments` - 创建缴费记录
- `GET /payments/student/{studentId}` - 获取学生缴费记录
- `GET /payments` - 搜索缴费记录（支持多条件筛选）
- `PUT /payments/{paymentId}/status` - 更新缴费状态

#### 5. 维修管理 (RepairController)
- `POST /repairs` - 创建维修工单
- `GET /repairs/student/{studentId}` - 获取学生维修记录
- `GET /repairs` - 搜索维修工单（支持多条件筛选）
- `PUT /repairs/{repairId}/status` - 更新维修状态

#### 6. 问卷管理 (QuestionnaireController)
- `POST /questionnaire/submit` - 提交问卷和标签
- `GET /questionnaire/student/{studentId}` - 获取学生问卷数据

#### 7. 管理员功能 (AdminController)
- `GET /admin/dashboard` - 获取仪表盘统计数据
- `GET /admin/students` - 获取所有学生信息
- `GET /admin/dormitories` - 获取所有宿舍信息
- `GET /admin/allocations` - 获取所有住宿分配

#### 8. 系统功能 (RootController)
- `GET /` - 根路径
- `GET /health` - 健康检查

## Postman 测试指南（逐步操作）

### 1. 启动并验证后端

1) 启动（见上文）
2) 验证：
- GET `{{base_url}}/health` → 应返回 `OK`
- GET `{{base_url}}/` → 应返回运行提示

### 2. 创建 Postman 环境与集合

#### 2.1 创建环境
1. 打开 Postman → 右上角齿轮 → Manage Environments → Add
2. 环境名称：`IHome Local`
3. 新增变量：
   - `base_url` = `http://localhost:8080/api`
   - `student_id`（初始可留空，登录/注册后在 Tests 中保存）
   - `token`（预留-JWT上线后使用）

#### 2.2 创建/导入集合
1. 左侧 Collections → New Collection → 命名 `IHome API Tests`
2. 或使用 Import → Raw text 导入你整理的请求

### 3. 学生模块接口测试（详细步骤）

#### 3.1 学生注册
- Method: POST
- URL: `{{base_url}}/students/register`
- Headers: `Content-Type: application/json`
- Body (raw / JSON):
```json
{
  "id": "2024001",
  "name": "张三",
  "phoneNumber": "13800138001",
  "email": "zhangsan@example.com",
  "password": "123456",
  "gender": "男",
  "college": "计算机学院",
  "major": "软件工程"
}
```
- 预期：`code: 0`

#### 3.2 学生登录
- Method: POST
- URL: `{{base_url}}/students/login`
- Body:
```json
{
  "id": "2024001",
  "password": "123456"
}
```
- 预期：`code: 0` 且返回学生信息（不含密码）
- 可在 Tests 中保存 `student_id`：
```javascript
if (pm.response.json().data && pm.response.json().data.id) {
  pm.environment.set('student_id', pm.response.json().data.id);
}
```

#### 3.3 查看学生信息
- Method: GET
- URL: `{{base_url}}/students/{{student_id}}`
- 预期：`code: 0`，返回学生信息；密码字段应为 `null` 或不返回

#### 3.4 更新学生信息
- Method: PUT
- URL: `{{base_url}}/students/{{student_id}}`
- Body:
```json
{
  "name": "张三丰",
  "phoneNumber": "13800138002",
  "email": "zhangsanfeng@example.com"
}
```
- 预期：`code: 0`

### 4. 宿舍管理接口测试

#### 4.1 获取宿舍列表
- Method: GET
- URL: `{{base_url}}/dorms`
- 可选参数: `?buildingId=B01&status=可用`
- 预期：`code: 0`，返回宿舍列表

#### 4.2 获取宿舍床位
- Method: GET
- URL: `{{base_url}}/dorms/D01-101/beds`
- 可选参数: `?status=可用`
- 预期：`code: 0`，返回床位列表

#### 4.3 选择床位
- Method: POST
- URL: `{{base_url}}/dorms/choose-bed?studentId={{student_id}}&bedId=BED-D01-101-1`
- 预期：`code: 0`

#### 4.4 退宿
- Method: POST
- URL: `{{base_url}}/dorms/checkout?studentId={{student_id}}`
- 预期：`code: 0`

### 5. 住宿分配接口测试

#### 5.1 获取当前住宿分配
- Method: GET
- URL: `{{base_url}}/allocations/student/{{student_id}}`
- 预期：`code: 0`，返回当前住宿信息

#### 5.2 获取住宿历史
- Method: GET
- URL: `{{base_url}}/allocations/student/{{student_id}}/history`
- 预期：`code: 0`，返回住宿历史记录

### 6. 缴费管理接口测试

#### 6.1 创建缴费记录
- Method: POST
- URL: `{{base_url}}/payments`
- Body:
```json
{
  "studentId": "2024001",
  "amount": 1200.00,
  "paymentMethod": "住宿费"
}
```
- 预期：`code: 0`

#### 6.2 获取学生缴费记录
- Method: GET
- URL: `{{base_url}}/payments/student/{{student_id}}`
- 可选参数: `?paymentMethod=住宿费`
- 预期：`code: 0`，返回缴费记录列表

<!-- 6.3 更新缴费状态：根据当前数据表结构已移除，不再提供该接口 -->

### 7. 维修管理接口测试

#### 7.1 创建维修工单
- Method: POST
- URL: `{{base_url}}/repairs`
- Body:
```json
{
  "studentId": "2024001",
  "dormitoryId": "D01-101",
  "repairType": "水电维修",
  "description": "宿舍水龙头漏水",
  "urgencyLevel": "一般"
}
```
- 预期：`code: 0`

#### 7.2 获取学生维修记录
- Method: GET
- URL: `{{base_url}}/repairs/student/{{student_id}}`
- 可选参数: `?status=待处理`
- 预期：`code: 0`，返回维修记录列表

#### 7.3 更新维修状态
- Method: PUT
- URL: `{{base_url}}/repairs/{{repairId}}/status?status=处理中`
- 预期：`code: 0`

### 8. 问卷管理接口测试

#### 8.1 提交问卷
- Method: POST
- URL: `{{base_url}}/questionnaire/submit`
- Body:
```json
{
  "studentId": "2024001",
  "answers": [
    {
      "sleepTimePreference": "早睡",
      "cleanlinessLevel": "爱整洁",
      "noiseTolerance": "能接受一点噪音",
      "eatingInRoom": "偶尔",
      "collectiveSpendingHabit": "愿意"
    }
  ],
  "tags": ["安静", "整洁", "不吸烟"]
}
```
- 预期：`code: 0`

#### 8.2 获取学生问卷数据
- Method: GET
- URL: `{{base_url}}/questionnaire/student/{{student_id}}`
- 预期：`code: 0`，返回问卷答案和标签

### 9. 管理员功能接口测试

#### 9.1 获取仪表盘统计
- Method: GET
- URL: `{{base_url}}/admin/dashboard`
- 预期：`code: 0`，返回统计数据

#### 9.2 获取所有学生
- Method: GET
- URL: `{{base_url}}/admin/students`
- 预期：`code: 0`，返回学生列表

#### 9.3 获取所有宿舍
- Method: GET
- URL: `{{base_url}}/admin/dormitories`
- 预期：`code: 0`，返回宿舍列表

#### 9.4 获取所有住宿分配
- Method: GET
- URL: `{{base_url}}/admin/allocations`
- 预期：`code: 0`，返回分配记录列表

### 10. 系统功能接口测试

#### 10.1 健康检查
- Method: GET
- URL: `{{base_url}}/health`
- 预期：`{"code":0,"message":"success","data":"OK"}`

#### 10.2 根路径
- Method: GET
- URL: `{{base_url}}/`
- 预期：返回运行提示字符串

### 11. Collection Runner 批量测试

在每个请求的 Tests 标签增加校验：
```javascript
pm.test('Status code is 200', function () { pm.response.to.have.status(200); });

pm.test('Response format', function () {
  const d = pm.response.json();
  pm.expect(d).to.have.property('code');
  pm.expect(d).to.have.property('message');
  pm.expect(d).to.have.property('data');
});

pm.test('Business success', function () {
  pm.expect(pm.response.json().code).to.eql(0);
});
```
运行：
1. 打开集合 → 右上 "…" → Run collection
2. 选择要运行的请求，点击 Run

### 12. 错误用例与边界测试

- 无效请求体 `{}`、无效 JSON、缺失必填字段等
- 超长字符串、特殊字符注入等
- 重复注册、不存在的学生ID等业务逻辑错误

## 常见问题（Postman/启动排查）

1) `/api` Whitelabel Error Page
- 解决：升级到包含 `RootController` 的版本并重启；或直接访问 `/api/health` 验证

2) Windows 无法清理 jar（被占用）
- 解决：停止正在运行的 `java -jar`（Ctrl+C 或 `taskkill /PID <PID> /F`）后再执行 `mvn clean`

3) 数据库连接失败
- 确认 MySQL 服务 `MySQL80` 运行中
- 校验 `application.yml` 的 URL/账号/密码

4) 数据访问错误: utf8mb4
- 解决：已在 `application.yml` 中配置 `hikari.connection-init-sql: SET NAMES utf8mb4`

5) BeanDefinitionStoreException: adminMapper
- 解决：已在所有 Mapper 接口添加 `@Mapper` 注解，并更新 `mybatis-spring` 依赖

## 开发计划

### 已完成功能 ✅
- [x] 项目基础架构搭建
- [x] 数据库实体类和Mapper（10张表）
- [x] 学生注册、登录、信息管理API
- [x] 宿舍管理API（列表、床位查询、选床、退宿）
- [x] 住宿分配API（当前分配、历史记录）
- [x] 缴费管理API（创建、查询、状态更新）
- [x] 维修管理API（创建、查询、状态更新）
- [x] 问卷和标签管理API（提交、查询）
- [x] 管理员功能API（仪表盘、数据查询）
- [x] 基础安全配置
- [x] Swagger API文档
- [x] 全局异常处理
- [x] 事务管理配置

### 已实现功能 ✅
- [x] JWT认证和授权（学生/管理员登录）
- [x] 管理员登录和管理功能
- [x] 智能分配算法
- [x] 数据统计和报表
- [x] 文件上传功能（头像、维修图片）
- [x] 消息通知系统
- [x] 宿舍调换申请和审核流程
- [x] 完整的业务流程管理

## 贡献指南

1. Fork 项目
2. 创建功能分支: `git checkout -b feature/AmazingFeature`
3. 提交更改: `git commit -m 'Add some AmazingFeature'`
4. 推送分支: `git push origin feature/AmazingFeature`
5. 提交Pull Request

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 联系方式

- 项目维护者: [您的姓名]
- 邮箱: [您的邮箱]
- 项目地址: [项目GitHub地址]

---

**注意**: 这是一个完整的宿舍管理系统，所有核心功能已实现。如有问题请提交Issue。
