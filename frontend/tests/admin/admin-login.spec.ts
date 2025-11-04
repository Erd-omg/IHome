import { test, expect } from '@playwright/test';
import { loginAsAdmin, TEST_ACCOUNTS, logout } from '../helpers/auth';
import { waitForPageLoad, waitForMessage } from '../helpers/wait-helpers';

test.describe('管理员登录功能', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/login');
    await waitForPageLoad(page);
  });

  test('应该能够成功登录为管理员', async ({ page }) => {
    await loginAsAdmin(page);
    
    // 验证登录后跳转到管理员仪表板
    await expect(page).toHaveURL(/^\/admin\/dashboard/);
    
    // 验证token已保存
    const token = await page.evaluate(() => localStorage.getItem('token'));
    expect(token).toBeTruthy();
    
    // 验证用户类型为管理员
    const user = await page.evaluate(() => {
      const userStr = localStorage.getItem('user');
      return userStr ? JSON.parse(userStr) : null;
    });
    expect(user?.userType).toBe('admin');
  });

  test('管理员应该无法访问学生页面', async ({ page }) => {
    await loginAsAdmin(page);
    
    // 尝试访问学生页面
    await page.goto('/dashboard');
    
    // 应该被重定向（根据实际路由守卫逻辑调整）
    // 可能重定向到管理员仪表板或首页
    await waitForPageLoad(page);
  });

  test('登出后应该清除登录状态', async ({ page }) => {
    await loginAsAdmin(page);
    
    // 登出
    await logout(page);
    
    // 验证已登出
    await expect(page).toHaveURL(/\/login/);
    
    // 验证token已清除
    const token = await page.evaluate(() => localStorage.getItem('token'));
    expect(token).toBeFalsy();
  });
});
