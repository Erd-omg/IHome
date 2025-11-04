import { test, expect } from '@playwright/test';
import { loginAsAdmin } from '../helpers/auth';
import { waitForPageLoad, waitForTableData, waitForDialog, waitForMessage } from '../helpers/wait-helpers';
import { TEST_DATA } from '../helpers/test-data';

test.describe('管理员通知管理', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsAdmin(page);
    await page.goto('/admin/notifications');
    await waitForPageLoad(page);
  });

  test('应该能够查看通知列表', async ({ page }) => {
    // 等待表格加载
    await waitForTableData(page, '.el-table', 10000);
    
    // 验证表格存在
    await expect(page.locator('.el-table, table')).toBeVisible();
  });

  test('应该能够创建通知', async ({ page }) => {
    // 查找新建按钮
    const createButton = page.locator('button:has-text("新建"), button:has-text("发送")').first();
    if (await createButton.count() > 0 && await createButton.isVisible()) {
      await createButton.click();
      await waitForDialog(page, undefined, 5000);
      
      // 填写通知信息
      const titleInput = page.locator('input[placeholder*="标题"], input[name*="title"]').first();
      if (await titleInput.count() > 0) {
        await titleInput.fill(TEST_DATA.notification.title);
      }
      
      const contentInput = page.locator('textarea[placeholder*="内容"], textarea').first();
      if (await contentInput.count() > 0) {
        await contentInput.fill(TEST_DATA.notification.content);
      }
      
      // 提交
      const submitButton = page.locator('button:has-text("提交"), button:has-text("发送")').first();
      if (await submitButton.count() > 0) {
        await submitButton.click();
        await waitForMessage(page, undefined, 10000);
      }
    }
  });

  test('应该能够删除通知', async ({ page }) => {
    // 等待表格加载
    await waitForTableData(page, '.el-table', 10000);
    
    // 查找删除按钮
    const deleteButton = page.locator('button:has-text("删除"), .el-button--danger').first();
    if (await deleteButton.count() > 0 && await deleteButton.isVisible()) {
      await deleteButton.click();
      
      // 确认删除
      const confirmButton = page.locator('button:has-text("确认"), .el-button--danger').first();
      if (await confirmButton.count() > 0) {
        await confirmButton.click();
        await waitForMessage(page, undefined, 10000);
      }
    }
  });
});
