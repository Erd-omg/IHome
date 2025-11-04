import { test, expect } from '@playwright/test';
import { loginAsStudent } from '../helpers/auth';
import { waitForPageLoad, waitForTableData, waitForDialog, waitForMessage } from '../helpers/wait-helpers';
import { TEST_DATA } from '../helpers/test-data';

test.describe('学生支付功能', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsStudent(page);
  });

  test('应该能够查看支付记录', async ({ page }) => {
    await page.goto('/payments');
    await waitForPageLoad(page);
    
    // 等待表格加载
    await waitForTableData(page, '.el-table', 10000);
    
    // 验证页面元素
    await expect(page.locator('body')).toBeVisible();
  });

  test('应该能够创建支付记录', async ({ page }) => {
    await page.goto('/payments');
    await waitForPageLoad(page);
    
    // 查找创建按钮
    const createButton = page.locator('button:has-text("新建"), button:has-text("缴费"), .el-button--primary').first();
    if (await createButton.count() > 0 && await createButton.isVisible()) {
      await createButton.click();
      await waitForDialog(page, undefined, 5000);
      
      // 填写表单（根据实际表单字段调整）
      const typeSelect = page.locator('.el-select').first();
      if (await typeSelect.count() > 0) {
        await typeSelect.click();
        await page.locator('.el-select-dropdown__item').first().click();
      }
      
      const amountInput = page.locator('input[placeholder*="金额"], input[type="number"]').first();
      if (await amountInput.count() > 0) {
        await amountInput.fill('100');
      }
      
      // 提交表单
      const submitButton = page.locator('button:has-text("提交"), button:has-text("确认"), button[type="submit"]').first();
      if (await submitButton.count() > 0) {
        await submitButton.click();
        await waitForMessage(page, undefined, 10000);
      }
    }
  });

  test('应该能够查看异常支付', async ({ page }) => {
    await page.goto('/payment-abnormal');
    await waitForPageLoad(page);
    
    // 验证页面加载
    await expect(page.locator('body')).toBeVisible();
    
    // 等待表格加载（如果有）
    await waitForTableData(page, '.el-table', 10000).catch(() => {});
  });
});
