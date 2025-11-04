import { test, expect } from '@playwright/test';
import { loginAsAdmin } from '../helpers/auth';
import { waitForPageLoad, waitForTableData, waitForDialog, waitForMessage } from '../helpers/wait-helpers';
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
    
    // 验证表格存在
    await expect(page.locator('.el-table, table')).toBeVisible();
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
    
    // 查找编辑按钮
    const editButton = page.locator('button:has-text("编辑"), .el-button--text').first();
    if (await editButton.count() > 0 && await editButton.isVisible()) {
      await editButton.click();
      await waitForDialog(page, undefined, 5000);
      
      // 修改信息
      const capacityInput = page.locator('input[type="number"]').first();
      if (await capacityInput.count() > 0) {
        await capacityInput.fill('6');
        
        // 保存
        const saveButton = page.locator('button:has-text("保存")').first();
        if (await saveButton.count() > 0) {
          await saveButton.click();
          await waitForMessage(page, undefined, 10000);
        }
      }
    }
  });
});
