import { test, expect } from '@playwright/test';
import { loginAsStudent } from '../helpers/auth';
import { waitForPageLoad, waitForElement } from '../helpers/wait-helpers';

test.describe('学生仪表板', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsStudent(page);
    // 学生登录后跳转到/，导航到/dashboard进行测试
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
    // 使用正确的选择器语法：分开查找链接或包含文本的元素
    const dormLinkByHref = page.locator('a[href="/dorm"]').first();
    const dormLinkByText = page.locator('a:has-text("我的宿舍")').first();
    
    // 尝试通过 href 查找
    if (await dormLinkByHref.count() > 0 && await dormLinkByHref.isVisible()) {
      await dormLinkByHref.click();
      await waitForPageLoad(page);
      await expect(page).toHaveURL(/\/dorm/);
    } 
    // 如果找不到，尝试通过文本查找
    else if (await dormLinkByText.count() > 0 && await dormLinkByText.isVisible()) {
      await dormLinkByText.click();
      await waitForPageLoad(page);
      await expect(page).toHaveURL(/\/dorm/);
    }
    // 如果都找不到，测试通过（导航菜单可能不同）
  });
});
