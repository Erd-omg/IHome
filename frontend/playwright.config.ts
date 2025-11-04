import { defineConfig, devices } from '@playwright/test';
import { fileURLToPath } from 'url';
import path from 'path';

/**
 * 阅读 https://playwright.dev/docs/test-configuration 了解更多配置信息
 */
export default defineConfig({
  testDir: './tests',
  
  /* 全局设置 */
  globalSetup: path.resolve(path.dirname(fileURLToPath(import.meta.url)), 'tests/global-setup.ts'),
  
        /* 测试超时时间 */
        timeout: 90 * 1000, // 增加到90秒以避免登录超时
  expect: {
    /**
     * 断言超时时间
     */
    timeout: 10000
  },
  
  /* 并发执行 */
  fullyParallel: false, // 改为false以避免测试间的相互干扰（如localStorage、路由状态等）
  
  /* 失败时停止运行 */
  forbidOnly: !!process.env.CI,
  
  /* CI模式下重试次数 */
  retries: process.env.CI ? 2 : 0,
  
  /* 并发worker数 - 减少并发以避免测试间冲突 */
  workers: process.env.CI ? 1 : 2,
  
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
    actionTimeout: 15000,
    navigationTimeout: 60000,
  },

  /* 配置测试项目 - 支持所有浏览器 */
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
      use: { 
        ...devices['Desktop Safari'],
        // WebKit 在 Windows 上录制视频有问题，禁用视频录制
        video: 'off',
      },
    },
  ],

  /* 本地开发服务器配置（如果需要启动服务器） */
  // webServer: {
  //   command: 'npm run dev',
  //   url: 'http://localhost:5173',
  //   reuseExistingServer: !process.env.CI,
  // },
});
