# Playwright 测试快速启动指南

## 快速开始

### 1. 确保服务已启动

在运行测试之前，请确保：

```bash
# 启动后端服务（在 backend 目录）
cd backend
mvn spring-boot:run

# 启动前端服务（在新终端，frontend 目录）
cd frontend
npm run dev
```

### 2. 安装 Playwright（如果还没有安装）

```bash
cd frontend
npm install
npx playwright install
```

### 3. 运行测试

#### 运行所有测试
```bash
npm test
```

#### 运行单个测试文件
```bash
npx playwright test tests/student/login.spec.ts
```

#### 以有头模式运行（可以看到浏览器）
```bash
npm run test:headed
```

#### 交互式UI模式（推荐用于调试）
```bash
npm run test:ui
```

#### 调试模式
```bash
npm run test:debug
```

### 4. 查看测试报告

测试完成后，查看 HTML 报告：

```bash
npm run test:report
```

## 测试账号

系统使用以下测试账号：

**学生账号:**
- 学号: `2024001`
- 密码: `password`

**管理员账号:**
- 账号: `admin001`
- 密码: `password`

## 测试覆盖

✅ **学生端功能:**
- 登录/登出
- 仪表板
- 个人资料
- 宿舍管理
- 支付功能
- 维修工单
- 宿舍交换
- 通知查看
- 生活方式标签

✅ **管理员端功能:**
- 登录/登出
- 仪表板
- 学生管理
- 宿舍管理
- 分配管理
- 维修管理
- 支付管理
- 交换管理
- 通知管理

✅ **端到端流程:**
- 完整业务流程测试

## 常见问题

### 测试失败：连接超时

**原因:** 前端或后端服务未启动

**解决:**
1. 检查前端是否运行在 `http://localhost:5173`
2. 检查后端是否运行在 `http://localhost:8080/api`

### 测试失败：元素找不到

**原因:** 页面加载慢或元素选择器不正确

**解决:**
1. 检查网络连接
2. 增加超时时间（在 `playwright.config.ts` 中）
3. 检查页面是否正常加载

### 测试失败：登录失败

**原因:** 测试账号不正确或数据库未初始化

**解决:**
1. 确认测试账号是否存在（`2024001` / `password`）
2. 检查数据库是否已初始化
3. 检查后端日志查看错误信息

## 下一步

- 查看 `tests/README.md` 了解更多详细信息
- 查看 `playwright.config.ts` 了解配置选项
- 根据需要修改测试用例以适应实际的UI
