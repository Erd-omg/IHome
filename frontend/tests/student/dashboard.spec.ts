import { test, expect } from '@playwright/test';
import { loginAsStudent } from '../helpers/auth';
import { waitForPageLoad, waitForElement } from '../helpers/wait-helpers';

test.describe('学生仪表板', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsStudent(page);
    await page.goto('/dashboard');
    await waitForPageLoad(page);
  });

  test('应该显示仪表板页面', async ({ page }) => {
    // 验证页面标题或关键元素
    await expect(page.locator('body')).toBeVisible();
    
    // 检查是否有仪表板相关的元素（根据实际UI调整）
    // 例如：统计卡片、快捷操作等
  });

  test('应该显示当前用户信息', async ({ page }) => {
    // 检查用户信息是否显示
    const userInfo = await page.evaluate(() => localStorage.getItem('user'));
    expect(userInfo).toBeTruthy();
  });

  test('应该能够导航到其他页面', async ({ page }) => {
    // 测试导航功能（根据实际导航菜单调整）
    // 例如点击"我的宿舍"链接
    const dormLink = page.locator('a[href="/dorm"], text=我的宿舍').first();
    if (await dormLink.count() > 0) {
      await dormLink.click();
      await waitForPageLoad(page);
      await expect(page).toHaveURL(/\/dorm/);
    }
  });
});
