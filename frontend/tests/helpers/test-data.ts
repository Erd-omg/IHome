/**
 * 测试数据常量
 */
export const TEST_DATA = {
  // 学生信息
  student: {
    id: '2024099',
    name: '测试学生',
    password: 'test123456',
    email: 'test@example.com',
    phone: '13800138000',
    gender: '男',
    major: '软件工程',
    grade: '2024',
    class: '2024-01'
  },
  
  // 宿舍信息
  dormitory: {
    buildingId: '1',
    buildingName: '测试楼',
    roomNumber: '101',
    capacity: 4,
    floor: 1,
    description: '测试宿舍'
  },
  
  // 维修工单
  repair: {
    title: '测试维修工单',
    description: '这是一个测试维修工单的描述',
    category: '水电',
    urgency: 'normal'
  },
  
  // 支付记录
  payment: {
    type: '水电费',
    amount: 100.00,
    description: '测试支付记录',
    month: '2024-01'
  },
  
  // 通知
  notification: {
    title: '测试通知',
    content: '这是一条测试通知内容',
    type: 'system',
    priority: 'normal'
  },
  
  // 宿舍交换申请
  exchange: {
    reason: '测试交换原因',
    targetDormitoryId: '2',
    targetBedId: '5'
  },
  
  // 生活方式标签
  lifestyleTags: {
    sleepSchedule: '早睡早起',
    noiseLevel: '安静',
    cleanliness: '整洁',
    social: '内向',
    study: '喜欢在宿舍学习',
    smoking: '不吸烟',
    pet: '不喜欢宠物'
  }
};

/**
 * 生成随机字符串
 */
export function randomString(length: number = 8): string {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
  let result = '';
  for (let i = 0; i < length; i++) {
    result += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  return result;
}

/**
 * 生成随机数字
 */
export function randomNumber(min: number = 1000, max: number = 9999): number {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}
