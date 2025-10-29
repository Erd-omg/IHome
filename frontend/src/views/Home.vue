<template>
  <div style="padding:24px;">
    <!-- 欢迎信息 -->
    <el-card style="margin:24px 0;">
      <template #header>
        <div style="display:flex; justify-content:space-between; align-items:center;">
          <span>欢迎回来，{{ user?.name || '同学' }}！</span>
          <el-tag type="success" v-if="user">学号：{{ user.id }}</el-tag>
        </div>
      </template>
      <div style="display:flex; gap:16px; flex-wrap:wrap;">
        <el-card class="box" @click="$router.push('/dorm')">
          <div class="box-icon">🏠</div>
          <div class="box-title">我的宿舍</div>
          <div class="box-desc">查看宿舍信息</div>
        </el-card>
        <el-card class="box" @click="$router.push('/payments')">
          <div class="box-icon">💰</div>
          <div class="box-title">在线缴费</div>
          <div class="box-desc">缴纳住宿费用</div>
        </el-card>
        <el-card class="box" @click="$router.push('/repairs')">
          <div class="box-icon">🔧</div>
          <div class="box-title">在线维修</div>
          <div class="box-desc">申请维修服务</div>
        </el-card>
        <el-card class="box" @click="$router.push('/exchange')">
          <div class="box-icon">🔄</div>
          <div class="box-title">宿舍调换</div>
          <div class="box-desc">申请调换宿舍</div>
        </el-card>
        <el-card class="box" @click="$router.push('/profile')">
          <div class="box-icon">👤</div>
          <div class="box-title">个人中心</div>
          <div class="box-desc">修改个人信息</div>
        </el-card>
      </div>
    </el-card>

    <!-- 统计信息 -->
    <el-row :gutter="16" style="margin:24px 0;">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-number">{{ stats.pendingPayments || 0 }}</div>
          <div class="stat-label">待缴费项</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-number">{{ stats.pendingRepairs || 0 }}</div>
          <div class="stat-label">待处理维修</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-number">{{ stats.exchangeRequests || 0 }}</div>
          <div class="stat-label">调换申请</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-number">{{ stats.unreadNotices || 0 }}</div>
          <div class="stat-label">未读通知</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最新通知 -->
    <el-card>
      <template #header>
        <div style="display:flex; justify-content:space-between; align-items:center;">
          <span>宿舍最新通知</span>
          <el-button type="primary" size="small" @click="$router.push('/notices')">查看全部</el-button>
        </div>
      </template>
      <div v-if="notices.length > 0">
        <div v-for="notice in notices" :key="notice.id" class="notice-item" @click="viewNotice(notice.id)">
          <div class="notice-title">{{ notice.title }}</div>
          <div class="notice-time">{{ formatDate(notice.publishTime || notice.createTime) }}</div>
        </div>
      </div>
      <el-empty v-else description="暂无通知" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { api } from '../api'
import { ElMessage } from 'element-plus'

const router = useRouter()
const store = useStore()

const user = computed(() => store.state.user)
const notices = ref<any[]>([])
const stats = reactive({
  pendingPayments: 0,
  pendingRepairs: 0,
  exchangeRequests: 0,
  unreadNotices: 0
})

// 格式化日期
const formatDate = (date: string) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString('zh-CN')
}

// 查看通知详情
const viewNotice = (id: string | number) => {
  router.push(`/notices/${id}`)
}

// 加载最新通知
const loadNotices = async () => {
  try {
    const response = await api.listNotices({ page: 1, size: 5 })
    const data = response.data.data
    if (data && data.content) {
      notices.value = data.content
    }
  } catch (error) {
    console.error('加载通知失败:', error)
  }
}

// 加载统计数据
const loadStats = async () => {
  if (!user.value?.id) return
  
  try {
    // 这里可以调用专门的统计接口，或者分别调用各个接口获取数据
    // 暂时使用模拟数据
    stats.pendingPayments = 2
    stats.pendingRepairs = 1
    stats.exchangeRequests = 0
    stats.unreadNotices = 3
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

onMounted(() => {
  loadNotices()
  loadStats()
})
</script>

<style scoped>
.box { 
  width: 180px; 
  text-align: center; 
  cursor: pointer; 
  transition: all 0.3s;
  border: 1px solid #f0f0f0;
}
.box:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.box-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.box-title {
  font-weight: 600;
  margin-bottom: 4px;
  color: #333;
}

.box-desc {
  font-size: 12px;
  color: #666;
}

.stat-card {
  text-align: center;
  border: 1px solid #f0f0f0;
}

.stat-number {
  font-size: 28px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 8px;
}

.stat-label {
  color: #666;
  font-size: 14px;
}

.notice-item {
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.3s;
}

.notice-item:hover {
  background-color: #f8f9fa;
}

.notice-item:last-child {
  border-bottom: none;
}

.notice-title {
  font-weight: 500;
  margin-bottom: 4px;
  color: #333;
}

.notice-time {
  font-size: 12px;
  color: #999;
}
</style>


