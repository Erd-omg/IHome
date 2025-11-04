# IHome 系统自动化测试文档

本项目使用 Playwright 进行端到端自动化测试，覆盖系统的所有功能模块。

## 测试结构

```
tests/
├── helpers/              # 测试辅助函数
│   ├── auth.ts          # 登录/登出辅助函数
│   ├── test-data.ts     # 测试数据常量
│   └── wait-helpers.ts  # 等待和异步操作辅助函数
├── student/             # 学生端功能测试
│   ├── login.spec.ts
│   ├── dashboard.spec.ts
│   ├── dormitory.spec.ts
│   ├── payments.spec.ts
│   ├── repairs.spec.ts
│   ├── exchange.spec.ts
│   ├── notices.spec.ts
│   └── profile.spec.ts
├── admin/               # 管理员端功能测试
│   ├── admin-login.spec.ts
│   ├── admin-dashboard.spec.ts
│   ├── admin-students.spec.ts
│   ├── admin-dormitories.spec.ts
│   ├── admin-allocations.spec.ts
│   ├── admin-repairs.spec.ts
│   ├── admin-payments.spec.ts
│   ├── admin-notifications.spec.ts
│   └── admin-exchanges.spec.ts
└── e2e/                 # 端到端完整流程测试
    └── complete-flow.spec.ts
```

## 运行测试

### 安装依赖

```bash
cd frontend
npm install
```

### 安装 Playwright 浏览器

```bash
npx playwright install
```

### 运行所有测试

```bash
npm test
```

### 运行指定测试文件

```bash
npx playwright test tests/student/login.spec.ts
```

### 以有头模式运行（可以看到浏览器）

```bash
npm run test:headed
```

### 以调试模式运行

```bash
npm run test:debug
```

### 使用 UI 模式运行（交互式）

```bash
npm run test:ui
```

### 查看测试报告

```bash
npm run test:report
```

## 测试配置

测试配置在 `playwright.config.ts` 中，主要配置项：

- **baseURL**: 前端应用的基础URL（默认: http://localhost:5173）
- **timeout**: 测试超时时间（默认: 30秒）
- **browsers**: 支持的浏览器（Chromium, Firefox, WebKit）
- **retries**: 失败重试次数

## 测试账号

### 学生账号
- 学号: `2024001` ~ `2024010`
- 密码: `password`

### 管理员账号
- 账号: `admin001`, `admin002`
- 密码: `password`

## 环境要求

1. 前端服务运行在 `http://localhost:5173`
2. 后端API服务运行在 `http://localhost:8080/api`
3. 数据库已初始化并包含测试数据

## 测试覆盖范围

### 学生端功能
- ✅ 登录/登出
- ✅ 仪表板
- ✅ 个人资料管理
- ✅ 宿舍信息查看和搜索
- ✅ 床位选择
- ✅ 支付记录查看和创建
- ✅ 维修工单创建和查看
- ✅ 宿舍交换申请
- ✅ 通知查看
- ✅ 生活方式标签设置

### 管理员端功能
- ✅ 登录/登出
- ✅ 仪表板
- ✅ 学生管理（CRUD）
- ✅ 宿舍管理（CRUD）
- ✅ 分配管理
- ✅ 维修工单管理
- ✅ 支付记录管理
- ✅ 交换申请审核
- ✅ 通知管理
- ✅ 学生导入

### 端到端流程
- ✅ 学生完整使用流程
- ✅ 管理员完整管理流程
- ✅ 学生和管理员交互流程

## 编写新测试

1. 在相应的目录下创建测试文件（如 `tests/student/new-feature.spec.ts`）
2. 使用 `test.describe` 组织测试用例
3. 使用辅助函数简化测试代码
4. 使用 `waitFor*` 函数处理异步操作
5. 使用有意义的测试描述

示例：

```typescript
import { test, expect } from '@playwright/test';
import { loginAsStudent } from '../helpers/auth';
import { waitForPageLoad } from '../helpers/wait-helpers';

test.describe('新功能测试', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsStudent(page);
  });

  test('应该能够使用新功能', async ({ page }) => {
    await page.goto('/new-feature');
    await waitForPageLoad(page);
    
    // 测试逻辑
    await expect(page.locator('body')).toBeVisible();
  });
});
```

## 注意事项

1. 测试前确保服务已启动
2. 测试可能会修改数据库数据，建议使用测试数据库
3. 某些测试可能依赖于现有的测试数据
4. 如果测试失败，检查网络连接和服务状态
5. 测试中使用 `waitFor*` 函数代替固定的 `waitForTimeout`

## 持续集成

可以在 CI/CD 流水线中运行测试：

```yaml
- name: Install dependencies
  run: npm install

- name: Install Playwright Browsers
  run: npx playwright install --with-deps

- name: Run tests
  run: npm test

- name: Upload test results
  if: always()
  uses: actions/upload-artifact@v2
  with:
    name: playwright-report
    path: playwright-report/
```

## 故障排除

### 测试超时
- 增加 `timeout` 配置
- 检查网络连接
- 检查服务是否正常运行

### 元素找不到
- 使用 `waitForElement` 等待元素加载
- 检查选择器是否正确
- 检查页面是否完全加载

### 登录失败
- 检查测试账号是否正确
- 检查后端服务是否运行
- 检查API地址配置

## 贡献

添加新功能时，请同时添加相应的测试用例，确保功能被正确测试。
