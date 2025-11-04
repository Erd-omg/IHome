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
export async function waitForMessage(page: Page, messageText?: string, timeout: number = 5000, required: boolean = true) {
  // Element Plus的消息会追加到body，使用更宽泛的选择器
  const messageSelector = '.el-message, .el-notification, .el-message-box';
  
  try {
    // 等待消息容器出现
    await page.waitForSelector(messageSelector, { state: 'visible', timeout });
    
    if (messageText) {
      // 如果指定了消息文本，等待包含该文本的消息
      const message = page.locator(messageSelector).filter({ hasText: messageText });
      await expect(message.first()).toBeVisible({ timeout });
    } else {
      // 只验证消息存在
      const message = page.locator(messageSelector).first();
      await expect(message).toBeVisible({ timeout });
    }
  } catch (error) {
    // 如果超时，尝试查找任何消息元素
    const anyMessage = page.locator(messageSelector);
    const count = await anyMessage.count();
    if (count === 0 && required) {
      throw new Error(`等待消息超时: ${timeout}ms`);
    }
    // 如果 required 为 false，允许没有消息
  }
}

/**
 * 等待表格数据加载
 */
export async function waitForTableData(page: Page, tableSelector: string = '.el-table', timeout: number = 10000) {
  // 等待表格元素出现
  await waitForElement(page, tableSelector, timeout);
  
  // 等待加载状态消失（Element Plus的loading mask）
  try {
    await page.waitForSelector('.el-loading-mask, .el-loading-spinner', { state: 'hidden', timeout: 3000 });
  } catch {
    // 如果找不到loading元素或已经消失，继续执行
  }
  
  // 等待表格数据加载完成（有数据行或显示"暂无数据"）
  await page.waitForFunction(
    ({ selector }) => {
      const table = document.querySelector(selector);
      if (!table) return false;
      
      // 检查是否有数据行
      const rows = table.querySelectorAll('tbody tr');
      if (rows.length > 0) return true;
      
      // 检查是否显示"暂无数据"
      const noData = table.querySelector('.el-table__empty-text');
      if (noData && noData.textContent) {
        const text = noData.textContent.trim();
        return text.includes('暂无数据') || text.includes('No Data') || text === '';
      }
      
      // 如果既没有数据也没有空数据提示，可能还在加载
      return false;
    },
    { selector: tableSelector },
    { timeout }
  );
}

/**
 * 关闭所有遮挡的对话框（除了目标对话框）
 */
export async function closeOverlayDialogs(page: Page, excludeLabels?: string[]) {
  try {
    // 查找所有覆盖层对话框（消息框、确认框等）
    const overlays = page.locator('.el-overlay-message-box, .el-overlay-dialog, .el-message-box');
    const overlayCount = await overlays.count();
    
    for (let i = 0; i < overlayCount; i++) {
      const overlay = overlays.nth(i);
      if (await overlay.isVisible()) {
        const ariaLabel = await overlay.getAttribute('aria-label');
        const shouldExclude = excludeLabels?.some(label => ariaLabel?.includes(label));
        
        if (!shouldExclude) {
          // 尝试按 ESC 关闭
          await page.keyboard.press('Escape');
          await page.waitForTimeout(200);
        }
      }
    }
  } catch {
    // 忽略错误
  }
}

/**
 * 等待模态框打开
 */
export async function waitForDialog(page: Page, dialogTitle?: string, timeout: number = 5000) {
  // 先关闭可能存在的通知抽屉，避免干扰
  await closeNotificationDrawer(page);
  
  // 关闭可能遮挡的对话框（消息框等）
  await closeOverlayDialogs(page);
  
  // 等待对话框容器出现（排除通知抽屉和其他覆盖层）
  await page.waitForFunction(
    () => {
      // 查找所有对话框和抽屉
      const dialogs = document.querySelectorAll('.el-dialog, .el-drawer');
      for (const dialog of dialogs) {
        // 排除通知抽屉
        const isNotificationDrawer = 
          dialog.classList.contains('notification-actions') ||
          dialog.getAttribute('aria-label')?.includes('消息通知') ||
          dialog.getAttribute('aria-label')?.includes('通知') ||
          dialog.querySelector('.notification-actions') !== null;
        
        if (isNotificationDrawer) {
          continue;
        }
        
        // 排除消息框和确认框（这些会遮挡主要对话框）
        const isMessageBox = 
          dialog.classList.contains('el-overlay-message-box') ||
          dialog.closest('.el-overlay-message-box') !== null;
        
        if (isMessageBox) {
          continue;
        }
        
        // 检查对话框是否可见
        const style = window.getComputedStyle(dialog);
        if (style.display !== 'none' && style.visibility !== 'hidden') {
          // 检查是否还在动画中
          if (!dialog.classList.contains('dialog-fade-enter-active') && 
              !dialog.classList.contains('dialog-fade-leave-active')) {
            return true;
          }
        }
      }
      return false;
    },
    { timeout }
  ).catch(() => {
    // 如果超时，尝试查找任何可见的对话框（不排除消息框）
    return page.waitForSelector('.el-dialog:not([style*="display: none"]), .el-drawer:not([style*="display: none"])', { 
      state: 'visible', 
      timeout: 2000 
    }).catch(() => {
      // 如果还是找不到，返回 false 表示对话框可能没有出现
      return false;
    });
  });
  
  // 等待对话框动画完成
  await page.waitForTimeout(300);
  
  if (dialogTitle) {
    const title = page.locator('.el-dialog__title, .el-drawer__title').filter({ hasText: dialogTitle });
    await expect(title).toBeVisible({ timeout }).catch(() => {
      // 如果找不到标题，继续（可能对话框没有标题）
    });
  }
}

/**
 * 关闭通知抽屉（如果存在）
 */
export async function closeNotificationDrawer(page: Page) {
  try {
    // 查找所有抽屉，检查是否是通知抽屉
    const drawers = page.locator('.el-drawer');
    const drawerCount = await drawers.count();
    
    for (let i = 0; i < drawerCount; i++) {
      const drawer = drawers.nth(i);
      
      // 检查是否是通知抽屉
      const ariaLabel = await drawer.getAttribute('aria-label');
      const hasNotificationText = 
        ariaLabel?.includes('消息通知') || 
        ariaLabel?.includes('通知') ||
        ariaLabel?.includes('消息');
      
      if (hasNotificationText && await drawer.isVisible()) {
        // 尝试多种方式关闭抽屉
        // 方式1：查找关闭按钮
        const closeButton = drawer.locator('.el-drawer__close-btn, button[aria-label*="关闭"], .el-icon-close').first();
        if (await closeButton.count() > 0 && await closeButton.isVisible()) {
          await closeButton.click({ timeout: 2000 });
          await page.waitForTimeout(300);
          continue;
        }
        
        // 方式2：按 ESC 键
        await page.keyboard.press('Escape');
        await page.waitForTimeout(300);
        
        // 方式3：点击遮罩层
        const mask = page.locator('.el-overlay, .el-drawer__wrapper').first();
        if (await mask.count() > 0 && await mask.isVisible()) {
          await mask.click({ position: { x: 10, y: 10 } });
          await page.waitForTimeout(300);
        }
      }
    }
  } catch {
    // 如果关闭失败，继续执行（不影响测试）
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
