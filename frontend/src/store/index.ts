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
        
        const { data } = response.data
        const user: User = {
          id: data.userInfo.id,
          name: data.userInfo.name,
          userType,
          ...data.userInfo
        }
        
        // 从JWT响应中获取token和refreshToken
        const token = data.accessToken
        const refreshToken = data.refreshToken
        
        commit('setUser', user)
        commit('setToken', token)
        commit('setRefreshToken', refreshToken)
        
        return { success: true, user }
      } catch (error: any) {
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


