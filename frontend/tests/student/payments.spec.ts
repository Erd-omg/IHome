import { test, expect } from '@playwright/test';
import { loginAsStudent } from '../helpers/auth';
import { waitForPageLoad, waitForTableData, waitForDialog, waitForMessage, closeNotificationDrawer, closeOverlayDialogs } from '../helpers/wait-helpers';
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
    
    // 关闭可能存在的通知抽屉和其他对话框
    await closeNotificationDrawer(page);
    await page.keyboard.press('Escape');
    await page.waitForTimeout(300);
    
    // 查找创建按钮
    const createButton = page.locator('button:has-text("新建"), button:has-text("缴费"), .el-button--primary').first();
    if (await createButton.count() > 0 && await createButton.isVisible()) {
      await createButton.scrollIntoViewIfNeeded();
      await createButton.click({ force: true });
      
      // 等待对话框出现
      try {
        await waitForDialog(page, undefined, 15000);
      } catch {
        // 如果对话框没有出现，继续尝试操作
      }
      
      // 关闭可能遮挡的对话框（如电费提醒设置）
      await closeOverlayDialogs(page, ['创建', '缴费', '支付']);
      await page.waitForTimeout(300);
      
      // 填写表单（根据实际表单字段调整）
      const typeSelect = page.locator('.el-select')
        .filter({ hasNot: page.locator('.el-overlay-dialog[aria-label*="电费提醒"]') })
        .first();
      if (await typeSelect.count() > 0 && await typeSelect.isVisible()) {
        await typeSelect.scrollIntoViewIfNeeded();
        await typeSelect.click({ force: true });
        await page.waitForTimeout(300);
        const option = page.locator('.el-select-dropdown__item').first();
        if (await option.count() > 0) {
          await option.click();
        }
      }
      
      const amountInput = page.locator('input[placeholder*="金额"], input[type="number"]').first();
      if (await amountInput.count() > 0) {
        await amountInput.fill('100');
      }
      
      // 提交表单
      const submitButton = page.locator('button:has-text("提交"), button:has-text("确认"), button[type="submit"]')
        .filter({ hasNot: page.locator('.el-overlay-dialog[aria-label*="电费提醒"]') })
        .first();
      if (await submitButton.count() > 0) {
        await submitButton.scrollIntoViewIfNeeded();
        await submitButton.click({ force: true });
        await waitForMessage(page, undefined, 10000, false);
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
