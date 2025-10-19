# IHome 宿舍管理系统 - 前端

## 技术栈
- **前端框架**: Vue 3 + Vite
- **路由管理**: Vue Router
- **状态管理**: Vuex
- **UI组件库**: Element Plus
- **HTTP客户端**: Axios
- **构建工具**: Vite
- **测试框架**: Playwright

## 快速开始

### 环境要求
- Node.js 16.0+
- npm 或 yarn

### 安装依赖
```bash
npm install
```

### 开发环境启动
```bash
npm run dev
```

### 生产环境构建
```bash
npm run build
```

### 预览构建结果
```bash
npm run preview
```

### 环境变量配置
```env
VITE_API_BASE=http://localhost:8080/api
VITE_USE_MOCK=false
```

## 项目结构

```
frontend/
├── src/
│   ├── api/                    # API接口
│   ├── components/             # 公共组件
│   ├── layouts/                # 布局组件
│   ├── router/                 # 路由配置
│   ├── store/                  # 状态管理
│   ├── styles/                 # 样式文件
│   ├── utils/                  # 工具函数
│   ├── views/                  # 页面组件
│   ├── App.vue                 # 根组件
│   └── main.ts                 # 入口文件
├── public/                     # 静态资源
├── package.json                # 项目配置
├── vite.config.ts              # Vite配置
└── README.md                   # 说明文档
```

## 核心功能

### 学生端功能
- 用户登录认证
- 个人信息管理
- 宿舍信息查询
- 调换申请提交
- 维修申请管理
- 缴费记录查询
- 消息通知查看

### 管理员端功能
- 管理员登录认证
- 学生信息管理
- 宿舍信息管理
- 调换申请审核
- 维修工单处理
- 缴费记录管理
- 数据统计报表
- 系统通知管理

## MockJS 使用指南

### 概述
项目集成了 MockJS 用于模拟后端 API 响应，支持分页、搜索、排序等功能。Mock 数据在开发环境中自动启用，无需额外配置。

### 功能特性
- ✅ 支持分页查询（page, size 参数）
- ✅ 支持搜索过滤（按学号、状态、类型等）
- ✅ 支持排序（sort 参数）
- ✅ 支持登录认证（学生/管理员）
- ✅ 支持 Token 刷新机制
- ✅ 模拟真实的数据结构和响应格式

### 已实现的 Mock API

#### 认证相关
- `POST /api/students/login` - 学生登录
- `POST /api/admin/login` - 管理员登录
- `POST /api/auth/refresh` - Token 刷新

#### 缴费管理
- `POST /api/payments` - 创建缴费记录
- `GET /api/payments` - 分页查询缴费记录
  - 支持参数：`page`, `size`, `studentId`, `paymentMethod`

#### 维修管理
- `POST /api/repairs` - 创建维修工单
- `GET /api/repairs` - 分页查询维修记录
  - 支持参数：`page`, `size`, `studentId`, `dormitoryId`, `repairType`, `status`
- `PUT /api/repairs/{id}/status` - 更新维修状态

#### 宿舍管理
- `GET /api/dorms` - 分页查询宿舍列表
  - 支持参数：`page`, `size`, `buildingId`, `status`, `name`
- `GET /api/dorms/{id}/beds` - 查询宿舍床位
- `POST /api/dorms/choose-bed` - 选择床位
- `POST /api/dorms/checkout` - 退宿

#### 住宿分配
- `GET /api/allocations/student/{id}` - 获取学生当前住宿
- `GET /api/allocations/student/{id}/history` - 获取住宿历史

#### 问卷管理
- `POST /api/questionnaire/submit` - 提交问卷
- `GET /api/questionnaire/student/{id}` - 获取学生问卷

#### 管理员功能
- `GET /api/admin/dashboard` - 仪表盘统计
- `GET /api/admin/students` - 分页查询学生列表
- `GET /api/admin/dormitories` - 分页查询宿舍列表
- `GET /api/admin/allocations` - 分页查询住宿分配

### 测试步骤

#### 1. 启动前端项目
```bash
cd frontend
npm install
npm run dev
```

#### 2. 测试登录功能
1. 访问 `http://localhost:5173/login`
2. 选择用户类型（学生/管理员）
3. 输入任意用户名和密码（Mock 会接受任何非空输入）
4. 点击登录，系统会返回模拟的用户信息和 Token

#### 3. 测试分页功能
1. 登录后访问缴费管理页面
2. 观察表格底部的分页组件
3. 尝试切换页码和每页条数
4. 验证数据是否正确分页显示

#### 4. 测试搜索功能
1. 在搜索表单中输入条件
2. 点击搜索按钮
3. 观察表格数据是否按条件过滤
4. 点击重置按钮清空搜索条件

#### 5. 测试排序功能
1. 点击表格列头的排序按钮
2. 观察数据是否按指定字段排序
3. 尝试升序和降序排列

#### 6. 测试 CRUD 操作
1. 创建新的缴费记录或维修工单
2. 观察数据是否正确添加到列表
3. 尝试更新记录状态
4. 验证操作反馈消息

### 自定义 Mock 数据

#### 修改数据量
在 `src/mocks/index.ts` 中调整生成规则：
```javascript
// 生成 100 条缴费记录
'list|100': [{ ... }]
```

#### 添加新的 API
```javascript
Mock.mock('/api/your-endpoint', 'get', (options) => {
  // 解析请求参数
  const url = new URL(options.url, 'http://localhost')
  const page = parseInt(url.searchParams.get('page') || '1')
  
  // 返回模拟数据
  return {
    code: 0,
    message: 'success',
    data: { /* your data */ }
  }
})
```

#### 模拟错误响应
```javascript
Mock.mock('/api/error-endpoint', 'get', {
  code: -1,
  message: '模拟错误信息',
  data: null
})
```

### 与真实后端切换

#### 启用真实后端
1. 确保后端服务运行在 `http://localhost:8080`
2. 在 `src/mocks/index.ts` 中注释掉对应的 Mock 规则
3. 重启前端项目

#### 混合使用
- Mock 只覆盖已定义的接口
- 未定义的接口会直接请求真实后端
- 可以通过注释/取消注释来灵活控制

### 调试技巧

#### 查看 Mock 请求
1. 打开浏览器开发者工具
2. 切换到 Network 标签
3. 执行操作，观察请求和响应
4. Mock 请求会显示为 `(from service worker)` 或 `(from disk cache)`

#### 修改响应延迟
```javascript
Mock.setup({ timeout: '1000-3000' }) // 1-3秒随机延迟
```

#### 动态数据
MockJS 支持动态生成数据：
```javascript
{
  id: '@increment',           // 自增ID
  name: '@cname',             // 中文姓名
  email: '@email',            // 邮箱
  date: '@datetime',          // 日期时间
  amount: '@float(100, 1000, 2, 2)', // 浮点数
  status: '@pick(["可用", "已满"])'   // 随机选择
}
```

### 注意事项
- Mock 数据在页面刷新后会重新生成
- 分页参数从 1 开始（不是 0）
- 所有时间字段使用 ISO 格式
- 金额字段保留两位小数
- 状态字段使用中文描述

## 技术架构

### 路由管理
- 文件位置：`src/router/index.ts`
- 功能：包含登录守卫、权限控制
- 特性：支持动态路由、路由懒加载

### 状态管理
- 文件位置：`src/store/index.ts`
- 管理内容：用户token、用户信息、刷新token
- 特性：持久化存储、响应式更新

### HTTP请求
- 文件位置：`src/api/http.ts` 和 `src/api/index.ts`
- 功能：axios拦截器、token自动刷新、API封装
- 特性：统一错误处理、请求响应拦截

### Mock数据
- 文件位置：`src/mocks/index.ts`
- 功能：MockJS配置、模拟API响应
- 特性：支持分页、搜索、排序

## 部署说明

### 开发环境
```bash
npm run dev
# 访问 http://localhost:5173
```

### 生产环境
```bash
npm run build
npm run preview
# 或使用nginx部署dist目录
```

### Docker部署
```bash
docker build -t ihome-frontend .
docker run -p 80:80 ihome-frontend
```

## 测试

### 运行测试
```bash
npm run test
```

### 测试报告
```bash
npm run test:report
```

## 贡献指南

1. Fork 项目
2. 创建功能分支: `git checkout -b feature/AmazingFeature`
3. 提交更改: `git commit -m 'Add some AmazingFeature'`
4. 推送分支: `git push origin feature/AmazingFeature`
5. 提交Pull Request

## 许可证

本项目采用 MIT 许可证

## 联系方式

- 项目维护者: [您的姓名]
- 邮箱: [您的邮箱]
- 项目地址: [项目GitHub地址]


