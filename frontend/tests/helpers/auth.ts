import { Page, expect } from '@playwright/test';

/**
 * 测试账号配置
 */
export const TEST_ACCOUNTS = {
  student: {
    id: '2024001',
    password: 'password',
    name: '张三'
  },
  admin: {
    id: 'admin001',
    password: 'password',
    name: '系统管理员'
  }
};

/**
 * 登录学生账号
 */
export async function loginAsStudent(page: Page, studentId: string = TEST_ACCOUNTS.student.id, password: string = TEST_ACCOUNTS.student.password) {
  await page.goto('/login');
  
  // 等待页面加载完成
  await page.waitForLoadState('networkidle', { timeout: 15000 }).catch(() => {});
  
  // 等待登录表单出现
  await page.waitForSelector('input[type="password"]', { timeout: 10000 });
  
  // 等待用户类型选择器出现
  const selectTrigger = page.locator('.el-select').first();
  await selectTrigger.waitFor({ state: 'visible', timeout: 10000 });
  
  // 检查当前是否已经是学生类型（默认值）
  const currentValue = await selectTrigger.textContent();
  if (!currentValue || !currentValue.includes('学生')) {
    // 点击选择器打开下拉菜单
  await selectTrigger.click();
  
    // 等待下拉菜单出现（Element Plus的下拉菜单会追加到body）
    await page.waitForSelector('.el-select-dropdown:not([style*="display: none"])', { timeout: 5000 });
    
    // 选择学生选项
  const studentOption = page.locator('.el-select-dropdown__item').filter({ hasText: '学生' }).first();
  await studentOption.waitFor({ state: 'visible', timeout: 5000 });
  await studentOption.click();
  
    // 等待下拉菜单关闭
  await page.waitForTimeout(300);
  }
  
  // 输入用户名
  const usernameInput = page.locator('input[placeholder*="学号"], input[placeholder*="工号"]').first();
  await usernameInput.waitFor({ state: 'visible', timeout: 5000 });
  await usernameInput.clear();
  await usernameInput.fill(studentId);
  // 触发blur事件以确保验证触发
  await usernameInput.blur();
  await page.waitForTimeout(300);
  
  // 输入密码
  const passwordInput = page.locator('input[type="password"]').first();
  await passwordInput.waitFor({ state: 'visible', timeout: 5000 });
  await passwordInput.clear();
  await passwordInput.fill(password);
  // 触发blur事件以确保验证触发
  await passwordInput.blur();
  await page.waitForTimeout(300);
  
  // 确保表单字段都已填充
  const usernameValue = await usernameInput.inputValue();
  const passwordValue = await passwordInput.inputValue();
  if (!usernameValue || !passwordValue) {
    throw new Error(`表单字段未正确填充: username=${usernameValue}, password=${passwordValue ? '***' : ''}`);
  }
  
  // 点击登录按钮
  const loginButton = page.locator('button:has-text("登录")').first();
  await loginButton.waitFor({ state: 'visible', timeout: 5000 });
  await loginButton.waitFor({ state: 'attached', timeout: 5000 });
  
  // 确保按钮未被禁用（如果表单验证失败，按钮可能被禁用）
  const isDisabled = await loginButton.isDisabled();
  if (isDisabled) {
    // 检查是否有验证错误
    const validationErrors = await page.locator('.el-form-item__error').allTextContents();
    throw new Error(`登录按钮被禁用，可能是表单验证失败: ${validationErrors.join(', ')}`);
  }
  
  // 等待导航开始（点击登录按钮）
  await loginButton.click();
  
  // 等待登录API完成（等待网络请求）
  let responseReceived = false;
  let responseData: any = null;
  let responseStatus: number | null = null;
  try {
    const response = await page.waitForResponse(
      (response) => {
        const url = response.url();
        const isLoginUrl = url.includes('/students/login') || url.includes('/admin/login');
        if (isLoginUrl) {
          responseStatus = response.status();
          if (response.status() === 200) {
            responseReceived = true;
            return true;
          }
        }
        return false;
      },
      { timeout: 20000 }
    );
    // 等待响应体完成加载并保存响应数据
    try {
      responseData = await response.json();
    } catch (e) {
      // 如果解析失败，尝试获取文本
      try {
        responseData = await response.text();
      } catch (e2) {
        // 如果都失败，继续
      }
    }
  } catch (e) {
    // 如果API请求已完成，继续
  }
  
  // 等待Vue处理响应（给store时间处理响应）
  await page.waitForTimeout(500);
  
  // 检查是否有错误消息（如果登录失败会有错误提示）
  try {
    const errorMessage = await page.locator('.el-message--error').textContent({ timeout: 2000 }).catch(() => null);
    if (errorMessage && !errorMessage.includes('登录成功')) {
      // 如果有错误消息且不是成功消息，说明登录失败
      throw new Error(`登录失败: ${errorMessage}. 响应状态: ${responseStatus}, 响应数据: ${JSON.stringify(responseData)}`);
    }
  } catch (e: any) {
    // 如果页面已关闭，抛出错误
    if (e.message && (e.message.includes('closed') || e.message.includes('Target page'))) {
      throw new Error(`登录过程中页面被关闭: ${e.message}`);
    }
    // 其他错误继续
  }
  
  // 等待Vue store完成登录操作（等待token保存到localStorage）
  // 同时等待页面导航（登录成功会导航），使用Promise.race提高效率
  let token = null;
  try {
    // 等待页面导航或token出现（最多8秒）
    await Promise.race([
      // 选项1：等待token出现
      page.waitForFunction(
        () => localStorage.getItem('token') !== null,
        { timeout: 8000 }
      ).then(() => 'token'),
      // 选项2：等待页面导航（登录成功会导航）
      page.waitForURL(/^\/$|^\/dashboard/, { timeout: 8000 }).then(() => 'navigation')
    ]).catch(() => {
      // 如果两个都失败，继续检查
    });
    
    // 检查token
    token = await page.evaluate(() => localStorage.getItem('token'));
  } catch (e: any) {
    // 如果页面已关闭，抛出错误
    if (e.message && (e.message.includes('closed') || e.message.includes('Target page'))) {
      throw new Error(`登录过程中页面被关闭: ${e.message}`);
    }
    // 其他错误，尝试获取token
    try {
      token = await page.evaluate(() => localStorage.getItem('token'));
    } catch (e2: any) {
      if (e2.message && (e2.message.includes('closed') || e2.message.includes('Target page'))) {
        throw new Error(`登录过程中页面被关闭: ${e2.message}`);
      }
    }
  }
  
  // 如果token仍然为null，再等待一下导航（最多3秒）
  if (!token) {
    try {
      await page.waitForURL(/^\/$|^\/dashboard/, { timeout: 3000 }).catch(() => {});
      token = await page.evaluate(() => localStorage.getItem('token'));
    } catch (e: any) {
      if (e.message && (e.message.includes('closed') || e.message.includes('Target page'))) {
        throw new Error(`登录过程中页面被关闭: ${e.message}`);
      }
    }
  }
  
  // 最终验证token
  if (!token) {
    // 获取页面URL、错误信息和响应数据用于调试
    const currentUrl = page.url();
    const localStorageData = await page.evaluate(() => {
      return {
        token: localStorage.getItem('token'),
        user: localStorage.getItem('user'),
        refreshToken: localStorage.getItem('refreshToken')
      };
    });
    const consoleLogs = await page.evaluate(() => {
      // 尝试获取console错误（如果有）
      return '无法获取console日志';
    });
    throw new Error(`登录失败: token为null。当前URL: ${currentUrl}, 响应接收: ${responseReceived}, 响应状态: ${responseStatus}, 响应数据: ${JSON.stringify(responseData)}, localStorage: ${JSON.stringify(localStorageData)}`);
  }
  
  expect(token).toBeTruthy();
}

/**
 * 登录管理员账号
 */
export async function loginAsAdmin(page: Page, adminId: string = TEST_ACCOUNTS.admin.id, password: string = TEST_ACCOUNTS.admin.password) {
  await page.goto('/login');
  
  // 等待页面加载完成
  await page.waitForLoadState('networkidle', { timeout: 15000 }).catch(() => {});
  
  // 等待登录表单出现
  await page.waitForSelector('input[type="password"]', { timeout: 10000 });
  
  // 等待用户类型选择器出现
  const selectTrigger = page.locator('.el-select').first();
  await selectTrigger.waitFor({ state: 'visible', timeout: 10000 });
  
  // 点击选择器打开下拉菜单
  await selectTrigger.click();
  
  // 等待下拉菜单出现
  await page.waitForSelector('.el-select-dropdown:not([style*="display: none"])', { timeout: 5000 });
  
  // 选择管理员选项
  const adminOption = page.locator('.el-select-dropdown__item').filter({ hasText: '管理员' }).first();
  await adminOption.waitFor({ state: 'visible', timeout: 5000 });
  await adminOption.click();
  
  // 等待下拉菜单关闭
  await page.waitForTimeout(300);
  
  // 输入用户名
  const usernameInput = page.locator('input[placeholder*="学号"], input[placeholder*="工号"]').first();
  await usernameInput.waitFor({ state: 'visible', timeout: 5000 });
  await usernameInput.clear();
  await usernameInput.fill(adminId);
  // 触发blur事件以确保验证触发
  await usernameInput.blur();
  await page.waitForTimeout(300);
  
  // 输入密码
  const passwordInput = page.locator('input[type="password"]').first();
  await passwordInput.waitFor({ state: 'visible', timeout: 5000 });
  await passwordInput.clear();
  await passwordInput.fill(password);
  // 触发blur事件以确保验证触发
  await passwordInput.blur();
  await page.waitForTimeout(300);
  
  // 确保表单字段都已填充
  const usernameValue = await usernameInput.inputValue();
  const passwordValue = await passwordInput.inputValue();
  if (!usernameValue || !passwordValue) {
    throw new Error(`表单字段未正确填充: username=${usernameValue}, password=${passwordValue ? '***' : ''}`);
  }
  
  // 点击登录按钮
  const loginButton = page.locator('button:has-text("登录")').first();
  await loginButton.waitFor({ state: 'visible', timeout: 5000 });
  await loginButton.waitFor({ state: 'attached', timeout: 5000 });
  
  // 确保按钮未被禁用（如果表单验证失败，按钮可能被禁用）
  const isDisabled = await loginButton.isDisabled();
  if (isDisabled) {
    // 检查是否有验证错误
    const validationErrors = await page.locator('.el-form-item__error').allTextContents();
    throw new Error(`登录按钮被禁用，可能是表单验证失败: ${validationErrors.join(', ')}`);
  }
  
  // 等待导航开始（点击登录按钮）
  await loginButton.click();
  
  // 等待登录API完成（等待网络请求）
  let responseReceived = false;
  let responseData: any = null;
  let responseStatus: number | null = null;
  try {
    const response = await page.waitForResponse(
      (response) => {
        const url = response.url();
        const isLoginUrl = url.includes('/admin/login');
        if (isLoginUrl) {
          responseStatus = response.status();
          if (response.status() === 200) {
            responseReceived = true;
            return true;
          }
        }
        return false;
      },
      { timeout: 20000 }
    );
    // 等待响应体完成加载并保存响应数据
    try {
      responseData = await response.json();
    } catch (e) {
      // 如果解析失败，尝试获取文本
      try {
        responseData = await response.text();
      } catch (e2) {
        // 如果都失败，继续
      }
    }
  } catch (e) {
    // 如果API请求已完成，继续
  }
  
  // 等待Vue处理响应（给store时间处理响应）
  await page.waitForTimeout(500);
  
  // 检查是否有错误消息（如果登录失败会有错误提示）
  try {
    const errorMessage = await page.locator('.el-message--error').textContent({ timeout: 2000 }).catch(() => null);
    if (errorMessage && !errorMessage.includes('登录成功')) {
      // 如果有错误消息且不是成功消息，说明登录失败
      throw new Error(`登录失败: ${errorMessage}. 响应状态: ${responseStatus}, 响应数据: ${JSON.stringify(responseData)}`);
    }
  } catch (e: any) {
    // 如果页面已关闭，抛出错误
    if (e.message && (e.message.includes('closed') || e.message.includes('Target page'))) {
      throw new Error(`登录过程中页面被关闭: ${e.message}`);
    }
    // 其他错误继续
  }
  
  // 等待Vue store完成登录操作（等待token保存到localStorage）
  // 同时等待页面导航（登录成功会导航），使用Promise.race提高效率
  let token = null;
  try {
    // 等待页面导航或token出现（最多8秒）
    await Promise.race([
      // 选项1：等待token出现
      page.waitForFunction(
        () => localStorage.getItem('token') !== null,
        { timeout: 8000 }
      ).then(() => 'token'),
      // 选项2：等待页面导航（登录成功会导航）
      page.waitForURL(/\/admin\/dashboard/, { timeout: 8000 }).then(() => 'navigation')
    ]).catch(() => {
      // 如果两个都失败，继续检查
    });
    
    // 检查token
    token = await page.evaluate(() => localStorage.getItem('token'));
  } catch (e: any) {
    // 如果页面已关闭，抛出错误
    if (e.message && (e.message.includes('closed') || e.message.includes('Target page'))) {
      throw new Error(`登录过程中页面被关闭: ${e.message}`);
    }
    // 其他错误，尝试获取token
    try {
      token = await page.evaluate(() => localStorage.getItem('token'));
    } catch (e2: any) {
      if (e2.message && (e2.message.includes('closed') || e2.message.includes('Target page'))) {
        throw new Error(`登录过程中页面被关闭: ${e2.message}`);
      }
    }
  }
  
  // 如果token仍然为null，再等待一下导航（最多3秒）
  if (!token) {
    try {
      await page.waitForURL(/\/admin\/dashboard/, { timeout: 3000 }).catch(() => {});
      token = await page.evaluate(() => localStorage.getItem('token'));
    } catch (e: any) {
      if (e.message && (e.message.includes('closed') || e.message.includes('Target page'))) {
        throw new Error(`登录过程中页面被关闭: ${e.message}`);
      }
    }
  }
  
  // 最终验证token
  if (!token) {
    // 获取页面URL、错误信息和响应数据用于调试
    const currentUrl = page.url();
    const localStorageData = await page.evaluate(() => {
      return {
        token: localStorage.getItem('token'),
        user: localStorage.getItem('user'),
        refreshToken: localStorage.getItem('refreshToken')
      };
    });
    throw new Error(`登录失败: token为null。当前URL: ${currentUrl}, 响应接收: ${responseReceived}, 响应状态: ${responseStatus}, 响应数据: ${JSON.stringify(responseData)}, localStorage: ${JSON.stringify(localStorageData)}`);
  }
  
  expect(token).toBeTruthy();
}

/**
 * 登出
 */
export async function logout(page: Page) {
  // 查找用户下拉菜单触发器（包含头像和用户名）
  const userDropdown = page.locator('.user-dropdown').first();
  
  if (await userDropdown.count() > 0) {
    // 点击用户下拉菜单
    await userDropdown.waitFor({ state: 'visible', timeout: 5000 });
    await userDropdown.click();
    
    // 等待下拉菜单出现
    await page.waitForTimeout(300);
    
    // 点击"退出登录"选项
    const logoutOption = page.locator('.el-dropdown-menu__item').filter({ hasText: /退出|登出/ }).first();
    await logoutOption.waitFor({ state: 'visible', timeout: 5000 });
    await logoutOption.click();
    
    // 等待登出完成和路由跳转
    await page.waitForURL(/\/login/, { timeout: 10000 }).catch(() => {});
  } else {
    // 如果找不到登出按钮，直接清除localStorage并跳转
    await page.evaluate(() => {
      localStorage.clear();
    });
    await page.goto('/login');
  }
  
  // 等待页面加载完成
  await page.waitForLoadState('networkidle', { timeout: 10000 }).catch(() => {});
  
  // 验证已登出
  await expect(page).toHaveURL(/\/login/);
  
  // 验证token已清除
  const token = await page.evaluate(() => localStorage.getItem('token'));
  expect(token).toBeFalsy();
}
