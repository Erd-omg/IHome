import { chromium, FullConfig, request } from '@playwright/test';

async function globalSetup(config: FullConfig) {
  // 确保前端服务正在运行
  const baseURL = config.projects[0]?.use?.baseURL || 'http://localhost:5173';
  const apiURL = process.env.VITE_API_BASE || 'http://localhost:8080/api';
  
  // 检查前端服务
  try {
    const browser = await chromium.launch();
    const page = await browser.newPage();
    await page.goto(baseURL, { timeout: 30000, waitUntil: 'networkidle' });
    await browser.close();
    console.log('✓ 前端服务已就绪:', baseURL);
  } catch (error) {
    console.warn('⚠ 无法连接到前端服务，请确保服务已启动:', baseURL);
    // 不抛出错误，允许测试继续（可能服务会稍后启动）
  }
  
  // 检查后端服务
  try {
    const apiRequest = await request.newContext();
    const response = await apiRequest.post(`${apiURL}/students/login`, {
      data: { id: 'test', password: 'test' },
      timeout: 5000
    });
    // 即使登录失败，只要返回响应就说明服务在运行
    const status = response.status();
    if (status === 200 || status === 401 || status === 400) {
      console.log('✓ 后端服务已就绪:', apiURL);
    } else {
      throw new Error(`后端服务返回异常状态: ${status}`);
    }
    await apiRequest.dispose();
  } catch (error: any) {
    if (error.message?.includes('timeout') || error.message?.includes('ECONNREFUSED')) {
      console.error('✗ 无法连接到后端服务，请确保后端服务已启动:', apiURL);
      console.error('  提示: 在 backend 目录运行 `mvn spring-boot:run` 启动后端服务');
      throw new Error('后端服务未就绪，请先启动后端服务');
    } else {
      throw error;
    }
  }
}

export default globalSetup;

