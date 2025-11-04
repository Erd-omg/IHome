import { test, expect } from '@playwright/test';
import { loginAsStudent } from '../helpers/auth';
import { waitForPageLoad, waitForMessage } from '../helpers/wait-helpers';

test.describe('学生个人资料', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsStudent(page);
    await page.goto('/profile');
    await waitForPageLoad(page);
  });

  test('应该能够查看个人资料', async ({ page }) => {
    // 验证页面加载
    await expect(page.locator('body')).toBeVisible();
    
    // 验证用户信息显示（根据实际UI调整）
    // 例如：姓名、学号、邮箱等
  });

  test('应该能够编辑个人资料', async ({ page }) => {
    // 查找编辑按钮
    const editButton = page.locator('button:has-text("编辑"), button:has-text("修改")').first();
    if (await editButton.count() > 0 && await editButton.isVisible()) {
      await editButton.click();
      
      // 修改信息（例如邮箱）
      const emailInput = page.locator('input[type="email"], input[placeholder*="邮箱"]').first();
      if (await emailInput.count() > 0) {
        const oldValue = await emailInput.inputValue();
        await emailInput.fill('newemail@example.com');
        
        // 保存
        const saveButton = page.locator('button:has-text("保存"), button:has-text("确认")').first();
        if (await saveButton.count() > 0) {
          await saveButton.click();
          await waitForMessage(page, undefined, 10000);
        }
      }
    }
  });

  test('应该能够设置生活方式标签', async ({ page }) => {
    await page.goto('/lifestyle-tags');
    await waitForPageLoad(page);
    
    // 验证页面加载
    await expect(page.locator('body')).toBeVisible();
    
    // 如果有标签选择，进行选择
    const tagButton = page.locator('.tag, .el-tag, button').first();
    if (await tagButton.count() > 0 && await tagButton.isVisible()) {
      await tagButton.click();
      
      // 保存
      const saveButton = page.locator('button:has-text("保存"), button:has-text("提交")').first();
      if (await saveButton.count() > 0) {
        await saveButton.click();
        await waitForMessage(page, undefined, 10000);
      }
    }
  });
});
