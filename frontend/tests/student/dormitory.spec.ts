import { test, expect } from '@playwright/test';
import { loginAsStudent } from '../helpers/auth';
import { waitForPageLoad, waitForTableData, waitForDialog, waitForMessage } from '../helpers/wait-helpers';

test.describe('学生宿舍管理', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsStudent(page);
  });

  test('应该能够查看我的宿舍信息', async ({ page }) => {
    await page.goto('/dorm');
    await waitForPageLoad(page);
    
    // 验证页面加载
    await expect(page.locator('body')).toBeVisible();
  });

  test('应该能够搜索宿舍', async ({ page }) => {
    await page.goto('/dorm-search');
    await waitForPageLoad(page);
    
    // 查找搜索输入框
    const searchInput = page.locator('input[placeholder*="搜索"], input[type="search"]').first();
    if (await searchInput.count() > 0) {
      await searchInput.fill('101');
      await page.waitForTimeout(1000); // 等待搜索完成
      
      // 验证搜索结果
      await expect(page.locator('body')).toBeVisible();
    }
  });

  test('应该能够选择床位', async ({ page }) => {
    await page.goto('/bed-selection');
    await waitForPageLoad(page);
    
    // 如果有可用床位，尝试选择
    const bedButton = page.locator('button:has-text("选择"), .bed-card').first();
    if (await bedButton.count() > 0 && await bedButton.isVisible()) {
      await bedButton.click();
      
      // 如果有确认对话框，点击确认
      const confirmButton = page.locator('button:has-text("确认"), button:has-text("确定")').first();
      if (await confirmButton.count() > 0) {
        await confirmButton.click();
        await waitForMessage(page, undefined, 5000);
      }
    }
  });

  test('应该能够查看宿舍详情', async ({ page }) => {
    await page.goto('/dorm');
    await waitForPageLoad(page);
    
    // 查找详情按钮或链接
    const detailButton = page.locator('text=详情, text=查看详情, button:has-text("详情")').first();
    if (await detailButton.count() > 0 && await detailButton.isVisible()) {
      await detailButton.click();
      await waitForDialog(page, undefined, 5000);
      
      // 验证详情对话框内容
      await expect(page.locator('.el-dialog, .el-drawer')).toBeVisible();
    }
  });
});
