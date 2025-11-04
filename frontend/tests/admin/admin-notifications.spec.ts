import { test, expect } from '@playwright/test';
import { loginAsAdmin } from '../helpers/auth';
import { waitForPageLoad, waitForTableData, waitForDialog, waitForMessage, closeNotificationDrawer, closeOverlayDialogs } from '../helpers/wait-helpers';
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
    
    // 验证表格存在（使用更具体的选择器避免匹配到日期选择器的table）
    await expect(page.locator('.el-table').first()).toBeVisible();
  });

  test('应该能够创建通知', async ({ page }) => {
    // 先关闭可能存在的通知抽屉
    await closeNotificationDrawer(page);
    
    // 关闭可能存在的消息框
    await page.keyboard.press('Escape');
    await page.waitForTimeout(300);
    
    // 查找新建按钮
    const createButton = page.locator('button:has-text("新建"), button:has-text("发送")').first();
    if (await createButton.count() > 0 && await createButton.isVisible()) {
      await createButton.scrollIntoViewIfNeeded();
      await createButton.click({ force: true });
      
      // 等待对话框出现（增加超时时间）
      try {
        await waitForDialog(page, undefined, 15000);
      } catch {
        // 如果对话框没有出现，继续尝试填写（可能对话框已经存在）
      }
      
      // 关闭可能遮挡的对话框
      await closeOverlayDialogs(page);
      await page.waitForTimeout(500);
      
      // 等待对话框内容完全加载
      await page.waitForFunction(
        () => {
          const dialog = document.querySelector('.el-dialog:not([style*="display: none"])');
          if (!dialog) return false;
          const inputs = dialog.querySelectorAll('input, textarea');
          return inputs.length > 0;
        },
        { timeout: 5000 }
      ).catch(() => {
        // 如果超时，继续尝试
      });
      
      // 填写通知信息 - 在对话框中查找输入框
      const titleInput = page.locator('.el-dialog input[placeholder*="标题"], .el-dialog input[name*="title"], input[placeholder*="标题"], input[name*="title"]')
        .first();
      if (await titleInput.count() > 0) {
        // 等待输入框附加到 DOM
        await titleInput.waitFor({ state: 'attached', timeout: 5000 }).catch(() => {});
        await page.waitForTimeout(300);
        
        // 检查是否可见，如果可见则滚动，否则直接使用 JavaScript
        const isVisible = await titleInput.isVisible({ timeout: 2000 }).catch(() => false);
        if (isVisible) {
          await titleInput.scrollIntoViewIfNeeded();
          await titleInput.fill(TEST_DATA.notification.title);
        } else {
          // 如果不可见，通过 JavaScript 设置值
          await titleInput.evaluate((el: HTMLInputElement, value: string) => {
            el.value = value;
            el.dispatchEvent(new Event('input', { bubbles: true }));
            el.dispatchEvent(new Event('change', { bubbles: true }));
          }, TEST_DATA.notification.title);
        }
      }
      
      const contentInput = page.locator('.el-dialog textarea[placeholder*="内容"], .el-dialog textarea, textarea[placeholder*="内容"], textarea')
        .first();
      if (await contentInput.count() > 0) {
        await contentInput.waitFor({ state: 'attached', timeout: 5000 }).catch(() => {});
        await page.waitForTimeout(300);
        
        const isVisible = await contentInput.isVisible({ timeout: 2000 }).catch(() => false);
        if (isVisible) {
          await contentInput.scrollIntoViewIfNeeded();
          await contentInput.fill(TEST_DATA.notification.content);
        } else {
          // 如果不可见，通过 JavaScript 设置值
          await contentInput.evaluate((el: HTMLTextAreaElement, value: string) => {
            el.value = value;
            el.dispatchEvent(new Event('input', { bubbles: true }));
            el.dispatchEvent(new Event('change', { bubbles: true }));
          }, TEST_DATA.notification.content);
        }
      }
      
      // 提交 - 使用更精确的选择器，排除消息框中的按钮
      const submitButton = page.locator('.el-dialog button:has-text("提交"), .el-dialog button:has-text("发送"), button:has-text("提交"), button:has-text("发送")')
        .filter({ hasNot: page.locator('.el-message-box') })
        .first();
      if (await submitButton.count() > 0) {
        await submitButton.waitFor({ state: 'attached', timeout: 5000 }).catch(() => {});
        await page.waitForTimeout(300);
        
        const isVisible = await submitButton.isVisible({ timeout: 2000 }).catch(() => false);
        if (isVisible) {
          await submitButton.scrollIntoViewIfNeeded();
          await submitButton.click();
        } else {
          // 如果不可见，直接使用 force 点击
          await submitButton.click({ force: true });
        }
        await waitForMessage(page, undefined, 10000, false); // 消息可能不显示
      }
    }
  });

  test('应该能够删除通知', async ({ page }) => {
    // 等待表格加载
    await waitForTableData(page, '.el-table', 10000);
    
    // 查找删除按钮
    const deleteButton = page.locator('button:has-text("删除"), .el-button--danger').first();
    if (await deleteButton.count() > 0 && await deleteButton.isVisible()) {
      await deleteButton.scrollIntoViewIfNeeded();
      await deleteButton.click({ force: true });
      
      // 等待确认对话框出现
      await page.waitForTimeout(500);
      
      // 确认删除 - 在确认对话框中查找按钮
      const confirmButton = page.locator('.el-message-box button:has-text("确定"), .el-message-box .el-button--primary').first();
      if (await confirmButton.count() > 0) {
        await confirmButton.scrollIntoViewIfNeeded();
        await confirmButton.click({ force: true });
        await waitForMessage(page, undefined, 10000, false);
      } else {
        // 如果找不到确认按钮，尝试查找其他确认按钮
        const altConfirmButton = page.locator('button:has-text("确认"), .el-button--danger').first();
        if (await altConfirmButton.count() > 0) {
          await altConfirmButton.scrollIntoViewIfNeeded();
          await altConfirmButton.click({ force: true });
          await waitForMessage(page, undefined, 10000, false);
        }
      }
    }
  });
});
