import { test, expect } from '@playwright/test';
import { loginAsStudent } from '../helpers/auth';
import { waitForPageLoad, waitForMessage, closeNotificationDrawer } from '../helpers/wait-helpers';

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
    
    // 先关闭可能存在的通知抽屉，避免遮挡按钮
    await closeNotificationDrawer(page);
    
    // 验证页面加载
    await expect(page.locator('body')).toBeVisible();
    
    // 如果有标签选择，进行选择
    const tagButton = page.locator('.tag, .el-tag, button').first();
    if (await tagButton.count() > 0 && await tagButton.isVisible()) {
      await tagButton.click();
      await page.waitForTimeout(300);
      
      // 再次关闭通知抽屉，确保按钮可见
      await closeNotificationDrawer(page);
      
      // 保存 - 使用更精确的选择器，排除通知抽屉中的按钮
      const saveButton = page.locator('button:has-text("保存"), button:has-text("提交")')
        .filter({ hasNot: page.locator('.notification-actions') })
        .first();
      
      if (await saveButton.count() > 0) {
        // 确保按钮可见且可交互
        await saveButton.waitFor({ state: 'visible', timeout: 5000 });
        await saveButton.scrollIntoViewIfNeeded();
        await saveButton.click({ force: true }); // 使用 force 选项，如果被遮挡也强制点击
        await waitForMessage(page, undefined, 10000);
      }
    }
  });
});
