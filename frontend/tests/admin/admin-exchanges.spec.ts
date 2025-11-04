import { test, expect } from '@playwright/test';
import { loginAsAdmin } from '../helpers/auth';
import { waitForPageLoad, waitForTableData, waitForDialog, waitForMessage } from '../helpers/wait-helpers';

test.describe('管理员交换管理', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsAdmin(page);
    await page.goto('/admin/exchanges');
    await waitForPageLoad(page);
  });

  test('应该能够查看交换申请列表', async ({ page }) => {
    // 等待表格加载
    await waitForTableData(page, '.el-table', 10000);
    
    // 验证表格存在
    await expect(page.locator('.el-table, table')).toBeVisible();
  });

  test('应该能够审核交换申请', async ({ page }) => {
    // 等待表格加载
    await waitForTableData(page, '.el-table', 10000);
    
    // 查找审核按钮
    const reviewButton = page.locator('button:has-text("审核"), button:has-text("处理")').first();
    if (await reviewButton.count() > 0 && await reviewButton.isVisible()) {
      await reviewButton.click();
      await waitForDialog(page, undefined, 5000);
      
      // 选择审核结果
      const approveButton = page.locator('button:has-text("同意"), button:has-text("批准")').first();
      const rejectButton = page.locator('button:has-text("拒绝"), button:has-text("驳回")').first();
      
      if (await approveButton.count() > 0) {
        await approveButton.click();
        await waitForMessage(page, undefined, 10000);
      } else if (await rejectButton.count() > 0) {
        // 拒绝时可能需要填写原因
        const reasonInput = page.locator('textarea, input[type="text"]').first();
        if (await reasonInput.count() > 0) {
          await reasonInput.fill('测试拒绝原因');
        }
        await rejectButton.click();
        await waitForMessage(page, undefined, 10000);
      }
    }
  });

  test('应该能够查看交换申请详情', async ({ page }) => {
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
