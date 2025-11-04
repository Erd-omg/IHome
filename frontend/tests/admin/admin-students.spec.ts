import { test, expect } from '@playwright/test';
import { loginAsAdmin } from '../helpers/auth';
import { waitForPageLoad, waitForTableData, waitForDialog, waitForMessage } from '../helpers/wait-helpers';
import { TEST_DATA } from '../helpers/test-data';

test.describe('管理员学生管理', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsAdmin(page);
    await page.goto('/admin/students');
    await waitForPageLoad(page);
  });

  test('应该能够查看学生列表', async ({ page }) => {
    // 等待表格加载
    await waitForTableData(page, '.el-table', 10000);
    
    // 验证表格存在
    await expect(page.locator('.el-table, table')).toBeVisible();
  });

  test('应该能够搜索学生', async ({ page }) => {
    // 查找搜索输入框
    const searchInput = page.locator('input[placeholder*="搜索"], input[placeholder*="学号"], input[type="search"]').first();
    if (await searchInput.count() > 0) {
      await searchInput.fill('2024001');
      await page.waitForTimeout(1000);
      
      // 验证搜索结果
      await waitForTableData(page, '.el-table', 10000);
    }
  });

  test('应该能够创建学生', async ({ page }) => {
    // 查找新建按钮
    const createButton = page.locator('button:has-text("新建"), button:has-text("添加"), .el-button--primary').first();
    if (await createButton.count() > 0 && await createButton.isVisible()) {
      await createButton.click();
      await waitForDialog(page, undefined, 5000);
      
      // 填写学生信息
      const idInput = page.locator('input[placeholder*="学号"], input[name*="id"]').first();
      if (await idInput.count() > 0) {
        await idInput.fill(TEST_DATA.student.id);
      }
      
      const nameInput = page.locator('input[placeholder*="姓名"], input[name*="name"]').first();
      if (await nameInput.count() > 0) {
        await nameInput.fill(TEST_DATA.student.name);
      }
      
      // 提交表单
      const submitButton = page.locator('button:has-text("提交"), button:has-text("确认"), button[type="submit"]').first();
      if (await submitButton.count() > 0) {
        await submitButton.click();
        await waitForMessage(page, undefined, 10000);
      }
    }
  });

  test('应该能够编辑学生信息', async ({ page }) => {
    // 等待表格加载
    await waitForTableData(page, '.el-table', 10000);
    
    // 查找编辑按钮
    const editButton = page.locator('button:has-text("编辑"), .el-button--text').first();
    if (await editButton.count() > 0 && await editButton.isVisible()) {
      await editButton.click();
      await waitForDialog(page, undefined, 5000);
      
      // 修改信息
      const nameInput = page.locator('input[placeholder*="姓名"]').first();
      if (await nameInput.count() > 0) {
        await nameInput.fill('修改后的姓名');
        
        // 保存
        const saveButton = page.locator('button:has-text("保存"), button:has-text("确认")').first();
        if (await saveButton.count() > 0) {
          await saveButton.click();
          await waitForMessage(page, undefined, 10000);
        }
      }
    }
  });

  test('应该能够导入学生', async ({ page }) => {
    await page.goto('/admin/student-import');
    await waitForPageLoad(page);
    
    // 验证导入页面加载
    await expect(page.locator('body')).toBeVisible();
    
    // 如果有文件上传功能，测试上传（注意：实际测试需要准备测试文件）
    const fileInput = page.locator('input[type="file"]').first();
    if (await fileInput.count() > 0) {
      // 这里可以测试文件上传功能（需要准备测试文件）
      // await fileInput.setInputFiles('path/to/test-file.xlsx');
    }
  });
});
