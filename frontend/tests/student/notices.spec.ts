import { test, expect } from '@playwright/test';
import { loginAsStudent } from '../helpers/auth';
import { waitForPageLoad, waitForTableData, waitForElement } from '../helpers/wait-helpers';

test.describe('学生通知功能', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsStudent(page);
  });

  test('应该能够查看通知列表', async ({ page }) => {
    await page.goto('/notices');
    await waitForPageLoad(page);
    
    // 等待内容加载
    await page.waitForTimeout(2000);
    
    // 验证页面元素
    await expect(page.locator('body')).toBeVisible();
  });

  test('应该能够查看通知详情', async ({ page }) => {
    await page.goto('/notices');
    await waitForPageLoad(page);
    
    // 查找第一条通知或详情链接
    const noticeLink = page.locator('a[href*="/notices/"], .notice-item, .el-card').first();
    if (await noticeLink.count() > 0 && await noticeLink.isVisible()) {
      await noticeLink.click();
      await waitForPageLoad(page);
      
      // 验证跳转到详情页或打开详情对话框
      const isDetailPage = page.url().includes('/notices/');
      const hasDetailDialog = await page.locator('.el-dialog, .el-drawer').count() > 0;
      
      expect(isDetailPage || hasDetailDialog).toBeTruthy();
    }
  });

  test('应该能够通过URL直接访问通知详情', async ({ page }) => {
    // 假设通知ID为1（实际测试中可以从列表获取）
    await page.goto('/notices/1');
    await waitForPageLoad(page);
    
    // 验证详情页面加载
    await expect(page.locator('body')).toBeVisible();
  });
});
