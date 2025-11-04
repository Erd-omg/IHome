import { defineConfig, devices } from '@playwright/test';

/**
 * 阅读 https://playwright.dev/docs/test-configuration 了解更多配置信息
 */
export default defineConfig({
  testDir: './tests',
  
  /* 测试超时时间 */
  timeout: 30 * 1000,
  expect: {
    /**
     * 断言超时时间
     */
    timeout: 5000
  },
  
  /* 并发执行 */
  fullyParallel: true,
  
  /* 失败时停止运行 */
  forbidOnly: !!process.env.CI,
  
  /* CI模式下重试次数 */
  retries: process.env.CI ? 2 : 0,
  
  /* 并发worker数 */
  workers: process.env.CI ? 1 : undefined,
  
  /* 报告配置 */
  reporter: [
    ['html'],
    ['list'],
    ['json', { outputFile: 'test-results/results.json' }]
  ],
  
  /* 共享测试配置 */
  use: {
    /* 基础URL */
    baseURL: process.env.BASE_URL || 'http://localhost:5173',
    
    /* 收集失败时的追踪信息 */
    trace: 'on-first-retry',
    
    /* 截图配置 */
    screenshot: 'only-on-failure',
    
    /* 视频配置 */
    video: 'retain-on-failure',
    
    /* 浏览器上下文选项 */
    viewport: { width: 1280, height: 720 },
    actionTimeout: 10000,
    navigationTimeout: 30000,
  },

  /* 配置测试项目 */
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },

    {
      name: 'firefox',
      use: { ...devices['Desktop Firefox'] },
    },

    {
      name: 'webkit',
      use: { ...devices['Desktop Safari'] },
    },

    /* 移动端测试 */
    {
      name: 'Mobile Chrome',
      use: { ...devices['Pixel 5'] },
    },
    {
      name: 'Mobile Safari',
      use: { ...devices['iPhone 12'] },
    },
  ],

  /* 本地开发服务器配置（如果需要启动服务器） */
  // webServer: {
  //   command: 'npm run dev',
  //   url: 'http://localhost:5173',
  //   reuseExistingServer: !process.env.CI,
  // },
});
