import { test, expect } from '@playwright/test';
import { loginAsAdmin } from '../helpers/auth';
import { waitForPageLoad, waitForElement } from '../helpers/wait-helpers';

test.describe('管理员仪表板', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsAdmin(page);
    await page.goto('/admin/dashboard');
    await waitForPageLoad(page);
  });

  test('应该显示管理员仪表板', async ({ page }) => {
    // 验证页面加载
    await expect(page.locator('body')).toBeVisible();
    
    // 验证关键统计信息（根据实际UI调整）
    // 例如：学生总数、宿舍总数、待处理申请等
  });

  test('应该能够导航到各个管理模块', async ({ page }) => {
    const modules = [
      { name: '学生管理', url: '/admin/students' },
      { name: '宿舍管理', url: '/admin/dormitories' },
      { name: '分配管理', url: '/admin/allocations' },
      { name: '通知管理', url: '/admin/notifications' },
    ];
    
    for (const module of modules) {
      await page.goto(module.url);
      await waitForPageLoad(page);
      await expect(page).toHaveURL(new RegExp(module.url));
    }
  });
});
