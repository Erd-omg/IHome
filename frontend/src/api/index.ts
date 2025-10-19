import http from './http'

// 分页参数接口
export interface PaginationParams {
  page?: number
  size?: number
  [key: string]: any
}

// 分页响应接口
export interface PaginatedResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
}

export const api = {
  // auth
  loginStudent: (id: string, password: string) => http.post('/students/login', { id, password }),
  loginAdmin: (id: string, password: string) => http.post('/admin/login', { id, password }),
  registerStudent: (payload: any) => http.post('/students/register', payload),
  getStudent: (id: string) => http.get(`/students/${id}`),
  updateStudent: (id: string, payload: any) => http.put(`/students/${id}`, payload),

  // dorms
  listDorms: (params?: PaginationParams) => http.get('/dorms', { params }),
  listBeds: (dormitoryId: string, params?: PaginationParams) => http.get(`/dorms/${dormitoryId}/beds`, { params }),
  chooseBed: (studentId: string, bedId: string) => http.post('/dorms/choose-bed', null, { params: { studentId, bedId } }),
  checkout: (studentId: string) => http.post('/dorms/checkout', null, { params: { studentId } }),

  // payments
  createPayment: (payload: any) => http.post('/payments', payload),
  studentPayments: (studentId: string, params?: PaginationParams) => http.get(`/payments/student/${studentId}`, { params }),
  listPayments: (params?: PaginationParams) => http.get('/payments', { params }),
  updatePaymentStatus: (id: string, status: string) => http.put(`/payments/${id}/status`, null, { params: { status } }),

  // repairs
  createRepair: (payload: any) => http.post('/repairs', payload),
  studentRepairs: (studentId: string, params?: PaginationParams) => http.get(`/repairs/student/${studentId}`, { params }),
  listRepairs: (params?: PaginationParams) => http.get('/repairs', { params }),
  updateRepairStatus: (id: string, status: string) => http.put(`/repairs/${id}/status`, null, { params: { status } }),

  // questionnaire
  submitQuestionnaire: (payload: any) => http.post('/questionnaire/submit', payload),
  getQuestionnaire: (studentId: string) => http.get(`/questionnaire/student/${studentId}`),

  // admin dashboard
  dashboard: () => http.get('/admin/dashboard'),
  adminStudents: (params?: PaginationParams) => http.get('/admin/students', { params }),
  adminDormitories: (params?: PaginationParams) => http.get('/admin/dormitories', { params }),
  adminAllocations: (params?: PaginationParams) => http.get('/admin/allocations', { params }),
  adminNotifications: (params?: PaginationParams) => http.get('/admin/notifications', { params }),
  adminRepairs: (params?: PaginationParams) => http.get('/admin/repairs', { params }),
  adminPayments: (params?: PaginationParams) => http.get('/admin/payments', { params }),
  adminExchanges: (params?: PaginationParams) => http.get('/admin/exchanges', { params }),

  // admin CRUD operations
  // 学生管理
  createStudent: (student: any) => http.post('/admin/students', student),
  updateStudentAdmin: (id: string, student: any) => http.put(`/admin/students/${id}`, student),
  deleteStudent: (id: string) => http.delete(`/admin/students/${id}`),
  
  // 宿舍管理
  createDormitory: (dormitory: any) => http.post('/admin/dormitories', dormitory),
  updateDormitory: (id: string, dormitory: any) => http.put(`/admin/dormitories/${id}`, dormitory),
  deleteDormitory: (id: string) => http.delete(`/admin/dormitories/${id}`),
  
  // 调换申请管理
  updateExchangeStatus: (id: number, status: string, reviewComment?: string) => 
    http.put(`/admin/exchanges/${id}/status`, null, { params: { status, reviewComment } }),
  deleteExchange: (id: number) => http.delete(`/admin/exchanges/${id}`),
  
  // 维修管理
  updateRepairStatusAdmin: (id: number, status: string) => 
    http.put(`/admin/repairs/${id}/status`, null, { params: { status } }),
  deleteRepair: (id: number) => http.delete(`/admin/repairs/${id}`),

  // notices
  listNotices: (params?: PaginationParams) => http.get('/notices', { params }),
  getNotice: (id: string | number) => http.get(`/notices/${id}`)
  ,
  // switch (dorm exchange) requests
  createSwitch: (payload: any) => http.post('/switches/apply', payload),
  listMySwitches: (studentId: string, params?: PaginationParams) => http.get('/switches/my-requests', { params: { ...params, studentId } })
  ,
  // admin - exchanges review
  adminListExchanges: (params?: PaginationParams) => http.get('/admin/exchanges', { params }),
  adminUpdateExchangeStatus: (id: string | number, status: string) => http.put(`/admin/exchanges/${id}/status`, null, { params: { status } }),

  // allocations
  getCurrentAllocation: (studentId: string) => http.get(`/students/${studentId}/current-allocation`),
  getAllocationHistory: (studentId: string) => http.get(`/students/${studentId}/allocations`),
  
  // student notifications
  getStudentNotifications: (studentId: string) => http.get(`/students/${studentId}/notifications`),

  // notifications
  getNotifications: (receiverId: string, receiverType: string) => http.get('/notifications/list', { params: { receiverId, receiverType } }),
  getUnreadNotifications: (receiverId: string, receiverType: string) => http.get('/notifications/unread', { params: { receiverId, receiverType } }),
  getUnreadCount: (receiverId: string, receiverType: string) => http.get('/notifications/unread-count', { params: { receiverId, receiverType } }),
  markAsRead: (notificationId: number, receiverId: string, receiverType: string) => http.post(`/notifications/${notificationId}/read`, null, { params: { receiverId, receiverType } }),
  markAllAsRead: (receiverId: string, receiverType: string) => http.post('/notifications/mark-all-read', null, { params: { receiverId, receiverType } }),
  deleteNotification: (notificationId: number, receiverId: string, receiverType: string) => http.delete(`/notifications/${notificationId}`, { params: { receiverId, receiverType } }),
  
  // admin notifications
  createNotification: (notification: any) => http.post('/notifications/create', notification),
  sendSystemNotification: (request: any) => http.post('/notifications/send-system', request),
  cleanExpiredNotifications: () => http.post('/notifications/clean-expired')
}

export default api




