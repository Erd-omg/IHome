<template>
  <el-container style="min-height:100vh;">
    <el-aside width="220px" style="background:var(--el-color-primary); color:#fff;">
      <div style="height:48px; display:flex; align-items:center; padding:0 16px; font-weight:600;">
        IHome 管理端
      </div>
      <el-menu :default-active="active" background-color="var(--el-color-primary)" text-color="#ffe8ee" active-text-color="#fff" router>
        <el-menu-item index="/admin/dashboard">
          <i class="el-icon-data-analysis"></i>
          <span style="margin-left: 8px;">仪表盘</span>
        </el-menu-item>
        <el-menu-item index="/admin/students">
          <i class="el-icon-user"></i>
          <span style="margin-left: 8px;">学生管理</span>
        </el-menu-item>
        <el-sub-menu index="dormitory">
          <template #title>
            <i class="el-icon-office-building"></i>
            <span style="margin-left: 8px;">宿舍管理</span>
          </template>
          <el-menu-item index="/admin/dormitories"><i class="el-icon-house" />宿舍信息</el-menu-item>
          <el-menu-item index="/admin/allocations"><i class="el-icon-s-operation" />分配记录</el-menu-item>
          <el-menu-item index="/admin/exchanges"><i class="el-icon-s-claim" />调换审核</el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/admin/notifications">
          <i class="el-icon-bell"></i>
          <span style="margin-left: 8px;">通知管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/repairs">
          <i class="el-icon-wrench"></i>
          <span style="margin-left: 8px;">维修管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/payments">
          <i class="el-icon-money"></i>
          <span style="margin-left: 8px;">缴费管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header height="56px" style="display:flex; align-items:center; padding:0 16px; background:#fff; border-bottom:1px solid #eee;">
        <div style="font-weight:600;">IHome 后台管理</div>
        <div style="margin-left:auto; display:flex; align-items:center; gap:12px;">
          <div v-if="user" style="color:#666;">
            已登录：<strong>{{ user.name }}</strong>
            <el-tag size="small" type="success" style="margin-left:8px;">{{ user.userType === 'admin' ? '管理员' : '用户' }}</el-tag>
          </div>
          <el-button text @click="logout">退出登录</el-button>
        </div>
      </el-header>
      <el-main style="background:#f5f6f8;">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useStore } from 'vuex'

const route = useRoute()
const router = useRouter()
const store = useStore()

const active = computed(() => route.path)
const user = computed(() => store.state.user)

function logout() {
  store.dispatch('logout')
  router.push('/login')
}
</script>

<style scoped>
.el-menu :deep(.el-menu-item.is-active) {
  background: var(--el-color-primary-light-1);
}
/* 统一图标宽度和对齐 */
.el-menu :deep(.el-menu-item i),
.el-menu :deep(.el-sub-menu__title i) {
  width: 16px;
  display: inline-block;
  text-align: center;
}
/* 菜单项鼠标悬停颜色 */
.el-menu :deep(.el-menu-item:hover) {
  background-color: #f17b95 !important;
}
.el-menu :deep(.el-sub-menu__title:hover) {
  background-color: #f17b95 !important;
}
</style>


