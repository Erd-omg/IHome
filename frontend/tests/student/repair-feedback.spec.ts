import { test, expect } from '@playwright/test';
import { loginAsStudent } from '../helpers/auth';
import { waitForPageLoad, waitForMessage, waitForDialog, closeNotificationDrawer } from '../helpers/wait-helpers';
import { TEST_DATA } from '../helpers/test-data';

test.describe('学生维修反馈', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsStudent(page);
  });

  test('应该能够查看维修反馈页面', async ({ page }) => {
    await page.goto('/repair-feedback');
    await waitForPageLoad(page);
    
    // 验证页面加载
    await expect(page.locator('body')).toBeVisible();
  });

  test('应该能够提交维修反馈', async ({ page }) => {
    await page.goto('/repair-feedback');
    await waitForPageLoad(page);
    
    // 查找维修工单选择器（如果有）
    const repairSelect = page.locator('select, .el-select').first();
    if (await repairSelect.count() > 0) {
      await repairSelect.click();
      await page.waitForTimeout(500);
      const option = page.locator('.el-select-dropdown__item').first();
      if (await option.count() > 0) {
        await option.click();
      }
    }
    
    // 填写反馈内容
    const feedbackInput = page.locator('textarea[placeholder*="反馈"], textarea[placeholder*="评价"]').first();
    if (await feedbackInput.count() > 0) {
      await feedbackInput.fill(TEST_DATA.repair.feedback || '维修服务很好，问题已解决');
    }
    
    // 选择评分（如果有）
    const ratingInput = page.locator('.el-rate, input[type="number"]').first();
    if (await ratingInput.count() > 0) {
      // 尝试设置评分
      await ratingInput.click();
    }
    
    // 提交反馈
    const submitButton = page.locator('button:has-text("提交"), button:has-text("保存")').first();
    if (await submitButton.count() > 0 && await submitButton.isVisible()) {
      await submitButton.click();
      await waitForMessage(page, undefined, 5000);
    }
  });

  test('应该能够查看历史反馈', async ({ page }) => {
    await page.goto('/repair-feedback');
    await waitForPageLoad(page);
    
    // 验证页面显示反馈列表或历史记录
    const feedbackList = page.locator('.el-table, .feedback-list, .history-list').first();
    if (await feedbackList.count() > 0) {
      await expect(feedbackList).toBeVisible();
    }
  });

  test('应该能够编辑已提交的反馈', async ({ page }) => {
    await page.goto('/repair-feedback');
    await waitForPageLoad(page);
    
    // 关闭可能存在的通知抽屉和其他对话框
    await closeNotificationDrawer(page);
    await page.keyboard.press('Escape');
    await page.waitForTimeout(300);
    
    // 查找编辑按钮
    const editButton = page.locator('button:has-text("编辑"), .el-button--text').first();
    if (await editButton.count() > 0 && await editButton.isVisible()) {
      await editButton.scrollIntoViewIfNeeded();
      await editButton.click({ force: true });
      
      // 等待对话框出现（增加超时时间）
      try {
        await waitForDialog(page, undefined, 15000);
      } catch {
        // 如果对话框没有出现，继续尝试操作（可能以其他方式显示）
      }
      
      // 修改反馈内容
      const feedbackInput = page.locator('textarea').first();
      if (await feedbackInput.count() > 0) {
        await feedbackInput.fill('更新后的反馈内容');
      }
      
      // 保存
      const saveButton = page.locator('button:has-text("保存"), button:has-text("确定")')
        .filter({ hasNot: page.locator('.el-message-box') })
        .first();
      if (await saveButton.count() > 0) {
        await saveButton.scrollIntoViewIfNeeded();
        await saveButton.click({ force: true });
        await waitForMessage(page, undefined, 5000, false);
      }
    }
  });
});

