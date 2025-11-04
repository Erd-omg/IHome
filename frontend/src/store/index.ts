import { createStore } from 'vuex'
import { api } from '../api'

export interface User {
  id: string
  name: string
  userType: 'student' | 'admin'
  [key: string]: any
}

export default createStore({
  state: {
    user: (() => {
      try {
        const userStr = localStorage.getItem('user')
        return userStr ? JSON.parse(userStr) as User : null
      } catch (e) {
        return null
      }
    })(),
    token: localStorage.getItem('token') || '',
    refreshToken: localStorage.getItem('refreshToken') || ''
  },
  mutations: {
    setUser(state, payload: User | null) { 
      state.user = payload
      if (payload) {
        localStorage.setItem('user', JSON.stringify(payload))
      } else {
        localStorage.removeItem('user')
      }
    },
    setToken(state, token: string) { 
      state.token = token
      if (token) {
        localStorage.setItem('token', token)
      } else {
        localStorage.removeItem('token')
      }
    },
    setRefreshToken(state, refreshToken: string) {
      state.refreshToken = refreshToken
      if (refreshToken) {
        localStorage.setItem('refreshToken', refreshToken)
      } else {
        localStorage.removeItem('refreshToken')
      }
    }
  },
  actions: {
    async login({ commit }, { id, password, userType }: { id: string; password: string; userType: 'student' | 'admin' }) {
      try {
        let response
        if (userType === 'student') {
          response = await api.loginStudent(id, password)
        } else {
          response = await api.loginAdmin(id, password)
        }
        
        // 检查响应格式
        if (!response || !response.data) {
          console.error('登录响应格式错误:', response)
          return { 
            success: false, 
            message: '登录响应格式错误' 
          }
        }
        
        // 检查响应数据结构
        // 后端返回格式: { code: 0, message: "success", data: { accessToken, refreshToken, userInfo } }
        const responseData = response.data
        
        // 如果响应中有code字段且不为0，说明登录失败（但这种情况应该被拦截器处理）
        if (responseData.code !== undefined && responseData.code !== 0) {
          console.error('登录失败，code不为0:', responseData)
          return { 
            success: false, 
            message: responseData.message || '登录失败' 
          }
        }
        
        const { data } = responseData
        
        // 检查data是否存在且包含必要字段
        if (!data) {
          console.error('登录响应中data字段为空:', responseData)
          return { 
            success: false, 
            message: '登录响应中data字段为空' 
          }
        }
        
        // 检查必要字段
        if (!data.userInfo) {
          console.error('登录响应中缺少userInfo:', data)
          return { 
            success: false, 
            message: '登录响应中缺少userInfo' 
          }
        }
        
        if (!data.accessToken) {
          console.error('登录响应中缺少accessToken:', data)
          return { 
            success: false, 
            message: '登录响应中缺少accessToken' 
          }
        }
        
        const user: User = {
          id: data.userInfo.id,
          name: data.userInfo.name,
          userType,
          ...data.userInfo
        }
        
        // 从JWT响应中获取token和refreshToken
        const token = data.accessToken
        const refreshToken = data.refreshToken || ''
        
        // 确保token存在
        if (!token) {
          console.error('登录响应中accessToken为空')
          return { 
            success: false, 
            message: '登录响应中accessToken为空' 
          }
        }
        
        // 保存到store和localStorage
        commit('setUser', user)
        commit('setToken', token)
        commit('setRefreshToken', refreshToken)
        
        // 验证是否已保存（使用nextTick确保Vue更新完成）
        // 注意：mutation中的localStorage.setItem是同步的，但为了确保，我们直接检查
        const savedToken = localStorage.getItem('token')
        if (!savedToken || savedToken !== token) {
          console.error('Token保存失败', { expected: token, saved: savedToken })
          // 尝试再次保存
          localStorage.setItem('token', token)
          localStorage.setItem('user', JSON.stringify(user))
          if (refreshToken) {
            localStorage.setItem('refreshToken', refreshToken)
          }
        }
        
        return { success: true, user }
      } catch (error: any) {
        console.error('登录错误:', error)
        // 检查是否是拦截器reject的错误
        if (error.code !== undefined && error.message) {
          // 这是拦截器reject的错误
          return { 
            success: false, 
            message: error.message || '登录失败' 
          }
        }
        return { 
          success: false, 
          message: error.response?.data?.message || error.message || '登录失败' 
        }
      }
    },
    async logout({ commit }) {
      commit('setToken', '')
      commit('setRefreshToken', '')
      commit('setUser', null)
    }
  },
  modules: {}
})


