import Mock from 'mockjs'

// Delay
Mock.setup({ timeout: '300-800' })

// 生成分页数据的辅助函数
const generatePaginatedData = (data: any[], page: number, size: number) => {
  const start = (page - 1) * size
  const end = start + size
  const content = data.slice(start, end)
  
  return {
    content,
    totalElements: data.length,
    totalPages: Math.ceil(data.length / size),
    size,
    number: page - 1,
    first: page === 1,
    last: page === Math.ceil(data.length / size)
  }
}

// 生成模拟数据
const generateMockData = () => {
  const payments = Mock.mock({
    'list|50': [{
      id: '@increment',
      studentId: '@pick(["2024001", "2024002", "2024003", "2024004", "2024005"])',
      amount: '@float(100, 2000, 2, 2)',
      paymentMethod: '@pick(["住宿费", "电费", "水费", "其他"])',
      paymentTime: '@datetime'
    }]
  }).list

  const repairs = Mock.mock({
    'list|30': [{
      id: '@increment',
      studentId: '@pick(["2024001", "2024002", "2024003", "2024004", "2024005"])',
      dormitoryId: '@pick(["D01-101", "D01-102", "D02-201", "D02-202"])',
      repairType: '@pick(["水电维修", "空调维修", "家具维修", "其他"])',
      description: '@cparagraph(1, 3)',
      urgencyLevel: '@pick(["一般", "紧急", "不紧急"])',
      status: '@pick(["待处理", "处理中", "已完成"])',
      createdAt: '@datetime'
    }]
  }).list

  const dormitories = Mock.mock({
    'list|20': [{
      id: '@pick(["D01-101", "D01-102", "D01-103", "D02-201", "D02-202", "D02-203"])',
      buildingId: '@pick(["A01", "B01", "C01"])',
      name: '@pick(["101室", "102室", "103室", "201室", "202室", "203室"])',
      bedCount: '@pick([2, 4, 6])',
      currentOccupancy: function() {
        return Math.floor(Math.random() * (this.bedCount + 1))
      },
      status: function() {
        if (this.currentOccupancy === 0) return '可用'
        if (this.currentOccupancy === this.bedCount) return '已满'
        return '可用'
      }
    }]
  }).list

  return { payments, repairs, dormitories }
}

const mockData = generateMockData()

// 学生登录
Mock.mock('/api/students/login', 'post', (options: any) => {
  const body = JSON.parse(options.body)
  if (body.id && body.password) {
    return { 
      code: 0, 
      message: 'success', 
      data: { 
        id: body.id, 
        name: '张三',
        token: 'mock-student-token',
        refreshToken: 'mock-student-refresh-token'
      } 
    }
  }
  return { code: -1, message: '学号或密码错误', data: null }
})

// 管理员登录
Mock.mock('/api/admin/login', 'post', (options: any) => {
  const body = JSON.parse(options.body)
  if (body.id && body.password) {
    return { 
      code: 0, 
      message: 'success', 
      data: { 
        id: body.id, 
        name: '管理员',
        role: '系统管理员',
        token: 'mock-admin-token',
        refreshToken: 'mock-admin-refresh-token'
      } 
    }
  }
  return { code: -1, message: '工号或密码错误', data: null }
})

// 缴费管理
Mock.mock(/\/api\/payments$/, 'post', {
  code: 0,
  message: 'success',
  data: { id: '@increment' }
})

Mock.mock(/\/api\/payments/, 'get', (options: any) => {
  const url = new URL(options.url, 'http://localhost')
  const page = parseInt(url.searchParams.get('page') || '1')
  const size = parseInt(url.searchParams.get('size') || '10')
  const studentId = url.searchParams.get('studentId')
  const paymentMethod = url.searchParams.get('paymentMethod')
  const sort = url.searchParams.get('sort') // e.g. amount,asc
  
  let filteredData = mockData.payments
  
  if (studentId) {
    filteredData = filteredData.filter((item: any) => item.studentId === studentId)
  }
  if (paymentMethod) {
    filteredData = filteredData.filter((item: any) => item.paymentMethod === paymentMethod)
  }
  if (sort) {
    const [field, order] = sort.split(',')
    filteredData = filteredData.slice().sort((a: any, b: any) => {
      const av = a[field]
      const bv = b[field]
      if (av === bv) return 0
      const res = av > bv ? 1 : -1
      return order === 'desc' ? -res : res
    })
  }
  
  const paginatedData = generatePaginatedData(filteredData, page, size)
  
  return {
    code: 0,
    message: 'success',
    data: paginatedData
  }
})

// 维修管理
Mock.mock('/api/repairs', 'post', { 
  code: 0, 
  message: 'success', 
  data: { id: '@increment' } 
})

Mock.mock(/\/api\/repairs/, 'get', (options: any) => {
  const url = new URL(options.url, 'http://localhost')
  const page = parseInt(url.searchParams.get('page') || '1')
  const size = parseInt(url.searchParams.get('size') || '10')
  const studentId = url.searchParams.get('studentId')
  const dormitoryId = url.searchParams.get('dormitoryId')
  const repairType = url.searchParams.get('repairType')
  const status = url.searchParams.get('status')
  const sort = url.searchParams.get('sort')
  
  let filteredData = mockData.repairs
  
  if (studentId) {
    filteredData = filteredData.filter((item: any) => item.studentId === studentId)
  }
  if (dormitoryId) {
    filteredData = filteredData.filter((item: any) => item.dormitoryId === dormitoryId)
  }
  if (repairType) {
    filteredData = filteredData.filter((item: any) => item.repairType === repairType)
  }
  if (status) {
    filteredData = filteredData.filter((item: any) => item.status === status)
  }
  if (sort) {
    const [field, order] = sort.split(',')
    filteredData = filteredData.slice().sort((a: any, b: any) => {
      const av = a[field]
      const bv = b[field]
      if (av === bv) return 0
      const res = av > bv ? 1 : -1
      return order === 'desc' ? -res : res
    })
  }
  
  const paginatedData = generatePaginatedData(filteredData, page, size)
  
  return {
    code: 0,
    message: 'success',
    data: paginatedData
  }
})

// 维修状态更新
Mock.mock(/\/api\/repairs\/.*\/status/, 'put', {
  code: 0,
  message: 'success',
  data: null
})

// 宿舍管理
Mock.mock(/\/api\/dorms/, 'get', (options: any) => {
  const url = new URL(options.url, 'http://localhost')
  const page = parseInt(url.searchParams.get('page') || '1')
  const size = parseInt(url.searchParams.get('size') || '10')
  const buildingId = url.searchParams.get('buildingId')
  const status = url.searchParams.get('status')
  const name = url.searchParams.get('name')
  const sort = url.searchParams.get('sort')
  
  let filteredData = mockData.dormitories
  
  if (buildingId) {
    filteredData = filteredData.filter((item: any) => item.buildingId === buildingId)
  }
  if (status) {
    filteredData = filteredData.filter((item: any) => item.status === status)
  }
  if (name) {
    filteredData = filteredData.filter((item: any) => item.id.includes(name))
  }
  if (sort) {
    const [field, order] = sort.split(',')
    filteredData = filteredData.slice().sort((a: any, b: any) => {
      const av = a[field]
      const bv = b[field]
      if (av === bv) return 0
      const res = av > bv ? 1 : -1
      return order === 'desc' ? -res : res
    })
  }
  
  const paginatedData = generatePaginatedData(filteredData, page, size)
  
  return {
    code: 0,
    message: 'success',
    data: paginatedData
  }
})

// 宿舍床位查询
Mock.mock(/\/api\/dorms\/.*\/beds/, 'get', {
  code: 0,
  message: 'success',
  data: Mock.mock({
    'list|4': [{
      id: '@pick(["BED-D01-101-1", "BED-D01-101-2", "BED-D01-101-3", "BED-D01-101-4"])',
      dormitoryId: 'D01-101',
      bedNumber: '@increment',
      type: '@pick(["上铺", "下铺"])',
      status: '@pick(["可用", "已占用"])'
    }]
  }).list
})

// 选择床位
Mock.mock('/api/dorms/choose-bed', 'post', {
  code: 0,
  message: 'success',
  data: null
})

// 退宿
Mock.mock('/api/dorms/checkout', 'post', {
  code: 0,
  message: 'success',
  data: null
})

// 住宿分配
Mock.mock(/\/api\/allocations\/student\/.*/, 'get', {
  code: 0,
  message: 'success',
  data: Mock.mock({
    id: '@increment',
    studentId: '2024001',
    bedId: 'BED-D01-101-1',
    checkInDate: '@datetime',
    checkOutDate: null,
    status: '在住'
  })
})

// 住宿历史
Mock.mock(/\/api\/allocations\/student\/.*\/history/, 'get', {
  code: 0,
  message: 'success',
  data: Mock.mock({
    'list|3': [{
      id: '@increment',
      studentId: '2024001',
      bedId: 'BED-D01-101-1',
      checkInDate: '@datetime',
      checkOutDate: '@datetime',
      status: '@pick(["已退宿", "在住"])'
    }]
  }).list
})

// 问卷提交
Mock.mock('/api/questionnaire/submit', 'post', {
  code: 0,
  message: 'success',
  data: null
})

// 问卷查询
Mock.mock(/\/api\/questionnaire\/student\/.*/, 'get', {
  code: 0,
  message: 'success',
  data: {
    answers: [{
      id: '@increment',
      studentId: '2024001',
      sleepTimePreference: '@pick(["早睡", "晚睡", "不固定"])',
      cleanlinessLevel: '@pick(["爱整洁", "一般", "随意"])',
      noiseTolerance: '@pick(["安静", "能接受一点噪音", "无所谓"])',
      eatingInRoom: '@pick(["经常", "偶尔", "从不"])',
      collectiveSpendingHabit: '@pick(["愿意", "一般", "不愿意"])'
    }],
    tags: ['安静', '整洁', '不吸烟']
  }
})

// 管理员仪表盘
Mock.mock('/api/admin/dashboard', 'get', {
  code: 0,
  message: 'success',
  data: {
    totalStudents: 150,
    totalDormitories: 50,
    totalBeds: 200,
    occupiedBeds: 120,
    totalPayments: 500,
    totalRepairs: 30,
    pendingRepairs: 5
  }
})

// 公告列表与详情
Mock.mock(/\/api\/notices$/, 'get', (options: any) => {
  const url = new URL(options.url, 'http://localhost')
  const page = parseInt(url.searchParams.get('page') || '1')
  const size = parseInt(url.searchParams.get('size') || '10')
  const notices = Mock.mock({
    'list|50': [{
      id: '@increment',
      title: '【公告】@ctitle(8, 16)',
      publishTime: '@datetime',
      content: '@cparagraph(4, 10)'
    }]
  }).list
  const paginatedData = generatePaginatedData(notices, page, size)
  return { code: 0, message: 'success', data: paginatedData }
})

Mock.mock(/\/api\/notices\/\d+$/, 'get', (options: any) => {
  const id = options.url.split('/').pop()
  return {
    code: 0,
    message: 'success',
    data: Mock.mock({
      id,
      title: '【公告】@ctitle(8, 16)',
      publishTime: '@datetime',
      content: '@cparagraph(6, 16)'
    })
  }
})

// 管理员学生列表
Mock.mock(/\/api\/admin\/students/, 'get', (options: any) => {
  const url = new URL(options.url, 'http://localhost')
  const page = parseInt(url.searchParams.get('page') || '1')
  const size = parseInt(url.searchParams.get('size') || '10')
  const sort = url.searchParams.get('sort')
  
  const students = Mock.mock({
    'list|50': [{
      id: '@pick(["2024001", "2024002", "2024003", "2024004", "2024005"])',
      name: '@cname',
      phoneNumber: '@phone',
      email: '@email',
      gender: '@pick(["男", "女"])',
      college: '@pick(["计算机学院", "机械学院", "经管学院"])',
      major: '@pick(["软件工程", "计算机科学", "机械工程", "工商管理"])',
      password: null
    }]
  }).list
  
  let data = students
  if (sort) {
    const [field, order] = sort.split(',')
    data = data.slice().sort((a: any, b: any) => {
      const av = a[field]
      const bv = b[field]
      if (av === bv) return 0
      const res = av > bv ? 1 : -1
      return order === 'desc' ? -res : res
    })
  }
  const paginatedData = generatePaginatedData(data, page, size)
  
  return {
    code: 0,
    message: 'success',
    data: paginatedData
  }
})

// 管理员宿舍列表
Mock.mock(/\/api\/admin\/dormitories/, 'get', (options: any) => {
  const url = new URL(options.url, 'http://localhost')
  const page = parseInt(url.searchParams.get('page') || '1')
  const size = parseInt(url.searchParams.get('size') || '10')
  const sort = url.searchParams.get('sort')
  let data = mockData.dormitories
  if (sort) {
    const [field, order] = sort.split(',')
    data = data.slice().sort((a: any, b: any) => {
      const av = a[field]
      const bv = b[field]
      if (av === bv) return 0
      const res = av > bv ? 1 : -1
      return order === 'desc' ? -res : res
    })
  }
  const paginatedData = generatePaginatedData(data, page, size)
  
  return {
    code: 0,
    message: 'success',
    data: paginatedData
  }
})

// 管理员住宿分配列表
Mock.mock(/\/api\/admin\/allocations/, 'get', (options: any) => {
  const url = new URL(options.url, 'http://localhost')
  const page = parseInt(url.searchParams.get('page') || '1')
  const size = parseInt(url.searchParams.get('size') || '10')
  const sort = url.searchParams.get('sort')
  
  const allocations = Mock.mock({
    'list|30': [{
      id: '@increment',
      studentId: '@pick(["2024001", "2024002", "2024003", "2024004", "2024005"])',
      bedId: '@pick(["BED-D01-101-1", "BED-D01-101-2", "BED-D02-201-1", "BED-D02-201-2"])',
      checkInDate: '@datetime',
      checkOutDate: '@pick([null, "@datetime"])',
      status: '@pick(["在住", "已退宿"])'
    }]
  }).list
  
  let data = allocations
  if (sort) {
    const [field, order] = sort.split(',')
    data = data.slice().sort((a: any, b: any) => {
      const av = a[field]
      const bv = b[field]
      if (av === bv) return 0
      const res = av > bv ? 1 : -1
      return order === 'desc' ? -res : res
    })
  }
  const paginatedData = generatePaginatedData(data, page, size)
  
  return {
    code: 0,
    message: 'success',
    data: paginatedData
  }
})

// Token刷新
Mock.mock('/api/auth/refresh', 'post', {
  code: 0,
  message: 'success',
  data: {
    token: 'new-mock-token',
    refreshToken: 'new-mock-refresh-token'
  }
})

// 宿舍调换（学生申请；管理员审核在后续实现）
const switchPool: any[] = []
Mock.mock('/api/switches', 'post', (options: any) => {
  const body = JSON.parse(options.body)
  const item = { id: switchPool.length + 1, status: '未处理', createdAt: Mock.mock('@datetime'), ...body }
  switchPool.unshift(item)
  return { code: 0, message: 'success', data: item }
})

Mock.mock(/\/api\/switches(\?.*)?$/, 'get', (options: any) => {
  const url = new URL(options.url, 'http://localhost')
  const page = parseInt(url.searchParams.get('page') || '1')
  const size = parseInt(url.searchParams.get('size') || '10')
  const studentId = url.searchParams.get('studentId')
  const status = url.searchParams.get('status')
  let data = switchPool
  if (studentId) data = data.filter((x: any) => x.studentId === studentId)
  if (status) data = data.filter((x: any) => x.status === status)
  const paginated = generatePaginatedData(data, page, size)
  return { code: 0, message: 'success', data: paginated }
})

// 管理端调换审核列表
Mock.mock(/\/api\/admin\/exchanges(\?.*)?$/, 'get', (options: any) => {
  const url = new URL(options.url, 'http://localhost')
  const page = parseInt(url.searchParams.get('page') || '1')
  const size = parseInt(url.searchParams.get('size') || '10')
  const status = url.searchParams.get('status')
  let data = switchPool
  if (status) data = data.filter((x: any) => x.status === status)
  const paginated = generatePaginatedData(data, page, size)
  return { code: 0, message: 'success', data: paginated }
})

Mock.mock(/\/api\/admin\/exchanges\/\d+\/status$/, 'put', (options: any) => {
  const id = Number(options.url.match(/exchanges\/(\d+)\/status/)?.[1])
  const url = new URL(options.url, 'http://localhost')
  const status = url.searchParams.get('status') || '未处理'
  const item = switchPool.find((x: any) => x.id === id)
  if (item) item.status = status
  return { code: 0, message: 'success', data: item || null }
})

export {}




