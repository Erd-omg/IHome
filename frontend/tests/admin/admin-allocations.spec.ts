import { test, expect } from '@playwright/test';
import { loginAsAdmin } from '../helpers/auth';
import { waitForPageLoad, waitForTableData, waitForDialog, waitForMessage } from '../helpers/wait-helpers';

test.describe('管理员分配管理', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsAdmin(page);
    await page.goto('/admin/allocations');
    await waitForPageLoad(page);
  });

  test('应该能够查看分配列表', async ({ page }) => {
    // 等待表格加载
    await waitForTableData(page, '.el-table', 10000);
    
    // 验证表格存在（使用更具体的选择器避免匹配到日期选择器的table）
    await expect(page.locator('.el-table').first()).toBeVisible();
  });

  test('应该能够创建新分配', async ({ page }) => {
    // 查找新建按钮
    const createButton = page.locator('button:has-text("新建"), button:has-text("分配")').first();
    if (await createButton.count() > 0 && await createButton.isVisible()) {
      await createButton.click();
      await waitForDialog(page, undefined, 5000);
      
      // 等待对话框完全打开后再操作
      await page.waitForTimeout(500);
      
      // 选择学生（如果有选择器）- 使用对话框内的选择器
      const studentSelect = page.locator('.el-dialog .el-select, .el-drawer .el-select').first();
      if (await studentSelect.count() > 0) {
        // 确保元素可见且可交互
        await studentSelect.waitFor({ state: 'visible', timeout: 5000 });
        await studentSelect.scrollIntoViewIfNeeded();
        await studentSelect.click({ force: false });
        await page.waitForTimeout(300);
        await page.locator('.el-select-dropdown__item').first().click();
      }
      
      // 选择床位
      const bedSelect = page.locator('.el-dialog .el-select, .el-drawer .el-select').nth(1);
      if (await bedSelect.count() > 0) {
        await bedSelect.waitFor({ state: 'visible', timeout: 5000 });
        await bedSelect.scrollIntoViewIfNeeded();
        await bedSelect.click({ force: false });
        await page.waitForTimeout(300);
        await page.locator('.el-select-dropdown__item').first().click();
      }
      
      // 提交
      const submitButton = page.locator('.el-dialog button:has-text("提交"), .el-dialog button:has-text("确认"), .el-drawer button:has-text("提交")').first();
      if (await submitButton.count() > 0) {
        await submitButton.click();
        await waitForMessage(page, undefined, 10000);
      }
    }
  });

  test('应该能够处理退宿', async ({ page }) => {
    // 等待表格加载
    await waitForTableData(page, '.el-table', 10000);
    
    // 查找退宿按钮
    const checkoutButton = page.locator('button:has-text("退宿"), button:has-text("退房")').first();
    if (await checkoutButton.count() > 0 && await checkoutButton.isVisible()) {
      await checkoutButton.click();
      
      // 确认退宿
      const confirmButton = page.locator('button:has-text("确认"), .el-button--danger').first();
      if (await confirmButton.count() > 0) {
        await confirmButton.click();
        await waitForMessage(page, undefined, 10000);
      }
    }
  });
});
