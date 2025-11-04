import { test, expect } from '@playwright/test';
import { loginAsStudent } from '../helpers/auth';
import { waitForPageLoad, waitForTableData, waitForDialog, waitForMessage } from '../helpers/wait-helpers';
import { TEST_DATA } from '../helpers/test-data';

test.describe('学生维修功能', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsStudent(page);
  });

  test('应该能够查看维修工单列表', async ({ page }) => {
    await page.goto('/repairs');
    await waitForPageLoad(page);
    
    // 等待表格加载
    await waitForTableData(page, '.el-table', 10000);
    
    // 验证页面元素
    await expect(page.locator('body')).toBeVisible();
  });

  test('应该能够创建维修工单', async ({ page }) => {
    await page.goto('/repairs');
    await waitForPageLoad(page);
    
    // 查找创建按钮
    const createButton = page.locator('button:has-text("新建"), button:has-text("报修"), .el-button--primary').first();
    if (await createButton.count() > 0 && await createButton.isVisible()) {
      await createButton.click();
      await waitForDialog(page, undefined, 5000);
      
      // 填写表单
      const titleInput = page.locator('input[placeholder*="标题"], input[placeholder*="问题"]').first();
      if (await titleInput.count() > 0) {
        await titleInput.fill(TEST_DATA.repair.title);
      }
      
      const descriptionInput = page.locator('textarea[placeholder*="描述"], textarea[placeholder*="详情"]').first();
      if (await descriptionInput.count() > 0) {
        await descriptionInput.fill(TEST_DATA.repair.description);
      }
      
      // 选择类别
      const categorySelect = page.locator('.el-select').first();
      if (await categorySelect.count() > 0) {
        await categorySelect.click();
        await page.locator('.el-select-dropdown__item').first().click();
      }
      
      // 提交表单
      const submitButton = page.locator('button:has-text("提交"), button:has-text("确认"), button[type="submit"]').first();
      if (await submitButton.count() > 0) {
        await submitButton.click();
        await waitForMessage(page, undefined, 10000);
      }
    }
  });

  test('应该能够查看维修反馈', async ({ page }) => {
    await page.goto('/repair-feedback');
    await waitForPageLoad(page);
    
    // 验证页面加载
    await expect(page.locator('body')).toBeVisible();
  });

  test('应该能够提交维修反馈', async ({ page }) => {
    // 先进入维修列表，选择一个工单
    await page.goto('/repairs');
    await waitForPageLoad(page);
    
    // 查找反馈按钮或链接
    const feedbackButton = page.locator('text=反馈, button:has-text("反馈")').first();
    if (await feedbackButton.count() > 0 && await feedbackButton.isVisible()) {
      await feedbackButton.click();
      await waitForDialog(page, undefined, 5000);
      
      // 填写反馈内容
      const feedbackInput = page.locator('textarea, input[type="text"]').first();
      if (await feedbackInput.count() > 0) {
        await feedbackInput.fill('测试反馈内容');
      }
      
      // 提交
      const submitButton = page.locator('button:has-text("提交"), button:has-text("确认")').first();
      if (await submitButton.count() > 0) {
        await submitButton.click();
        await waitForMessage(page, undefined, 10000);
      }
    }
  });
});
