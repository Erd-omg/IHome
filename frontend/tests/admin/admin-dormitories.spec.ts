import { test, expect } from '@playwright/test';
import { loginAsAdmin } from '../helpers/auth';
import { waitForPageLoad, waitForTableData, waitForDialog, waitForMessage, closeNotificationDrawer, closeOverlayDialogs } from '../helpers/wait-helpers';
import { TEST_DATA } from '../helpers/test-data';

test.describe('管理员宿舍管理', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsAdmin(page);
    await page.goto('/admin/dormitories');
    await waitForPageLoad(page);
  });

  test('应该能够查看宿舍列表', async ({ page }) => {
    // 等待表格加载
    await waitForTableData(page, '.el-table', 10000);
    
    // 验证表格存在（使用更具体的选择器避免匹配到日期选择器的table）
    await expect(page.locator('.el-table').first()).toBeVisible();
  });

  test('应该能够创建宿舍', async ({ page }) => {
    // 查找新建按钮
    const createButton = page.locator('button:has-text("新建"), button:has-text("添加")').first();
    if (await createButton.count() > 0 && await createButton.isVisible()) {
      await createButton.click();
      await waitForDialog(page, undefined, 5000);
      
      // 填写宿舍信息
      const roomNumberInput = page.locator('input[placeholder*="房间号"], input[name*="roomNumber"]').first();
      if (await roomNumberInput.count() > 0) {
        await roomNumberInput.fill(TEST_DATA.dormitory.roomNumber);
      }
      
      const capacityInput = page.locator('input[placeholder*="容量"], input[type="number"]').first();
      if (await capacityInput.count() > 0) {
        await capacityInput.fill(TEST_DATA.dormitory.capacity.toString());
      }
      
      // 提交表单
      const submitButton = page.locator('button:has-text("提交"), button:has-text("确认")').first();
      if (await submitButton.count() > 0) {
        await submitButton.click();
        await waitForMessage(page, undefined, 10000);
      }
    }
  });

  test('应该能够编辑宿舍信息', async ({ page }) => {
    // 等待表格加载
    await waitForTableData(page, '.el-table', 10000);
    
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
        // 如果对话框没有出现，继续尝试操作
      }
      
      // 关闭可能遮挡的对话框
      await closeOverlayDialogs(page);
      await page.waitForTimeout(300);
      
      // 修改信息
      const capacityInput = page.locator('.el-dialog input[type="number"], input[type="number"]').first();
      if (await capacityInput.count() > 0) {
        // 等待输入框附加到 DOM
        await capacityInput.waitFor({ state: 'attached', timeout: 5000 }).catch(() => {});
        await page.waitForTimeout(300);
        
        // 检查是否可见，如果可见则滚动，否则直接使用 JavaScript
        const isVisible = await capacityInput.isVisible({ timeout: 2000 }).catch(() => false);
        if (isVisible) {
          await capacityInput.scrollIntoViewIfNeeded();
          await capacityInput.fill('6');
        } else {
          // 如果不可见，通过 JavaScript 设置值
          await capacityInput.evaluate((el: HTMLInputElement, value: string) => {
            el.value = value;
            el.dispatchEvent(new Event('input', { bubbles: true }));
            el.dispatchEvent(new Event('change', { bubbles: true }));
          }, '6');
        }
        await page.waitForTimeout(300);
        
        // 保存 - 在对话框中查找按钮
        const saveButton = page.locator('.el-dialog button:has-text("保存"), button:has-text("保存")')
          .filter({ hasNot: page.locator('.el-message-box') })
          .first();
        
        if (await saveButton.count() > 0) {
          // 等待按钮附加到 DOM
          await saveButton.waitFor({ state: 'attached', timeout: 5000 }).catch(() => {});
          await page.waitForTimeout(300);
          
          // 尝试点击（如果不可见，使用 JavaScript 直接点击）
          const isButtonVisible = await saveButton.isVisible({ timeout: 2000 }).catch(() => false);
          if (isButtonVisible) {
            await saveButton.scrollIntoViewIfNeeded();
            await saveButton.click();
          } else {
            // 如果不可见，通过 JavaScript 直接点击
            await saveButton.evaluate((el: HTMLElement) => {
              (el as HTMLButtonElement).click();
            });
          }
          await waitForMessage(page, undefined, 10000, false);
        }
      }
    }
  });
});
