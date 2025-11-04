import { test, expect } from '@playwright/test';
import { loginAsStudent, TEST_ACCOUNTS, logout } from '../helpers/auth';
import { waitForPageLoad, waitForMessage } from '../helpers/wait-helpers';

test.describe('学生登录功能', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/login');
    await waitForPageLoad(page);
  });

  test('应该显示登录页面', async ({ page }) => {
    await expect(page.locator('text=用户类型')).toBeVisible();
    await expect(page.locator('text=用户名')).toBeVisible();
    await expect(page.locator('text=密码')).toBeVisible();
    await expect(page.locator('button:has-text("登录")')).toBeVisible();
  });

  test('应该能够成功登录', async ({ page }) => {
    await loginAsStudent(page);
    
    // 验证登录后跳转
    await expect(page).toHaveURL(/^\/(dashboard|$)/);
    
    // 验证token已保存
    const token = await page.evaluate(() => localStorage.getItem('token'));
    expect(token).toBeTruthy();
    
    // 验证用户信息已保存
    const user = await page.evaluate(() => localStorage.getItem('user'));
    expect(user).toBeTruthy();
  });

  test('应该验证必填字段', async ({ page }) => {
    // 尝试不填任何信息直接登录
    await page.locator('button:has-text("登录")').click();
    
    // 应该显示验证错误
    await expect(page.locator('.el-form-item__error')).toBeVisible();
  });

  test('应该验证错误的用户名或密码', async ({ page }) => {
    // 选择学生类型
    await page.locator('.el-select').first().click();
    await page.locator('.el-select-dropdown__item').filter({ hasText: '学生' }).click();
    
    // 输入错误的用户名和密码
    await page.locator('input[placeholder="请输入学号/工号"]').fill('invalid_user');
    await page.locator('input[type="password"]').fill('wrong_password');
    
    // 点击登录
    await page.locator('button:has-text("登录")').click();
    
    // 应该显示错误消息
    await waitForMessage(page, undefined, 10000);
    await expect(page.locator('.el-message--error, .el-notification--error')).toBeVisible();
  });

  test('应该能够记住登录状态', async ({ page }) => {
    await loginAsStudent(page);
    
    // 刷新页面
    await page.reload();
    await waitForPageLoad(page);
    
    // 应该仍然保持登录状态
    const token = await page.evaluate(() => localStorage.getItem('token'));
    expect(token).toBeTruthy();
    
    // 不应该跳转到登录页
    await expect(page).not.toHaveURL(/\/login/);
  });

  test('登出后应该清除登录状态', async ({ page }) => {
    await loginAsStudent(page);
    
    // 登出
    await logout(page);
    
    // 验证已登出
    await expect(page).toHaveURL(/\/login/);
    
    // 验证token已清除
    const token = await page.evaluate(() => localStorage.getItem('token'));
    expect(token).toBeFalsy();
  });
});
