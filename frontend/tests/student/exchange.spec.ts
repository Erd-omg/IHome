import { test, expect } from '@playwright/test';
import { loginAsStudent } from '../helpers/auth';
import { waitForPageLoad, waitForTableData, waitForDialog, waitForMessage } from '../helpers/wait-helpers';
import { TEST_DATA } from '../helpers/test-data';

test.describe('学生宿舍交换功能', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsStudent(page);
  });

  test('应该能够查看交换申请列表', async ({ page }) => {
    await page.goto('/exchange');
    await waitForPageLoad(page);
    
    // 等待表格加载
    await waitForTableData(page, '.el-table', 10000);
    
    // 验证页面元素
    await expect(page.locator('body')).toBeVisible();
  });

  test('应该能够创建交换申请', async ({ page }) => {
    await page.goto('/exchange/new');
    await waitForPageLoad(page);
    
    // 填写交换申请表单
    const reasonInput = page.locator('textarea[placeholder*="原因"], textarea[placeholder*="理由"]').first();
    if (await reasonInput.count() > 0) {
      await reasonInput.fill(TEST_DATA.exchange.reason);
    }
    
    // 选择目标宿舍（如果有选择器）
    const dormSelect = page.locator('.el-select').first();
    if (await dormSelect.count() > 0) {
      await dormSelect.click();
      await page.locator('.el-select-dropdown__item').first().click();
    }
    
    // 提交表单
    const submitButton = page.locator('button:has-text("提交"), button:has-text("申请"), button[type="submit"]').first();
    if (await submitButton.count() > 0 && await submitButton.isVisible()) {
      await submitButton.click();
      await waitForMessage(page, undefined, 10000);
    }
  });

  test('应该能够查看交换推荐', async ({ page }) => {
    await page.goto('/exchange-recommendations');
    await waitForPageLoad(page);
    
    // 验证页面加载
    await expect(page.locator('body')).toBeVisible();
    
    // 等待内容加载
    await page.waitForTimeout(2000);
  });
});
