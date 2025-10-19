# IHome宿舍管理系统项目结构说明

## 📁 项目整体结构

```
IHome/
├── backend/                    # 后端Spring Boot项目
├── frontend/                   # 前端Vue.js项目
├── README.md                   # 项目说明文档
├── PROJECT_STATUS_AND_SOLUTIONS.md # 项目状态与解决方案
├── DEVELOPMENT_TODO_LIST.md    # 开发待办事项
├── PRD.md                      # 产品需求文档
├── COMPREHENSIVE_TEST_GUIDE.md # 测试指南
├── PROJECT_STRUCTURE.md        # 项目结构说明
├── PORT_CONFIGURATION.md       # 端口配置说明
├── 部署说明.md                  # 部署说明
├── docker-compose.yml          # Docker配置
├── start-backend.bat           # 后端启动脚本
└── init-database.bat           # 数据库初始化脚本
```

## 🔧 后端项目结构 (backend/)

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/ihome/
│   │   │   ├── IhomeApplication.java          # 主启动类
│   │   │   ├── common/                        # 公共组件
│   │   │   │   ├── ApiResponse.java          # 统一响应格式
│   │   │   │   ├── JwtResponse.java          # JWT响应格式
│   │   │   │   ├── JwtUtils.java             # JWT工具类
│   │   │   │   └── Result.java               # 结果封装类
│   │   │   ├── config/                        # 配置类
│   │   │   │   ├── SecurityConfig.java       # Spring Security配置
│   │   │   │   ├── WebConfig.java            # Web配置
│   │   │   │   ├── MybatisPlusConfig.java    # MyBatis Plus配置
│   │   │   │   └── CorsConfig.java           # 跨域配置
│   │   │   ├── controller/                    # 控制器层
│   │   │   │   ├── AdminController.java      # 管理员控制器
│   │   │   │   ├── StudentController.java    # 学生控制器
│   │   │   │   ├── AuthController.java       # 认证控制器
│   │   │   │   ├── DormitoryController.java  # 宿舍控制器
│   │   │   │   ├── RepairController.java     # 维修控制器
│   │   │   │   ├── PaymentController.java    # 缴费控制器
│   │   │   │   ├── NotificationController.java # 通知控制器
│   │   │   │   ├── SwitchController.java     # 调换控制器
│   │   │   │   ├── AllocationController.java # 分配控制器
│   │   │   │   ├── StatisticsController.java # 统计控制器
│   │   │   │   ├── QuestionnaireController.java # 问卷控制器
│   │   │   │   ├── NoticeController.java     # 公告控制器
│   │   │   │   ├── BuildingController.java   # 楼栋控制器
│   │   │   │   ├── BedController.java        # 床位控制器
│   │   │   │   └── RoommateTagController.java # 室友标签控制器
│   │   │   ├── entity/                        # 实体类
│   │   │   │   ├── Admin.java                # 管理员实体
│   │   │   │   ├── Student.java              # 学生实体
│   │   │   │   ├── Dormitory.java            # 宿舍实体
│   │   │   │   ├── DormitoryAllocation.java  # 宿舍分配实体
│   │   │   │   ├── RepairOrder.java          # 维修订单实体
│   │   │   │   ├── PaymentRecord.java        # 缴费记录实体
│   │   │   │   ├── Notification.java         # 通知实体
│   │   │   │   ├── DormitorySwitch.java      # 宿舍调换实体
│   │   │   │   ├── QuestionnaireAnswer.java  # 问卷答案实体
│   │   │   │   ├── Notice.java               # 公告实体
│   │   │   │   ├── Building.java             # 楼栋实体
│   │   │   │   ├── Bed.java                  # 床位实体
│   │   │   │   └── RoommateTag.java          # 室友标签实体
│   │   │   ├── mapper/                        # 数据访问层
│   │   │   │   ├── AdminMapper.java          # 管理员Mapper
│   │   │   │   ├── StudentMapper.java        # 学生Mapper
│   │   │   │   ├── DormitoryMapper.java      # 宿舍Mapper
│   │   │   │   ├── DormitoryAllocationMapper.java # 分配Mapper
│   │   │   │   ├── RepairOrderMapper.java    # 维修Mapper
│   │   │   │   ├── PaymentRecordMapper.java  # 缴费Mapper
│   │   │   │   ├── NotificationMapper.java   # 通知Mapper
│   │   │   │   ├── DormitorySwitchMapper.java # 调换Mapper
│   │   │   │   ├── QuestionnaireAnswerMapper.java # 问卷Mapper
│   │   │   │   ├── NoticeMapper.java         # 公告Mapper
│   │   │   │   ├── BuildingMapper.java       # 楼栋Mapper
│   │   │   │   ├── BedMapper.java            # 床位Mapper
│   │   │   │   └── RoommateTagMapper.java    # 室友标签Mapper
│   │   │   ├── service/                       # 服务层
│   │   │   │   ├── StatisticsService.java    # 统计服务
│   │   │   │   ├── AllocationService.java    # 分配服务
│   │   │   │   ├── DormitoryService.java     # 宿舍服务
│   │   │   │   ├── NotificationService.java  # 通知服务
│   │   │   │   ├── RepairService.java        # 维修服务
│   │   │   │   └── PaymentService.java       # 缴费服务
│   │   │   └── util/                         # 工具类
│   │   │       └── PasswordUtils.java        # 密码工具类
│   │   └── resources/                         # 资源文件
│   │       ├── application.yml               # 主配置文件
│   │       ├── application-dev.yml           # 开发环境配置
│   │       ├── application-test.yml          # 测试环境配置
│   │       ├── db-init.sql                   # 数据库初始化脚本
│   │       ├── db-init-simple.sql            # 简化数据库脚本
│   │       ├── mock_data.sql                 # 模拟数据脚本
│   │       └── mock-data-complete.sql        # 完整模拟数据脚本
│   └── test/                                 # 测试代码
│       ├── java/com/ihome/
│       │   └── FunctionalTestRunner.java     # 功能测试运行器
│       └── resources/
│           └── test-data.sql                 # 测试数据脚本
├── pom.xml                                   # Maven配置文件
└── README.md                                 # 后端说明文档
```

## 🎨 前端项目结构 (frontend/)

```
frontend/
├── src/
│   ├── api/                                  # API接口
│   │   ├── http.ts                          # HTTP请求配置
│   │   └── index.ts                         # API接口定义
│   ├── components/                           # 公共组件
│   │   ├── Navbar.vue                       # 导航栏组件
│   │   └── NotificationCenter.vue           # 通知中心组件
│   ├── layouts/                              # 布局组件
│   │   ├── AdminLayout.vue                  # 管理员布局
│   │   └── MainLayout.vue                   # 主布局
│   ├── router/                               # 路由配置
│   │   └── index.ts                         # 路由定义
│   ├── store/                                # 状态管理
│   │   └── index.ts                         # Vuex store
│   ├── styles/                               # 样式文件
│   │   └── theme.css                        # 主题样式
│   ├── utils/                                # 工具函数
│   │   └── csv.ts                           # CSV处理工具
│   ├── views/                                # 页面组件
│   │   ├── Login.vue                        # 登录页面
│   │   ├── Home.vue                         # 首页
│   │   ├── StudentDashboard.vue             # 学生仪表盘
│   │   ├── AdminDashboard.vue               # 管理员仪表盘
│   │   ├── AdminStudents.vue                # 学生管理页面
│   │   ├── AdminDormitories.vue             # 宿舍管理页面
│   │   ├── AdminAllocations.vue             # 分配记录页面
│   │   ├── AdminNotifications.vue           # 通知管理页面
│   │   ├── AdminRepairs.vue                 # 维修管理页面
│   │   ├── AdminPayments.vue                # 缴费管理页面
│   │   ├── AdminExchanges.vue               # 调换审核页面
│   │   ├── StudentProfile.vue               # 学生个人信息页面
│   │   ├── StudentDormitory.vue             # 学生宿舍信息页面
│   │   ├── StudentRepairs.vue               # 学生维修申请页面
│   │   ├── StudentPayments.vue              # 学生缴费页面
│   │   ├── StudentNotifications.vue         # 学生通知页面
│   │   ├── StudentExchanges.vue             # 学生调换申请页面
│   │   ├── StudentQuestionnaire.vue         # 学生问卷页面
│   │   ├── StudentNotices.vue               # 学生公告页面
│   │   ├── StudentRoommateTags.vue          # 学生室友标签页面
│   │   └── NotFound.vue                     # 404页面
│   ├── App.vue                               # 根组件
│   ├── main.ts                               # 入口文件
│   └── env.d.ts                              # 环境类型定义
├── public/                                   # 静态资源
├── tests/                                    # 测试文件
│   ├── auth.spec.ts                         # 认证测试
│   ├── admin-dashboard.spec.ts              # 管理员仪表盘测试
│   ├── admin-management.spec.ts             # 管理员功能测试
│   ├── dormitory-management.spec.ts         # 宿舍管理测试
│   ├── exchange-management.spec.ts          # 调换管理测试
│   ├── notice-management.spec.ts            # 公告管理测试
│   ├── payment-management.spec.ts           # 缴费管理测试
│   ├── profile-management.spec.ts           # 个人信息测试
│   ├── repair-management.spec.ts            # 维修管理测试
│   ├── student-dashboard.spec.ts            # 学生仪表盘测试
│   ├── functional-tests.spec.ts             # 功能测试
│   └── helpers.ts                            # 测试辅助函数
├── package.json                              # NPM配置文件
├── vite.config.ts                            # Vite配置文件
├── playwright.config.ts                      # Playwright测试配置
└── README.md                                 # 前端说明文档
```

## 📚 文档结构

```
文档/
├── PRD.md                                    # 产品需求文档
├── PROBLEM_RESOLUTION_PROGRESS.md            # 问题解决进度
├── 测试指南.md                               # 测试指南
├── PROJECT_STRUCTURE.md                      # 项目结构说明（本文档）
├── DEVELOPMENT_TODO_LIST.md                  # 开发待办事项
├── FUNCTIONAL_TESTING_GUIDE.md               # 功能测试指南
├── AI_ALLOCATION_ALGORITHM.md                # AI分配算法说明
├── DATA_STATISTICS_REPORTS.md                # 数据统计报告
├── DORMITORY_SWITCH_SYSTEM.md                # 宿舍调换系统说明
├── FILE_UPLOAD_SYSTEM.md                     # 文件上传系统说明
├── MYSQL_INSTALLATION_GUIDE.md               # MySQL安装指南
├── NOTIFICATION_SYSTEM.md                    # 通知系统说明
└── TROUBLESHOOTING.md                        # 故障排除指南
```

## 🚀 脚本文件

```
脚本/
├── start-backend-simple.bat                  # 简单后端启动脚本
├── start-backend.bat                         # 完整后端启动脚本
├── start-system-clean.bat                    # 清理启动脚本
├── start-system.bat                          # 系统启动脚本
└── init-database.bat                         # 数据库初始化脚本
```

## 🔧 配置文件

```
配置文件/
├── admin_dashboard.json                      # 管理员仪表盘配置
├── admin_dormitories.json                    # 宿舍管理配置
├── admin_login.json                          # 管理员登录配置
├── admin_students.json                       # 学生管理配置
├── notices.json                              # 公告配置
├── student_dorms.json                        # 学生宿舍配置
├── student_login.json                        # 学生登录配置
├── student_notifications.json                # 学生通知配置
├── student_profile.json                      # 学生个人信息配置
└── switches.json                             # 调换配置
```

## 📊 数据库结构

### 主要数据表
- **students**: 学生信息表
- **admins**: 管理员信息表
- **dormitories**: 宿舍信息表
- **dormitory_allocations**: 宿舍分配记录表
- **repair_orders**: 维修订单表
- **payment_records**: 缴费记录表
- **notifications**: 通知表
- **dormitory_switches**: 宿舍调换申请表
- **questionnaire_answers**: 问卷答案表
- **notices**: 公告表
- **buildings**: 楼栋表
- **beds**: 床位表
- **roommate_tags**: 室友标签表

## 🎯 技术栈

### 后端技术
- **框架**: Spring Boot 3.x
- **安全**: Spring Security + JWT
- **数据库**: MySQL 8.0
- **ORM**: MyBatis Plus
- **构建工具**: Maven

### 前端技术
- **框架**: Vue.js 3.x
- **UI库**: Element Plus
- **状态管理**: Vuex
- **路由**: Vue Router
- **HTTP客户端**: Axios
- **构建工具**: Vite
- **测试框架**: Playwright

## 🔄 开发流程

### 1. 环境准备
1. 安装JDK 17+
2. 安装Node.js 16+
3. 安装MySQL 8.0
4. 安装Maven 3.6+

### 2. 项目启动
1. 克隆项目代码
2. 初始化数据库
3. 启动后端服务
4. 启动前端服务

### 3. 开发规范
- 后端遵循RESTful API设计
- 前端遵循Vue.js最佳实践
- 代码提交前进行测试
- 保持文档更新

---
*最后更新: 2025-09-20*