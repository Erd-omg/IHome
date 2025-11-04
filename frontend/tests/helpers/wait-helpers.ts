import { Page, expect } from '@playwright/test';

/**
 * 等待元素可见并可交互
 */
export async function waitForElement(page: Page, selector: string, timeout: number = 10000) {
  await page.waitForSelector(selector, { state: 'visible', timeout });
  const element = page.locator(selector).first();
  await expect(element).toBeVisible({ timeout });
  return element;
}

/**
 * 等待页面加载完成
 */
export async function waitForPageLoad(page: Page) {
  await page.waitForLoadState('networkidle');
  await page.waitForLoadState('domcontentloaded');
}

/**
 * 等待API请求完成
 */
export async function waitForApiResponse(page: Page, urlPattern: string | RegExp, timeout: number = 10000) {
  const response = await page.waitForResponse(
    (response) => {
      const url = response.url();
      if (typeof urlPattern === 'string') {
        return url.includes(urlPattern);
      }
      return urlPattern.test(url);
    },
    { timeout }
  );
  return response;
}

/**
 * 等待消息提示出现
 */
export async function waitForMessage(page: Page, messageText?: string, timeout: number = 5000) {
  const messageSelector = '.el-message, .el-notification';
  await waitForElement(page, messageSelector, timeout);
  
  if (messageText) {
    const message = page.locator(messageSelector).filter({ hasText: messageText });
    await expect(message).toBeVisible({ timeout });
  }
  
  // 等待消息自动消失（可选）
  // await page.waitForTimeout(3000);
}

/**
 * 等待表格数据加载
 */
export async function waitForTableData(page: Page, tableSelector: string = '.el-table', timeout: number = 10000) {
  await waitForElement(page, tableSelector, timeout);
  
  // 等待加载状态消失
  const loadingSelector = '.el-loading-mask, .el-loading-spinner';
  await page.waitForSelector(loadingSelector, { state: 'hidden', timeout }).catch(() => {});
  
  // 等待表格行出现或显示"暂无数据"
  await page.waitForFunction(
    ({ selector }) => {
      const table = document.querySelector(selector);
      if (!table) return false;
      const rows = table.querySelectorAll('tbody tr');
      const noData = table.querySelector('.el-table__empty-text');
      return rows.length > 0 || (noData && noData.textContent?.includes('暂无数据'));
    },
    { selector: tableSelector },
    { timeout }
  );
}

/**
 * 等待模态框打开
 */
export async function waitForDialog(page: Page, dialogTitle?: string, timeout: number = 5000) {
  const dialogSelector = '.el-dialog, .el-drawer';
  await waitForElement(page, dialogSelector, timeout);
  
  if (dialogTitle) {
    const title = page.locator('.el-dialog__title, .el-drawer__title').filter({ hasText: dialogTitle });
    await expect(title).toBeVisible({ timeout });
  }
}

/**
 * 等待模态框关闭
 */
export async function waitForDialogClose(page: Page, timeout: number = 5000) {
  await page.waitForSelector('.el-dialog, .el-drawer', { state: 'hidden', timeout }).catch(() => {});
}

/**
 * 等待导航完成
 */
export async function waitForNavigation(page: Page, urlPattern: string | RegExp, timeout: number = 10000) {
  await page.waitForURL(urlPattern, { timeout });
  await waitForPageLoad(page);
}

/**
 * 等待表单验证完成
 */
export async function waitForFormValidation(page: Page, formSelector: string = 'form') {
  // 等待表单验证消息出现或消失
  await page.waitForTimeout(500);
  const errorMessages = page.locator('.el-form-item__error');
  const count = await errorMessages.count();
  return count === 0; // 返回是否有错误
}
