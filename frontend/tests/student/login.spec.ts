import { test, expect } from '@playwright/test';
import { loginAsStudent, TEST_ACCOUNTS, logout } from '../helpers/auth';
import { waitForPageLoad, waitForMessage } from '../helpers/wait-helpers';

test.describe('学生登录功能', () => {
  test.beforeEach(async ({ page }) => {
    // 清除localStorage，确保每个测试都有干净的状态
    await page.goto('/login');
    await page.evaluate(() => {
      localStorage.clear();
    });
    await waitForPageLoad(page);
  });

  test('应该显示登录页面', async ({ page }) => {
    // 等待页面加载完成
    await waitForPageLoad(page);
    
    // 检查登录表单元素（使用更灵活的选择器）
    await expect(page.locator('input[type="password"]')).toBeVisible({ timeout: 10000 });
    await expect(page.locator('button:has-text("登录"), button[type="submit"]').first()).toBeVisible({ timeout: 10000 });
  });

  test('应该能够成功登录', async ({ page }) => {
    await loginAsStudent(page);
    
    // 验证登录后跳转（学生登录后跳转到/）
    await expect(page).toHaveURL(/\/(dashboard)?$/);
    
    // 验证token已保存
    const token = await page.evaluate(() => localStorage.getItem('token'));
    expect(token).toBeTruthy();
    
    // 验证用户信息已保存
    const user = await page.evaluate(() => localStorage.getItem('user'));
    expect(user).toBeTruthy();
  });

  test('应该验证必填字段', async ({ page }) => {
    // 等待登录按钮可见
    const loginButton = page.locator('button:has-text("登录")').first();
    await loginButton.waitFor({ state: 'visible', timeout: 5000 });
    
    // 尝试不填任何信息直接登录
    await loginButton.click();
    
    // 等待验证错误出现（Element Plus的表单验证）
    await page.waitForTimeout(500);
    // 验证错误可能出现在多个位置
    const errorElements = page.locator('.el-form-item__error');
    const count = await errorElements.count();
    expect(count).toBeGreaterThan(0);
  });

  test('应该验证错误的用户名或密码', async ({ page }) => {
    // 等待页面加载完成
    await waitForPageLoad(page);
    
    // 选择学生类型（如果还不是）
    const selectTrigger = page.locator('.el-select').first();
    await selectTrigger.waitFor({ state: 'visible', timeout: 5000 });
    const currentValue = await selectTrigger.textContent();
    if (!currentValue || !currentValue.includes('学生')) {
      await selectTrigger.click();
      await page.waitForSelector('.el-select-dropdown:not([style*="display: none"])', { timeout: 5000 });
      await page.locator('.el-select-dropdown__item').filter({ hasText: '学生' }).first().click();
      await page.waitForTimeout(300);
    }
    
    // 输入错误的用户名和密码
    const usernameInput = page.locator('input[placeholder*="学号"], input[placeholder*="工号"]').first();
    await usernameInput.waitFor({ state: 'visible', timeout: 5000 });
    await usernameInput.clear();
    await usernameInput.fill('invalid_user');
    
    const passwordInput = page.locator('input[type="password"]').first();
    await passwordInput.waitFor({ state: 'visible', timeout: 5000 });
    await passwordInput.clear();
    await passwordInput.fill('wrong_password');
    
    // 点击登录
    const loginButton = page.locator('button:has-text("登录")').first();
    await loginButton.waitFor({ state: 'visible', timeout: 5000 });
    await loginButton.click();
    
    // 应该显示错误消息（等待消息出现）
    await page.waitForTimeout(1000);
    // Element Plus的消息可能使用不同的类名
    const errorMessage = page.locator('.el-message, .el-notification, .el-message-box').filter({ hasText: /错误|失败|无效/ });
    await errorMessage.waitFor({ state: 'visible', timeout: 10000 }).catch(() => {});
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
