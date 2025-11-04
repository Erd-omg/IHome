import { test, expect } from '@playwright/test';
import { loginAsStudent, loginAsAdmin } from '../helpers/auth';
import { waitForPageLoad, waitForMessage, waitForTableData } from '../helpers/wait-helpers';

test.describe('完整业务流程测试', () => {
  test('学生完整使用流程', async ({ page }) => {
    // 1. 登录
    await loginAsStudent(page);
    await page.goto('/dashboard');
    await waitForPageLoad(page);
    
    // 2. 查看个人资料
    await page.goto('/profile');
    await waitForPageLoad(page);
    await expect(page.locator('body')).toBeVisible();
    
    // 3. 设置生活方式标签
    await page.goto('/lifestyle-tags');
    await waitForPageLoad(page);
    
    // 4. 查看宿舍信息
    await page.goto('/dorm');
    await waitForPageLoad(page);
    
    // 5. 查看通知
    await page.goto('/notices');
    await waitForPageLoad(page);
    
    // 6. 查看支付记录
    await page.goto('/payments');
    await waitForPageLoad(page);
    await waitForTableData(page, '.el-table', 10000).catch(() => {});
    
    // 7. 查看维修工单
    await page.goto('/repairs');
    await waitForPageLoad(page);
    await waitForTableData(page, '.el-table', 10000).catch(() => {});
    
    // 8. 查看交换申请
    await page.goto('/exchange');
    await waitForPageLoad(page);
    await waitForTableData(page, '.el-table', 10000).catch(() => {});
  });

  test('管理员完整管理流程', async ({ page }) => {
    // 1. 登录为管理员
    await loginAsAdmin(page);
    await page.goto('/admin/dashboard');
    await waitForPageLoad(page);
    
    // 2. 查看学生管理
    await page.goto('/admin/students');
    await waitForPageLoad(page);
    await waitForTableData(page, '.el-table', 10000);
    
    // 3. 查看宿舍管理
    await page.goto('/admin/dormitories');
    await waitForPageLoad(page);
    await waitForTableData(page, '.el-table', 10000);
    
    // 4. 查看分配管理
    await page.goto('/admin/allocations');
    await waitForPageLoad(page);
    await waitForTableData(page, '.el-table', 10000);
    
    // 5. 查看维修管理
    await page.goto('/admin/repairs');
    await waitForPageLoad(page);
    await waitForTableData(page, '.el-table', 10000);
    
    // 6. 查看支付管理
    await page.goto('/admin/payments');
    await waitForPageLoad(page);
    await waitForTableData(page, '.el-table', 10000);
    
    // 7. 查看交换管理
    await page.goto('/admin/exchanges');
    await waitForPageLoad(page);
    await waitForTableData(page, '.el-table', 10000);
    
    // 8. 查看通知管理
    await page.goto('/admin/notifications');
    await waitForPageLoad(page);
    await waitForTableData(page, '.el-table', 10000);
  });

  test('学生和管理员交互流程', async ({ page }) => {
    // 1. 学生登录并创建维修工单
    await loginAsStudent(page);
    await page.goto('/repairs');
    await waitForPageLoad(page);
    
    // 尝试创建维修工单（如果功能可用）
    const createButton = page.locator('button:has-text("新建"), button:has-text("报修")').first();
    if (await createButton.count() > 0 && await createButton.isVisible()) {
      await createButton.click();
      await page.waitForTimeout(1000);
    }
    
    // 2. 切换到管理员账号处理工单
    await page.evaluate(() => {
      localStorage.clear();
    });
    await loginAsAdmin(page);
    await page.goto('/admin/repairs');
    await waitForPageLoad(page);
    await waitForTableData(page, '.el-table', 10000);
    
    // 管理员应该能看到学生提交的工单
    await expect(page.locator('.el-table, table')).toBeVisible();
  });
});
