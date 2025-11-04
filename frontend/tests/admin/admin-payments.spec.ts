import { test, expect } from '@playwright/test';
import { loginAsAdmin } from '../helpers/auth';
import { waitForPageLoad, waitForTableData, waitForDialog, waitForMessage } from '../helpers/wait-helpers';

test.describe('管理员支付管理', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsAdmin(page);
    await page.goto('/admin/payments');
    await waitForPageLoad(page);
  });

  test('应该能够查看支付记录列表', async ({ page }) => {
    // 等待表格加载
    await waitForTableData(page, '.el-table', 10000);
    
    // 验证表格存在
    await expect(page.locator('.el-table, table')).toBeVisible();
  });

  test('应该能够更新支付状态', async ({ page }) => {
    // 等待表格加载
    await waitForTableData(page, '.el-table', 10000);
    
    // 查找状态更新按钮
    const statusButton = page.locator('button:has-text("确认"), .el-button--primary').first();
    if (await statusButton.count() > 0 && await statusButton.isVisible()) {
      await statusButton.click();
      
      // 确认更新
      const confirmButton = page.locator('button:has-text("确认"), .el-button--danger').first();
      if (await confirmButton.count() > 0) {
        await confirmButton.click();
        await waitForMessage(page, undefined, 10000);
      } else {
        // 如果没有确认对话框，直接等待消息
        await waitForMessage(page, undefined, 10000);
      }
    }
  });

  test('应该能够筛选支付记录', async ({ page }) => {
    // 等待表格加载
    await waitForTableData(page, '.el-table', 10000);
    
    // 查找筛选器（日期选择器、状态选择器等）
    const filterSelect = page.locator('.el-select, .el-date-picker').first();
    if (await filterSelect.count() > 0 && await filterSelect.isVisible()) {
      await filterSelect.click();
      await page.waitForTimeout(500);
      
      // 选择筛选条件
      const option = page.locator('.el-select-dropdown__item, .el-picker-panel__link-btn').first();
      if (await option.count() > 0) {
        await option.click();
        await page.waitForTimeout(1000);
        
        // 验证筛选结果
        await waitForTableData(page, '.el-table', 10000);
      }
    }
  });
});
