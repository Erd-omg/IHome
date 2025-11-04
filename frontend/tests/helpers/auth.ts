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
  
  // 选择用户类型为学生
  await page.locator('.el-select').first().click();
  await page.locator('.el-select-dropdown__item').filter({ hasText: '学生' }).click();
  
  // 输入用户名
  await page.locator('input[placeholder="请输入学号/工号"]').fill(studentId);
  
  // 输入密码
  await page.locator('input[type="password"]').fill(password);
  
  // 点击登录按钮
  await page.locator('button:has-text("登录")').click();
  
  // 等待登录成功（检查是否跳转到首页或仪表板）
  await page.waitForURL(/^\/(dashboard|$)/, { timeout: 10000 });
  
  // 验证登录状态
  const token = await page.evaluate(() => localStorage.getItem('token'));
  expect(token).toBeTruthy();
}

/**
 * 登录管理员账号
 */
export async function loginAsAdmin(page: Page, adminId: string = TEST_ACCOUNTS.admin.id, password: string = TEST_ACCOUNTS.admin.password) {
  await page.goto('/login');
  
  // 选择用户类型为管理员
  await page.locator('.el-select').first().click();
  await page.locator('.el-select-dropdown__item').filter({ hasText: '管理员' }).click();
  
  // 输入用户名
  await page.locator('input[placeholder="请输入学号/工号"]').fill(adminId);
  
  // 输入密码
  await page.locator('input[type="password"]').fill(password);
  
  // 点击登录按钮
  await page.locator('button:has-text("登录")').click();
  
  // 等待登录成功（检查是否跳转到管理员仪表板）
  await page.waitForURL(/^\/admin\/dashboard/, { timeout: 10000 });
  
  // 验证登录状态
  const token = await page.evaluate(() => localStorage.getItem('token'));
  expect(token).toBeTruthy();
}

/**
 * 登出
 */
export async function logout(page: Page) {
  // 根据实际情况查找登出按钮（可能在导航栏或下拉菜单中）
  const logoutButton = page.locator('text=登出').or(page.locator('text=退出')).or(page.locator('[aria-label="登出"]'));
  
  if (await logoutButton.count() > 0) {
    await logoutButton.click();
  } else {
    // 如果找不到登出按钮，直接清除localStorage并跳转
    await page.evaluate(() => {
      localStorage.clear();
    });
    await page.goto('/login');
  }
  
  // 验证已登出
  await page.waitForURL(/\/login/);
}
