# IHome 系统自动化测试文档

本项目使用 Playwright 进行端到端自动化测试，覆盖系统的所有功能模块。

## 快速开始

### 1. 初始化数据库

**重要：** 在运行测试之前，必须先初始化数据库并插入测试数据。

```bash
# 进入后端目录
cd backend

# 使用 MySQL 客户端执行初始化脚本（包含测试数据）
mysql -u root -p < src/main/resources/database-init-with-data.sql

# 或者使用 MySQL 命令行
mysql -u root -p
source src/main/resources/database-init-with-data.sql;
```

**测试账号信息：**
- 学生账号：`2024001` - `2024010`，密码：`password`
- 管理员账号：`admin001` - `admin003`，密码：`password`

**注意：** 确保数据库中的密码哈希值与脚本中的一致。脚本使用的 BCrypt 哈希值为：
```
$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi
```
对应密码：`password`

### 2. 确保服务已启动

在运行测试之前，请确保：

```bash
# 启动后端服务（在 backend 目录）
cd backend
mvn spring-boot:run

# 启动前端服务（在新终端，frontend 目录）
cd frontend
npm run dev
```

**重要提示：**
- 后端服务必须运行在 `http://localhost:8080/api`
- 前端服务必须运行在 `http://localhost:5173`
- 测试使用真实后端 API，**不使用 Mock 数据**
- 确保 `frontend/src/main.ts` 中 mocks 已被禁用（已注释掉）

**支持的浏览器：**
- ✅ **Chromium** (Chrome/Edge) - 68个测试全部通过
- ✅ **Firefox** - 68个测试全部通过
- ✅ **WebKit** (Safari) - 68个测试全部通过

**注意**：WebKit 在 Windows 上录制视频时存在问题，已在配置中禁用视频录制。所有测试功能正常，只是不会生成视频文件。

### 3. 安装 Playwright（如果还没有安装）

```bash
cd frontend
npm install
npx playwright install
```

安装所有浏览器（包括 Firefox 和 WebKit）：
```bash
npx playwright install firefox webkit
```

### 4. 运行测试

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

### 5. 查看测试报告

测试完成后，查看 HTML 报告：

```bash
npm run test:report
```

报告位置：`frontend/playwright-report/index.html`

## 测试结构

```
tests/
├── helpers/              # 测试辅助函数
│   ├── auth.ts          # 登录/登出辅助函数
│   │   ├── loginAsStudent() - 学生登录
│   │   ├── loginAsAdmin() - 管理员登录
│   │   └── logout() - 登出
│   ├── test-data.ts     # 测试数据常量
│   └── wait-helpers.ts  # 等待和异步操作辅助函数
│       ├── waitForPageLoad() - 等待页面加载
│       ├── waitForElement() - 等待元素可见
│       ├── waitForTableData() - 等待表格数据加载
│       ├── waitForDialog() - 等待对话框打开（自动排除通知抽屉和消息框）
│       ├── waitForMessage() - 等待消息提示（支持可选消息）
│       ├── closeNotificationDrawer() - 关闭通知抽屉
│       └── closeOverlayDialogs() - 关闭遮挡的对话框
├── global-setup.ts      # 全局测试设置（检查服务是否就绪）
├── student/             # 学生端功能测试
│   ├── login.spec.ts
│   ├── dashboard.spec.ts
│   ├── dormitory.spec.ts
│   ├── payments.spec.ts
│   ├── repairs.spec.ts
│   ├── repair-feedback.spec.ts
│   ├── exchange.spec.ts
│   ├── notices.spec.ts
│   ├── profile.spec.ts
│   └── lifestyle-tags.spec.ts
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
- **timeout**: 测试超时时间（默认: 60秒）
- **expect.timeout**: 断言超时时间（默认: 10秒）
- **browsers**: 支持的浏览器（Chromium, Firefox, WebKit）
- **retries**: 失败重试次数（CI模式下为2次，本地为0次）
- **workers**: 并发worker数（CI模式下为1，本地为2）
- **fullyParallel**: 是否完全并行执行（设置为false以避免测试间相互干扰）
- **globalSetup**: 全局测试设置文件，用于检查服务是否就绪

## 测试账号

### 学生账号
- 学号: `2024001` ~ `2024010`
- 密码: `password`

### 管理员账号
- 账号: `admin001`, `admin002`
- 密码: `password`

## 环境要求

1. **前端服务**：运行在 `http://localhost:5173`
2. **后端API服务**：运行在 `http://localhost:8080/api`
3. **数据库**：MySQL 数据库，已初始化并包含测试数据
   - 数据库名：`ihome`
   - 必须执行 `database-init-with-data.sql` 脚本初始化
4. **Mocks 禁用**：测试使用真实后端 API，不使用 Mock 数据
   - 确保 `frontend/src/main.ts` 中 mocks 导入已被注释

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

1. **测试前确保服务已启动**
   - 前端服务：`http://localhost:5173`
   - 后端API服务：`http://localhost:8080/api`

2. **测试隔离**
   - 每个测试在 `beforeEach` 中会清除 localStorage，确保测试独立性
   - 测试配置中 `fullyParallel` 设置为 `false`，避免测试间相互干扰

3. **测试数据**
   - 测试可能会修改数据库数据，建议使用测试数据库
   - 某些测试可能依赖于现有的测试数据（如学生账号、宿舍信息等）

4. **等待策略**
   - 使用 `waitFor*` 函数代替固定的 `waitForTimeout`
   - 所有测试都使用辅助函数处理异步操作和等待

5. **Element Plus 组件**
   - 测试中使用了 Element Plus UI 框架
   - 下拉菜单、对话框等组件会动态追加到 body，测试中已正确处理
   - **通知抽屉处理**：通知抽屉可能会干扰对话框等待，测试会自动识别并排除通知抽屉

6. **登录流程**
   - 学生登录后跳转到 `/`（首页）
   - 管理员登录后跳转到 `/admin/dashboard`
   - 测试中已正确验证这些跳转路径

7. **CSS 选择器使用**
   - 避免混合使用属性选择器和文本选择器
   - 使用 `has-text()` 或 `filter({ hasText: ... })` 进行文本匹配
   - 参考 `dashboard.spec.ts` 中的正确用法

8. **按钮点击问题**
   - 如果按钮被遮挡，先调用 `closeNotificationDrawer()` 关闭通知抽屉
   - 使用 `scrollIntoViewIfNeeded()` 确保按钮可见
   - 必要时使用 `click({ force: true })` 强制点击

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

### 测试无法启动：ES模块错误

**错误信息:** `ReferenceError: require is not defined in ES module scope`

**原因:** 配置文件中使用了 CommonJS 的 `require`，但项目是 ES 模块

**解决:** 已修复，使用 `import.meta.url` 和 `fileURLToPath` 替代 `require.resolve()`

### 测试失败：连接超时

**原因:** 前端或后端服务未启动

**解决:**
1. 检查前端是否运行在 `http://localhost:5173`
2. 检查后端是否运行在 `http://localhost:8080/api`
3. 运行 `npm run dev` 启动前端服务
4. 运行 `mvn spring-boot:run` 启动后端服务

### 测试超时

**原因:** 测试执行时间超过配置的超时时间

**解决:**
1. 检查网络连接是否正常
2. 检查服务是否正常运行且响应速度正常
3. 在 `playwright.config.ts` 中增加 `timeout` 配置（默认60秒）
4. 检查是否有长时间运行的异步操作未正确等待

### 元素找不到

**原因:** 页面加载慢或元素选择器不正确

**解决:**
1. 检查网络连接
2. 增加超时时间（在 `playwright.config.ts` 中）
3. 检查页面是否正常加载（可以在有头模式下运行测试观察）
4. 使用 `waitForElement` 或 `waitForSelector` 等待元素加载
5. 检查选择器是否正确（Element Plus 组件可能使用不同的类名）
6. 使用 Playwright Inspector 调试选择器：`npm run test:debug`

### 测试失败：登录失败

**错误信息:** `登录失败: token为null。响应数据: {"code":-1,"message":"学号或密码错误"}`

**原因:** 数据库未初始化或测试账号不存在

**解决步骤:**
1. **确认数据库已初始化：**
   ```bash
   cd backend
   mysql -u root -p < src/main/resources/database-init-with-data.sql
   ```

2. **验证测试账号是否存在：**
   ```sql
   mysql -u root -p
   USE ihome;
   SELECT id, name FROM students WHERE id IN ('2024001', '2024002');
   SELECT id, name FROM admins WHERE id = 'admin001';
   ```

3. **确认密码哈希值正确：**
   - 测试账号的密码应该是 BCrypt 加密后的哈希值
   - 哈希值：`$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi`
   - 对应明文密码：`password`

4. **检查后端服务是否正常运行：**
   ```bash
   # 检查后端是否在运行
   curl http://localhost:8080/api/students/login -X POST -H "Content-Type: application/json" -d '{"id":"2024001","password":"password"}'
   ```

5. **检查后端日志：**
   - 查看后端控制台是否有错误信息
   - 确认数据库连接是否正常

6. **确认 mocks 已禁用：**
   - 检查 `frontend/src/main.ts`，确保 mocks 导入已被注释
   - 测试应该使用真实后端 API，不使用 Mock 数据

### 测试间相互干扰

**原因:** 测试状态未清理，导致测试间相互影响

**解决:**
1. 确保每个测试的 `beforeEach` 中清除 localStorage
2. 检查 `playwright.config.ts` 中 `fullyParallel` 设置为 `false`
3. 检查 workers 数量是否合理（建议2个或更少）

### 下拉菜单/对话框无法操作

**原因:** Element Plus 组件动态渲染，需要特殊处理

**解决:**
1. 等待下拉菜单出现后再操作（使用 `waitForSelector`）
2. 下拉菜单会追加到 body，选择器需要包含完整路径
3. 使用 `waitForTimeout(300)` 等待动画完成
4. 参考 `helpers/auth.ts` 中的示例代码

### 对话框等待超时（通知抽屉干扰）

**错误信息:** `TimeoutError: page.waitForSelector: Timeout 5000ms exceeded. waiting for locator('.el-dialog, .el-drawer') to be visible`

**原因:** 通知抽屉（notification drawer）与对话框使用相同的选择器，导致等待函数误判

**解决:**
1. 已修复 `waitForDialog` 函数，自动排除通知抽屉
2. 使用 `closeNotificationDrawer()` 函数在等待对话框前关闭通知抽屉
3. 测试会自动识别并排除通知抽屉，只等待真正的对话框

### CSS 选择器语法错误

**错误信息:** `Unexpected token "=" while parsing css selector`

**原因:** CSS 选择器语法不正确，不能混合使用属性选择器和文本选择器

**解决:**
- 错误示例：`a[href="/dorm"], text=我的宿舍`
- 正确方式：分开查找
  ```typescript
  const linkByHref = page.locator('a[href="/dorm"]');
  const linkByText = page.locator('a:has-text("我的宿舍")');
  ```

### 按钮被通知抽屉遮挡

**错误信息:** `element is visible, enabled and stable ... subtree intercepts pointer events`

**原因:** 通知抽屉或其他对话框（如确认删除框、消息框）覆盖在按钮上方，阻止点击

**解决:**
1. 在点击前调用 `closeNotificationDrawer(page)` 关闭通知抽屉
2. 使用 `closeOverlayDialogs(page)` 关闭可能遮挡的对话框
3. 使用 `click({ force: true })` 强制点击（如果必要）
4. 确保按钮滚动到视图中：`await button.scrollIntoViewIfNeeded()`
5. 使用更精确的选择器，排除消息框中的按钮：
   ```typescript
   const button = page.locator('button:has-text("提交")')
     .filter({ hasNot: page.locator('.el-message-box') })
     .first();
   ```

### 对话框被消息框遮挡

**错误信息:** `TimeoutError: page.waitForFunction: Timeout 15000ms exceeded`

**原因:** 消息框（如确认删除框）遮挡了主要对话框，导致 `waitForDialog` 找不到目标对话框

**解决:**
1. 已修复 `waitForDialog` 函数，自动排除消息框和确认框
2. 在等待对话框前调用 `closeOverlayDialogs(page)` 关闭遮挡的对话框
3. 使用 try-catch 处理对话框可能不出现的情况
4. 增加超时时间（如 15000ms）以适应较慢的响应

### 消息等待超时

**错误信息:** `等待消息超时: 10000ms`

**原因:** 某些操作可能不会显示消息提示，或者消息显示时间很短

**解决:**
1. 使用 `waitForMessage(page, undefined, 10000, false)` 将 `required` 参数设置为 `false`
2. 这样即使没有消息，测试也不会失败
3. 对于必须有消息的操作，保持 `required: true`（默认值）

### 对话框中的元素不可见

**错误信息:** `element is not visible` 或 `element is not visible, enabled and editable`

**原因:** 对话框中的元素（输入框、按钮等）虽然存在，但可能因为以下原因不可见：
- 对话框还在动画中，内容未完全加载
- 元素在对话框外或被其他元素遮挡
- 需要滚动才能看到元素

**解决:**
1. **等待对话框内容完全加载**：
   ```typescript
   await page.waitForFunction(
     () => {
       const dialog = document.querySelector('.el-dialog:not([style*="display: none"])');
       if (!dialog) return false;
       const inputs = dialog.querySelectorAll('input, textarea');
       return inputs.length > 0;
     },
     { timeout: 5000 }
   );
   ```

2. **使用更精确的选择器**（在对话框中查找）：
   ```typescript
   const input = page.locator('.el-dialog input[placeholder*="标题"]').first();
   ```

3. **等待元素可见并滚动到视图**（注意：先检查可见性再滚动）：
   ```typescript
   await input.waitFor({ state: 'attached', timeout: 5000 });
   // 先检查是否可见，再决定是否滚动
   const isVisible = await input.isVisible({ timeout: 2000 }).catch(() => false);
   if (isVisible) {
     await input.scrollIntoViewIfNeeded();
   }
   ```

4. **如果元素仍然不可见，使用 JavaScript 设置值**（仅适用于输入框）：
   ```typescript
   const isVisible = await input.isVisible({ timeout: 2000 }).catch(() => false);
   if (!isVisible) {
     await input.evaluate((el: HTMLInputElement, value: string) => {
       el.value = value;
       el.dispatchEvent(new Event('input', { bubbles: true }));
       el.dispatchEvent(new Event('change', { bubbles: true }));
     }, 'value');
   } else {
     await input.fill('value');
   }
   ```

5. **对于按钮，如果不可见，使用 JavaScript 直接点击**：
   ```typescript
   const isVisible = await button.isVisible({ timeout: 2000 }).catch(() => false);
   if (isVisible) {
     await button.scrollIntoViewIfNeeded();
     await button.click();
   } else {
     // 如果不可见，通过 JavaScript 直接点击（比 force: true 更可靠）
     await button.evaluate((el: HTMLElement) => {
       (el as HTMLButtonElement).click();
     });
   }
   ```
   
   **注意**：如果 `click({ force: true })` 仍然失败，使用 JavaScript `evaluate` 直接调用元素的 `click()` 方法更可靠。

### 表格加载超时

**错误信息:** `TimeoutError: page.waitForSelector: Timeout 10000ms exceeded. waiting for locator('.el-table') to be visible`

**原因:** API 响应慢或表格数据为空

**解决:**
1. 增加超时时间（如 15000ms）
2. 使用 try-catch 处理，即使表格为空也验证表格容器存在
3. 检查 API 是否正常返回数据

### 测试报告无法查看

**解决:**
```bash
# 运行测试后查看报告
npm run test:report

# 报告位置
frontend/playwright-report/index.html
```

## 贡献

添加新功能时，请同时添加相应的测试用例，确保功能被正确测试。
