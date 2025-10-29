# IHome 宿舍管理系统

<div align="center">

![License](https://img.shields.io/badge/license-MIT-blue.svg)
![Java](https://img.shields.io/badge/Java-21-orange.svg)
![Vue](https://img.shields.io/badge/Vue-3-42b983.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)

**一个功能完善、体验优良的现代化宿舍管理系统**

[快速开始](#-快速开始) • [功能特性](#-功能特性) • [技术架构](#-技术架构) • [部署指南](#-部署指南)

</div>

---

## 📖 项目简介

IHome是一个基于Spring Boot + Vue 3的现代化宿舍管理系统，旨在为大学提供完整的宿舍分配、维修报修、费用管理、通知公告等功能。系统采用前后端分离架构，提供学生端和管理员端双界面，支持智能分配算法和室友匹配系统。

### ✨ 核心特性

- 🎓 **智能分配**: 基于专业、标签、问卷的多维度匹配算法
- 🔧 **维修报修**: 在线提交维修工单，实时跟踪进度
- 💰 **费用管理**: 多种缴费类型，自定义金额，自动提醒
- 🔄 **调换申请**: 床位模糊搜索，申请进度跟踪
- 📢 **通知公告**: 系统公告、定向通知，优先级管理
- 📊 **数据统计**: 实时仪表盘，多维度图表可视化

---

## 🎯 功能特性

### 学生端功能

#### 📋 首页模块
- ✅ 核心功能入口（调换宿舍、缴费、维修、个人信息）
- ✅ 最新通知展示（按时间排序）
- ✅ 快速搜索功能

#### 🏠 宿舍管理
- ✅ 床位自主选择（上铺/下铺）
- ✅ 调换宿舍申请
- ✅ 宿舍信息和室友查看

#### 💰 缴费管理
- ✅ 在线缴费（多种类型和支付方式）
- ✅ 缴费记录查询和搜索
- ✅ 电费余额查询
- ✅ 电费提醒设置

#### 🔧 维修管理
- ✅ 在线提交维修工单
- ✅ 图片上传功能
- ✅ 维修进度跟踪
- ✅ 反馈评价功能

#### 👤 个人信息
- ✅ 查看和编辑个人信息
- ✅ 室友标签管理（自动生成+手动选择）
- ✅ 室友匹配问卷

### 管理员端功能

#### 📊 仪表盘
- ✅ 统计数据展示（宿舍、学生、维修、缴费）
- ✅ 图表可视化（饼图、柱状图、折线图）
- ✅ 实时数据更新

#### 🏠 宿舍管理
- ✅ 宿舍信息增删改查
- ✅ 床位管理
- ✅ 智能分配和手动分配
- ✅ 入住退宿管理

#### 👥 学生管理
- ✅ 学生信息管理
- ✅ 批量导入学生
- ✅ 分配记录查询

#### 🔍 审核管理
- ✅ 调换申请审核（同意/拒绝）
- ✅ 维修工单管理
- ✅ 缴费异常处理

#### 📢 通知管理
- ✅ 系统公告发布
- ✅ 定向通知发送
- ✅ 通知统计查看

---

## 🛠️ 技术架构

### 后端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.x | 核心框架 |
| MyBatis Plus | 3.5.x | ORM框架 |
| Spring Security | 6.x | 安全框架 |
| MySQL | 8.0 | 数据库 |
| JWT | - | 认证机制 |
| Maven | 3.9+ | 构建工具 |

### 前端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.x | 核心框架 |
| TypeScript | 5.x | 类型支持 |
| Element Plus | 2.x | UI组件库 |
| Vue Router | 4.x | 路由管理 |
| Vuex | 4.x | 状态管理 |
| Vite | 5.x | 构建工具 |
| Axios | 1.x | HTTP客户端 |

---

## 🚀 快速开始

### 环境要求

- **Java**: JDK 21+
- **Maven**: 3.9+
- **Node.js**: 18+
- **MySQL**: 8.0+

### 启动步骤

#### 1. 克隆项目

```bash
git clone https://github.com/yourusername/IHome.git
cd IHome
```

#### 2. 数据库初始化

**方式1: 使用MySQL命令行**

```bash
# 开发环境（包含测试数据）
mysql -u root -p < backend/src/main/resources/database-init-with-data.sql

# 生产环境（仅创建结构）
mysql -u root -p < backend/src/main/resources/database-init.sql
```

**方式2: 使用数据库管理工具**

1. 打开MySQL Workbench或其他数据库管理工具
2. 打开 `backend/src/main/resources/database-init-with-data.sql`
3. 执行脚本

**方式3: 使用 Docker 一键启动（可选）**

```bash
# 启动 MySQL、后端、前端（首次会自动初始化数据库：01-init.sql与02-data.sql）
docker compose up -d

# 访问
# 前端： http://localhost:5173
# 后端： http://localhost:8080/api
```

> 说明：`docker-compose.yml` 已挂载 `backend/src/main/resources/database-init.sql` 和 `database-init-with-data.sql` 到容器的初始化目录。

### 数据库初始化说明（核心）

1) 文件说明
- `database-init.sql`: 仅创建数据库结构与必要默认数据（生产环境推荐）
- `database-init-with-data.sql`: 创建结构并插入测试数据（开发/测试推荐）

2) 默认配置
- 数据库名: `ihome`
- 字符集/排序: `utf8mb4 / utf8mb4_unicode_ci`
- 引擎: `InnoDB`

3) 连接配置示例（backend/src/main/resources/application.yml）
```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/ihome?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=utf8&autoReconnect=true
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
```

4) 测试数据概览（使用 with-data 脚本）
- 管理员账号：`admin001/admin002`（默认密码：password）
- 学生账号：`2024001` ~ `2024010`（默认密码：password）
- 宿舍/床位：示例宿舍与床位完整生成
- 典型业务数据：部分分配记录、缴费记录、维修工单、系统通知

5) 数据库结构（核心表）
- buildings / dormitories / beds
- students / admins
- questionnaire_answers / roommate_tags
- payment_records / repair_orders / repair_feedback
- dormitory_allocations / dormitory_switches
- notifications / operation_logs
- allocation_feedback / algorithm_weights
- electricity_bills / electricity_reminders / electricity_payments

6) 注意事项
- 使用 `utf8mb4` 字符集
- 外键均已设置级联删除/更新
- 已为常用查询字段创建索引
- 密码字段存储为 BCrypt 散列

7) 重置数据库（可选）
```bash
# 备份（可选）
mysqldump -u root -p ihome > backup.sql

# 删除并重建（进入 mysql 命令行）
DROP DATABASE IF EXISTS ihome;
source backend/src/main/resources/database-init.sql;
```

8) 常见问题
- Unknown database 'ihome'：脚本中已创建数据库；如存在旧库建议先 DROP
- 初始化后如何验证：`USE ihome; SHOW TABLES;`
- 忘记管理员密码：直接更新数据库中管理员记录的 password（BCrypt 后的值）

#### 3. 配置后端

编辑 `backend/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ihome?useSSL=false&serverTimezone=UTC
    username: root
    password: your_password
```

#### 4. 启动后端

```bash
cd backend
mvn clean package -DskipTests
java -jar target/ihome-0.0.1-SNAPSHOT.jar
```

后端服务运行在: `http://localhost:8080`

#### 5. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端服务运行在: `http://localhost:5173`

#### 6. 访问系统

- **前端地址**: http://localhost:5173
- **后端API**: http://localhost:8080/api
- **健康检查**: http://localhost:8080/api/health

---

## 👤 测试账号

### 学生账号

| 学号 | 密码 | 姓名 | 性别 | 专业 |
|------|------|------|------|------|
| 2024001 | password | 张三 | 男 | 软件工程 |
| 2024002 | password | 李四 | 男 | 网络工程 |
| 2024003 | password | 王五 | 男 | 信息安全 |
| 2024004 | password | 赵六 | 女 | 软件工程 |
| 2024005 | password | 钱七 | 女 | 网络工程 |

### 管理员账号

| 账号 | 密码 | 姓名 | 角色 |
|------|------|------|------|
| admin001 | password | 系统管理员 | 系统管理员 |
| admin002 | password | 宿管阿姨 | 宿管员 |

> 注意：所有账号密码统一为 `password`

---

## 🏗️ 项目结构

```
IHome/
├── backend/                    # 后端Spring Boot项目
│   ├── src/main/java/          # Java源代码
│   ├── src/main/resources/     # 配置文件
│   │   ├── application.yml     # 应用配置
│   │   ├── database-init.sql  # 数据库初始化脚本
│   │   └── database-init-with-data.sql  # 含测试数据
│   └── pom.xml                # Maven配置
├── frontend/                   # 前端Vue项目
│   ├── src/                   # Vue源代码
│   │   ├── api/               # API接口
│   │   ├── views/             # 页面组件
│   │   ├── components/         # 公共组件
│   │   └── router/            # 路由配置
│   └── package.json           # NPM配置
└── README.md                  # 项目说明（本文档）
```

---

## 🎯 核心算法

### 智能分配算法

系统采用多维度加权匹配算法，综合考虑以下因素：

```
总分 = 专业匹配度 × 35% 
     + 标签匹配度 × 15% 
     + 床位匹配度 × 20% 
     + 问卷匹配度 × 30%
```

**分配规则**:
1. 性别隔离：只分配同性宿舍
2. 专业优先：同专业优先分配
3. 智能匹配：基于问卷和标签计算匹配度
4. 下铺优先：特殊标签（如行动不便）优先分配下铺

### 室友标签系统

**自动生成标签**（基于问卷答案）:
- 睡眠时间偏好 → 早睡/晚睡
- 整洁程度 → 整洁/随意
- 噪音容忍度 → 安静/适度噪音
- 房间用餐 → 宿舍用餐/不在宿舍用餐
- 集体消费 → 集体消费/独立消费

**冲突检测**: 6对冲突标签
- 安静 ↔ 吵闹
- 整洁 ↔ 邋遢
- 早睡 ↔ 晚睡
- 作息规律 ↔ 作息不规律
- 宿舍用餐 ↔ 不在宿舍用餐
- 集体消费 ↔ 独立消费

---


## 🚢 部署指南

### 开发环境部署

详见上述"快速开始"章节

### 生产环境部署

1. **环境准备**
   - Linux服务器（推荐阿里云ECS）
   - 安装MySQL 8.0
   - 安装Nginx

2. **后端部署**
   ```bash
   # 打包
   cd backend
   mvn clean package -DskipTests
   
   # 启动
   nohup java -jar target/ihome-0.0.1-SNAPSHOT.jar &
   ```

3. **前端部署**
   ```bash
   # 构建
   cd frontend
   npm run build
   
   # 部署到Nginx
   cp -r dist/* /usr/share/nginx/html/
   ```

4. **Nginx配置**
   ```nginx
   server {
       listen 80;
       server_name your-domain.com;
       
       location /api {
           proxy_pass http://localhost:8080;
       }
       
       location / {
           root /usr/share/nginx/html;
           try_files $uri $uri/ /index.html;
       }
   }
   ```

---

## 🤝 贡献指南

1. Fork 本项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 提交 Pull Request

---

## 📞 联系方式

如有问题或建议，请通过以下方式联系：

- **项目Issues**: [GitHub Issues](https://github.com/yourusername/IHome/issues)

---

<div align="center">

**⭐ 如果这个项目对你有帮助，请给它一个星标！⭐**

Made with ❤️ by IHome Team

</div>

---
