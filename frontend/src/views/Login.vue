<template>
  <div class="login-wrap">
    <el-card class="card">
      <el-tabs v-model="active">
        <el-tab-pane label="密码登录" name="password">
          <el-form 
            ref="pwdFormRef"
            :model="pwd" 
            :rules="pwdRules"
            label-width="100px"
            @submit.prevent="handlePasswordLogin"
          >
            <el-form-item label="用户类型" prop="userType">
              <el-select v-model="pwd.userType" style="width:240px;">
                <el-option label="学生" value="student" />
                <el-option label="管理员" value="admin" />
              </el-select>
            </el-form-item>
            <el-form-item label="用户名" prop="username">
              <el-input 
                v-model="pwd.username" 
                placeholder="请输入学号/工号"
                :disabled="loading"
              />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input 
                v-model="pwd.password" 
                type="password" 
                placeholder="请输入密码"
                show-password
                :disabled="loading"
                @keyup.enter="handlePasswordLogin"
              />
            </el-form-item>
            <el-form-item>
              <el-checkbox v-model="remember" :disabled="loading">7天免登录</el-checkbox>
              <el-link style="margin-left:auto;" type="primary">忘记密码</el-link>
            </el-form-item>
            <el-form-item>
              <el-button 
                type="primary" 
                style="width:100%" 
                :loading="loading"
                @click="handlePasswordLogin"
              >
                {{ loading ? '登录中...' : '登录' }}
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="验证码登录" name="code">
          <el-form 
            ref="codeFormRef"
            :model="code" 
            :rules="codeRules"
            label-width="100px"
            @submit.prevent="handleCodeLogin"
          >
            <el-form-item label="用户类型" prop="userType">
              <el-select v-model="code.userType" style="width:240px;">
                <el-option label="学生" value="student" />
                <el-option label="管理员" value="admin" />
              </el-select>
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input 
                v-model="code.phone" 
                placeholder="请输入手机号"
                :disabled="loading"
              />
            </el-form-item>
            <el-form-item>
              <el-button 
                :disabled="loading || !isValidPhone(code.phone)"
                @click="sendCode"
              >
                {{ codeCountdown > 0 ? `${codeCountdown}s后重发` : '获取验证码' }}
              </el-button>
            </el-form-item>
            <el-form-item label="验证码" prop="verifyCode">
              <el-input 
                v-model="code.verifyCode" 
                placeholder="请输入验证码"
                :disabled="loading"
                @keyup.enter="handleCodeLogin"
              />
            </el-form-item>
            <el-form-item>
              <el-button 
                type="primary" 
                style="width:100%" 
                :loading="loading"
                @click="handleCodeLogin"
              >
                {{ loading ? '登录中...' : '登录' }}
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
      <div class="footer">IHome.com 版权所有 ICP证：XXXXXXXXXXXX</div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'

const router = useRouter()
const store = useStore()

const active = ref('password')
const loading = ref(false)
const remember = ref(false)
const codeCountdown = ref(0)

// 密码登录表单
const pwdFormRef = ref<FormInstance>()
const pwd = reactive({ 
  userType: 'student' as 'student' | 'admin', 
  username: '', 
  password: '' 
})

// 验证码登录表单
const codeFormRef = ref<FormInstance>()
const code = reactive({ 
  userType: 'student' as 'student' | 'admin', 
  phone: '', 
  verifyCode: '' 
})

// 表单验证规则
const pwdRules: FormRules = {
  userType: [{ required: true, message: '请选择用户类型', trigger: 'change' }],
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ]
}

const codeRules: FormRules = {
  userType: [{ required: true, message: '请选择用户类型', trigger: 'change' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  verifyCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为6位数字', trigger: 'blur' }
  ]
}

// 验证手机号格式
const isValidPhone = (phone: string) => {
  return /^1[3-9]\d{9}$/.test(phone)
}

// 发送验证码
const sendCode = async () => {
  if (!isValidPhone(code.phone)) {
    ElMessage.error('请输入正确的手机号')
    return
  }
  
  try {
    // 这里调用发送验证码的API
    // await api.sendSmsCode(code.phone, code.userType)
    ElMessage.success('验证码已发送')
    
    // 开始倒计时
    codeCountdown.value = 60
    const timer = setInterval(() => {
      codeCountdown.value--
      if (codeCountdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (error: any) {
    ElMessage.error(error.message || '发送验证码失败')
  }
}

// 密码登录
const handlePasswordLogin = async () => {
  if (!pwdFormRef.value) return
  
  try {
    await pwdFormRef.value.validate()
    loading.value = true
    
    const result = await store.dispatch('login', {
      id: pwd.username,
      password: pwd.password,
      userType: pwd.userType
    })
    
    if (result.success) {
      ElMessage.success('登录成功')
      // 根据用户类型跳转到不同页面
      if (result.user.userType === 'admin') {
        router.push('/admin/dashboard')
      } else {
        router.push('/')
      }
    } else {
      ElMessage.error(result.message)
    }
  } catch (error) {
    console.error('登录失败:', error)
  } finally {
    loading.value = false
  }
}

// 验证码登录
const handleCodeLogin = async () => {
  if (!codeFormRef.value) return
  
  try {
    await codeFormRef.value.validate()
    loading.value = true
    
    // 这里调用验证码登录的API
    // const result = await api.loginWithCode(code.phone, code.verifyCode, code.userType)
    
    // 模拟验证码登录成功
    const result = await store.dispatch('login', {
      id: code.phone,
      password: 'mock-password',
      userType: code.userType
    })
    
    if (result.success) {
      ElMessage.success('登录成功')
      if (result.user.userType === 'admin') {
        router.push('/admin/dashboard')
      } else {
        router.push('/')
      }
    } else {
      ElMessage.error(result.message)
    }
  } catch (error) {
    console.error('登录失败:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-wrap { min-height: calc(100vh - 48px); display:flex; align-items:center; justify-content:center; background:#f5f5f5; }
.card { width: 420px; }
.footer { text-align:center; color:#999; margin-top:8px; font-size:12px; }
</style>


