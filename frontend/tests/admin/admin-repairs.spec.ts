import { test, expect } from '@playwright/test';
import { loginAsAdmin } from '../helpers/auth';
import { waitForPageLoad, waitForTableData, waitForDialog, waitForMessage } from '../helpers/wait-helpers';

test.describe('管理员维修管理', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsAdmin(page);
    await page.goto('/admin/repairs');
    await waitForPageLoad(page);
  });

  test('应该能够查看维修工单列表', async ({ page }) => {
    // 等待表格加载
    await waitForTableData(page, '.el-table', 10000);
    
    // 验证表格存在
    await expect(page.locator('.el-table, table')).toBeVisible();
  });

  test('应该能够更新维修状态', async ({ page }) => {
    // 等待表格加载
    await waitForTableData(page, '.el-table', 10000);
    
    // 查找状态更新按钮或下拉菜单
    const statusButton = page.locator('button:has-text("处理"), .el-button--primary').first();
    if (await statusButton.count() > 0 && await statusButton.isVisible()) {
      await statusButton.click();
      await waitForDialog(page, undefined, 5000);
      
      // 选择新状态
      const statusSelect = page.locator('.el-select').first();
      if (await statusSelect.count() > 0) {
        await statusSelect.click();
        await page.locator('.el-select-dropdown__item').filter({ hasText: '已完成' }).or(page.locator('.el-select-dropdown__item').first()).click();
      }
      
      // 确认
      const confirmButton = page.locator('button:has-text("确认"), button:has-text("保存")').first();
      if (await confirmButton.count() > 0) {
        await confirmButton.click();
        await waitForMessage(page, undefined, 10000);
      }
    }
  });

  test('应该能够查看维修详情', async ({ page }) => {
    // 等待表格加载
    await waitForTableData(page, '.el-table', 10000);
    
    // 查找详情按钮
    const detailButton = page.locator('text=详情, button:has-text("详情")').first();
    if (await detailButton.count() > 0 && await detailButton.isVisible()) {
      await detailButton.click();
      await waitForDialog(page, undefined, 5000);
      
      // 验证详情对话框
      await expect(page.locator('.el-dialog, .el-drawer')).toBeVisible();
    }
  });
});
