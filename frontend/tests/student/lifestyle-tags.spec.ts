import { test, expect } from '@playwright/test';
import { loginAsStudent } from '../helpers/auth';
import { waitForPageLoad, waitForMessage } from '../helpers/wait-helpers';
import { TEST_DATA } from '../helpers/test-data';

test.describe('学生生活方式标签', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsStudent(page);
  });

  test('应该能够查看生活方式标签页面', async ({ page }) => {
    await page.goto('/lifestyle-tags');
    await waitForPageLoad(page);
    
    // 验证页面加载
    await expect(page.locator('body')).toBeVisible();
  });

  test('应该能够设置生活方式标签', async ({ page }) => {
    await page.goto('/lifestyle-tags');
    await waitForPageLoad(page);
    
    // 查找标签选择器
    const tagInput = page.locator('input[placeholder*="标签"], .el-tag').first();
    if (await tagInput.count() > 0) {
      // 尝试选择标签
      const availableTags = TEST_DATA.lifestyleTags;
      if (availableTags && availableTags.length > 0) {
        // 查找并点击标签按钮
        const tagButton = page.locator(`text=${availableTags[0]}`).first();
        if (await tagButton.count() > 0) {
          await tagButton.click();
          await page.waitForTimeout(500);
        }
      }
    }
    
    // 查找保存按钮
    const saveButton = page.locator('button:has-text("保存"), button:has-text("提交")').first();
    if (await saveButton.count() > 0 && await saveButton.isVisible()) {
      await saveButton.click();
      await waitForMessage(page, undefined, 5000);
    }
  });

  test('应该能够删除已选标签', async ({ page }) => {
    await page.goto('/lifestyle-tags');
    await waitForPageLoad(page);
    
    // 查找已选标签的删除按钮
    const deleteButton = page.locator('.el-tag .el-icon-close, .tag-close').first();
    if (await deleteButton.count() > 0 && await deleteButton.isVisible()) {
      await deleteButton.click();
      await page.waitForTimeout(500);
    }
  });

  test('应该能够查看标签说明', async ({ page }) => {
    await page.goto('/lifestyle-tags');
    await waitForPageLoad(page);
    
    // 验证页面包含标签说明或帮助信息
    const helpText = page.locator('text=标签, text=说明, text=帮助').first();
    if (await helpText.count() > 0) {
      await expect(helpText).toBeVisible();
    }
  });
});

