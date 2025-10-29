<template>
  <div style="padding:24px;">
    <div style="font-size:16px; font-weight:600; margin-bottom:16px;">管理员仪表盘</div>
    
    <!-- 统计卡片 -->
    <el-row :gutter="16" style="margin:24px 0;">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon students">👥</div>
          <div class="stat-content">
            <div class="stat-number">{{ dashboardData.totalStudents || 0 }}</div>
            <div class="stat-label">学生总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon dormitories">🏠</div>
          <div class="stat-content">
            <div class="stat-number">{{ dashboardData.totalDormitories || 0 }}</div>
            <div class="stat-label">宿舍总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon beds">🛏️</div>
          <div class="stat-content">
            <div class="stat-number">{{ dashboardData.occupiedBeds || 0 }}/{{ dashboardData.totalBeds || 0 }}</div>
            <div class="stat-label">床位使用率</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon repairs">🔧</div>
          <div class="stat-content">
            <div class="stat-number">{{ dashboardData.pendingRepairs || 0 }}</div>
            <div class="stat-label">待处理维修</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="16" style="margin:24px 0;">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div style="display:flex; justify-content:space-between; align-items:center;">
              <span>宿舍使用率</span>
              <el-button size="small" @click="refreshData">刷新</el-button>
            </div>
          </template>
          <div class="chart-container">
            <div class="chart-placeholder">
              <div class="chart-title">宿舍使用率统计</div>
              <div class="usage-rate">
                <div class="rate-number">{{ getUsageRate() }}%</div>
                <div class="rate-bar">
                  <div class="rate-fill" :style="{ width: getUsageRate() + '%' }"></div>
                </div>
              </div>
              <div class="rate-details">
                <div class="detail-item">
                  <span class="detail-label">已入住:</span>
                  <span class="detail-value">{{ dashboardData.occupiedBeds || 0 }} 人</span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">空余床位:</span>
                  <span class="detail-value">{{ (dashboardData.totalBeds || 0) - (dashboardData.occupiedBeds || 0) }} 个</span>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <div style="display:flex; justify-content:space-between; align-items:center;">
              <span>维修工单状态</span>
              <el-button size="small" @click="refreshData">刷新</el-button>
            </div>
          </template>
          <div class="chart-container">
            <div class="chart-placeholder">
              <div class="chart-title">维修工单分布</div>
              <div class="repair-stats">
                <div class="repair-item">
                  <div class="repair-label">待处理</div>
                  <div class="repair-count pending">{{ dashboardData.pendingRepairs || 0 }}</div>
                </div>
                <div class="repair-item">
                  <div class="repair-label">处理中</div>
                  <div class="repair-count processing">{{ (dashboardData.totalRepairs || 0) - (dashboardData.pendingRepairs || 0) - (dashboardData.completedRepairs || 0) }}</div>
                </div>
                <div class="repair-item">
                  <div class="repair-label">已完成</div>
                  <div class="repair-count completed">{{ dashboardData.completedRepairs || 0 }}</div>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快速操作 -->
    <el-card style="margin:24px 0;">
      <template #header>快速操作</template>
      <div class="quick-actions">
        <el-button type="primary" @click="$router.push('/admin/students')">
          <el-icon><User /></el-icon>
          学生管理
        </el-button>
        <el-button type="success" @click="$router.push('/admin/dormitories')">
          <el-icon><OfficeBuilding /></el-icon>
          宿舍管理
        </el-button>
        <el-button type="warning" @click="$router.push('/admin/repairs')">
          <el-icon><Tools /></el-icon>
          维修管理
        </el-button>
        <el-button type="info" @click="$router.push('/admin/payments')">
          <el-icon><Money /></el-icon>
          缴费管理
        </el-button>
        <el-button type="danger" @click="$router.push('/admin/exchanges')">
          <el-icon><Switch /></el-icon>
          调换审核
        </el-button>
      </div>
    </el-card>

    <!-- 最近活动 -->
    <el-card style="margin:24px 0;">
      <template #header>最近活动</template>
      <el-timeline v-if="recentActivities.length > 0">
        <el-timeline-item 
          v-for="(activity, index) in recentActivities.slice(0, 5)" 
          :key="index"
          :timestamp="formatDate(activity.timestamp)" 
          placement="top"
        >
          <el-card>
            <h4>{{ activity.title }}</h4>
            <p>{{ activity.description }}</p>
          </el-card>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-else description="暂无活动" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { api } from '../api'
import { ElMessage } from 'element-plus'
import { User, OfficeBuilding, Tools, Money, Switch } from '@element-plus/icons-vue'

const dashboardData = reactive({
  totalStudents: 0,
  totalDormitories: 0,
  totalBeds: 0,
  occupiedBeds: 0,
  totalPayments: 0,
  totalRepairs: 0,
  pendingRepairs: 0,
  completedRepairs: 0
})

const recentActivities = ref<any[]>([])

// 计算使用率
const getUsageRate = () => {
  if (!dashboardData.totalBeds) return 0
  return Math.round((dashboardData.occupiedBeds / dashboardData.totalBeds) * 100)
}

// 格式化日期
const formatDate = (date: any) => {
  if (!date) return '-'
  try {
    const d = new Date(date)
    return d.toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
  } catch {
    return date
  }
}

// 加载仪表盘数据
const loadDashboardData = async () => {
  try {
    const response = await api.dashboard()
    const data = response.data.data
    Object.assign(dashboardData, data)
    
    // 加载最近活动
    if (data.recentActivities) {
      recentActivities.value = data.recentActivities
    }
  } catch (error) {
    console.error('加载仪表盘数据失败:', error)
    ElMessage.error('加载数据失败')
  }
}

// 刷新数据
const refreshData = () => {
  loadDashboardData()
  ElMessage.success('数据已刷新')
}

onMounted(() => {
  loadDashboardData()
})
</script>

<style scoped>
.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
  border: 1px solid #f0f0f0;
  transition: all 0.3s;
}

.stat-card:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  transform: translateY(-2px);
}

.stat-icon {
  font-size: 40px;
  margin-right: 16px;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
}

.stat-icon.students {
  background-color: #e3f2fd;
  color: #1976d2;
}

.stat-icon.dormitories {
  background-color: #e8f5e8;
  color: #388e3c;
}

.stat-icon.beds {
  background-color: #fff3e0;
  color: #f57c00;
}

.stat-icon.repairs {
  background-color: #fce4ec;
  color: #c2185b;
}

.stat-content {
  flex: 1;
}

.stat-number {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  margin-bottom: 4px;
}

.stat-label {
  color: #666;
  font-size: 14px;
}

.chart-container {
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chart-placeholder {
  text-align: center;
  width: 100%;
}

.chart-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 20px;
  color: #333;
}

.usage-rate {
  margin-bottom: 20px;
}

.rate-number {
  font-size: 36px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 12px;
}

.rate-bar {
  width: 200px;
  height: 8px;
  background-color: #f0f0f0;
  border-radius: 4px;
  margin: 0 auto;
  overflow: hidden;
}

.rate-fill {
  height: 100%;
  background-color: #409eff;
  transition: width 0.3s;
}

.rate-details {
  display: flex;
  justify-content: space-around;
  margin-top: 16px;
}

.detail-item {
  text-align: center;
}

.detail-label {
  display: block;
  font-size: 12px;
  color: #666;
  margin-bottom: 4px;
}

.detail-value {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.repair-stats {
  display: flex;
  justify-content: space-around;
  margin-top: 20px;
}

.repair-item {
  text-align: center;
}

.repair-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.repair-count {
  font-size: 24px;
  font-weight: bold;
  padding: 8px 16px;
  border-radius: 20px;
}

.repair-count.pending {
  background-color: #fff3cd;
  color: #856404;
}

.repair-count.processing {
  background-color: #d1ecf1;
  color: #0c5460;
}

.repair-count.completed {
  background-color: #d4edda;
  color: #155724;
}

.quick-actions {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.quick-actions .el-button {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
}
</style>


