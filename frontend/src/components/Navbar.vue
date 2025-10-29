<template>
  <el-header height="64px" class="nav">
    <div class="brand" @click="$router.push('/')">IHome</div>
    <el-menu mode="horizontal" :default-active="active" class="menu" @select="onSelect">
      <el-menu-item index="/">首页</el-menu-item>
      <el-menu-item index="/dorm">我的宿舍</el-menu-item>
      <el-menu-item index="/payments">缴费管理</el-menu-item>
      <el-menu-item index="/repairs">维修管理</el-menu-item>
      <el-menu-item index="/notices">通知公告</el-menu-item>
    </el-menu>
    <div class="right">
      <!-- 通知中心 -->
      <NotificationCenter v-if="user" />
      <el-divider direction="vertical" v-if="user" />
      
      <!-- 用户状态 -->
      <div v-if="user" class="user-info">
        <el-dropdown @command="handleUserCommand">
          <div class="user-dropdown">
            <el-avatar :size="32">
              {{ user.name?.charAt(0) }}
            </el-avatar>
            <span class="user-name">{{ user.name }}</span>
            <el-icon><arrow-down /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人中心</el-dropdown-item>
              <el-dropdown-item command="admin" v-if="user.userType === 'admin'">管理后台</el-dropdown-item>
              <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
      
      <!-- 未登录状态 -->
      <div v-else class="login-section">
        <el-link type="primary" @click="$router.push('/login')">登录</el-link>
      </div>
    </div>
  </el-header>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import NotificationCenter from './NotificationCenter.vue'

const route = useRoute()
const router = useRouter()
const store = useStore()

const active = computed(() => route.path)
const user = computed(() => store.state.user)

function onSelect(path: string) { 
  router.push(path) 
}

function handleUserCommand(command: string) {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'admin':
      router.push('/admin/dashboard')
      break
    case 'logout':
      handleLogout()
      break
  }
}

function handleLogout() {
  store.dispatch('logout')
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<style scoped>
.nav { 
  display:flex; 
  align-items:center; 
  gap:16px; 
  padding:0 24px; 
  background:#fff; 
  border-bottom:1px solid #f0f0f0; 
}

.brand { 
  font-weight:700; 
  color:#d84c6f; 
  cursor:pointer; 
  margin-right:8px; 
  font-size: 20px;
}

.menu { 
  flex:1; 
}

.right { 
  display:flex; 
  align-items:center; 
  gap:12px; 
}

.user-info {
  display: flex;
  align-items: center;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-dropdown:hover {
  background-color: #f5f5f5;
}

.user-name {
  font-size: 14px;
  color: #333;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.login-section {
  display: flex;
  align-items: center;
}
</style>


