import axios, { AxiosResponse } from 'axios'
import store from '../store'
import { ElMessage } from 'element-plus'
import router from '../router'

const http = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 是否正在刷新token
let isRefreshing = false
// 等待刷新token的请求队列
let failedQueue: Array<{
  resolve: (value?: any) => void
  reject: (error?: any) => void
}> = []

// 处理队列中的请求
const processQueue = (error: any, token: string | null = null) => {
  failedQueue.forEach(({ resolve, reject }) => {
    if (error) {
      reject(error)
    } else {
      resolve(token)
    }
  })
  
  failedQueue = []
}

http.interceptors.request.use((config) => {
  const token = store.state.token
  if (token) {
    config.headers = config.headers || {}
    ;(config.headers as any)['Authorization'] = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (res: AxiosResponse) => {
    const data = res.data
    if (data && data.code !== undefined && data.code !== 0) {
      ElMessage.error(data.message || '请求失败')
      return Promise.reject(data)
    }
    return res
  },
  async (err) => {
    const originalRequest = err.config

    // 处理401错误（未授权）
    if (err.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        // 如果正在刷新token，将请求加入队列
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject })
        }).then(() => {
          return http(originalRequest)
        }).catch(err => {
          return Promise.reject(err)
        })
      }

      originalRequest._retry = true
      isRefreshing = true

      try {
        // 尝试刷新token
        const refreshToken = localStorage.getItem('refreshToken')
        if (refreshToken) {
          const response = await http.post('/auth/refresh', {
            refreshToken
          })
          
          const { accessToken: newToken, refreshToken: newRefreshToken } = response.data.data
          store.commit('setToken', newToken)
          store.commit('setRefreshToken', newRefreshToken)
          
          // 处理队列中的请求
          processQueue(null, newToken)
          
          // 重试原请求
          originalRequest.headers.Authorization = `Bearer ${newToken}`
          return http(originalRequest)
        } else {
          // 没有refreshToken，直接跳转登录
          throw new Error('No refresh token')
        }
      } catch (refreshError) {
        // 刷新失败，清除token并跳转登录
        processQueue(refreshError, null)
        store.dispatch('logout')
        router.push('/login')
        ElMessage.error('登录已过期，请重新登录')
        return Promise.reject(refreshError)
      } finally {
        isRefreshing = false
      }
    }

    // 其他错误
    const errorMessage = err.response?.data?.message || err.message || '网络错误'
    ElMessage.error(errorMessage)
    return Promise.reject(err)
  }
)

export default http




