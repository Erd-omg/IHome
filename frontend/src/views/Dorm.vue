<template>
  <div style="padding:24px;">
    <el-page-header content="我的宿舍" @back="$router.push('/')" />
    
    <!-- 当前住宿信息 -->
    <el-card style="margin-top:16px;" v-if="currentAllocation">
      <template #header>
        <div style="display:flex; justify-content:space-between; align-items:center;">
          <span>当前住宿信息</span>
          <el-tag type="success">{{ currentAllocation.status }}</el-tag>
        </div>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="学号">{{ user?.id }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ user?.name }}</el-descriptions-item>
        <el-descriptions-item label="床位ID">{{ currentAllocation.bedId }}</el-descriptions-item>
        <el-descriptions-item label="入住时间">{{ formatDate(currentAllocation.checkInDate) }}</el-descriptions-item>
        <el-descriptions-item label="宿舍状态" span="2">
          <el-tag type="primary">正常入住</el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 宿舍详情 -->
    <el-card style="margin-top:16px;" v-if="dormitoryInfo">
      <template #header>宿舍详情</template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="宿舍ID">{{ dormitoryInfo.id }}</el-descriptions-item>
        <el-descriptions-item label="宿舍名称">{{ dormitoryInfo.name }}</el-descriptions-item>
        <el-descriptions-item label="楼栋">{{ dormitoryInfo.buildingId }}</el-descriptions-item>
        <el-descriptions-item label="床位总数">{{ dormitoryInfo.bedCount }}</el-descriptions-item>
        <el-descriptions-item label="当前入住">{{ dormitoryInfo.currentOccupancy }}</el-descriptions-item>
        <el-descriptions-item label="宿舍状态">
          <el-tag :type="getDormitoryStatusType(dormitoryInfo.status)">
            {{ dormitoryInfo.status }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 床位信息 -->
    <el-card style="margin-top:16px;" v-if="beds.length > 0">
      <template #header>床位信息</template>
      <el-row :gutter="16">
        <el-col :span="6" v-for="bed in beds" :key="bed.id">
          <el-card class="bed-card" :class="{ 'occupied': bed.status === '已占用' }">
            <div class="bed-number">{{ bed.bedNumber }}号床</div>
            <div class="bed-type">{{ bed.type }}</div>
            <div class="bed-status">
              <el-tag :type="bed.status === '可用' ? 'success' : 'danger'" size="small">
                {{ bed.status }}
              </el-tag>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>

    <!-- 住宿历史 -->
    <el-card style="margin-top:16px;">
      <template #header>住宿历史</template>
      <el-table :data="allocationHistory" v-loading="loadingHistory">
        <el-table-column prop="bedId" label="床位ID" />
        <el-table-column prop="checkInDate" label="入住时间">
          <template #default="{ row }">
            {{ formatDate(row.checkInDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="checkOutDate" label="退宿时间">
          <template #default="{ row }">
            {{ row.checkOutDate ? formatDate(row.checkOutDate) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="row.status === '在住' ? 'success' : 'info'">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 操作按钮 -->
    <div style="margin-top:24px; text-align:center;">
      <el-button type="primary" @click="$router.push('/dorm-search')">查看可选宿舍</el-button>
      <el-button type="warning" @click="handleCheckout" :disabled="!currentAllocation">申请退宿</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useStore } from 'vuex'
import { api } from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const store = useStore()
const user = computed(() => store.state.user)

const currentAllocation = ref<any>(null)
const dormitoryInfo = ref<any>(null)
const beds = ref<any[]>([])
const allocationHistory = ref<any[]>([])
const loadingHistory = ref(false)

// 格式化日期
const formatDate = (date: string) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString('zh-CN')
}

// 获取宿舍状态标签类型
const getDormitoryStatusType = (status: string) => {
  switch (status) {
    case '可用': return 'success'
    case '已满': return 'danger'
    default: return 'info'
  }
}

// 加载当前住宿信息
const loadCurrentAllocation = async () => {
  if (!user.value?.id) return
  
  try {
    const response = await api.getCurrentAllocation(user.value.id)
    currentAllocation.value = response.data.data
    
    if (currentAllocation.value) {
      // 加载宿舍信息
      await loadDormitoryInfo(currentAllocation.value.bedId)
    }
  } catch (error) {
    console.error('加载当前住宿信息失败:', error)
  }
}

// 加载宿舍信息
const loadDormitoryInfo = async (bedId: string) => {
  try {
    // 从床位ID推断宿舍ID（这里需要根据实际数据结构调整）
    const dormitoryId = bedId.split('-').slice(0, 3).join('-')
    
    // 加载宿舍基本信息
    const dormResponse = await api.listDorms({ name: dormitoryId })
    const dormData = dormResponse.data.data
    if (dormData && dormData.content && dormData.content.length > 0) {
      dormitoryInfo.value = dormData.content[0]
    }
    
    // 加载床位信息
    const bedsResponse = await api.listBeds(dormitoryId)
    beds.value = bedsResponse.data.data || []
  } catch (error) {
    console.error('加载宿舍信息失败:', error)
  }
}

// 加载住宿历史
const loadAllocationHistory = async () => {
  if (!user.value?.id) return
  
  loadingHistory.value = true
  try {
    const response = await api.getAllocationHistory(user.value.id)
    allocationHistory.value = response.data.data || []
  } catch (error) {
    console.error('加载住宿历史失败:', error)
  } finally {
    loadingHistory.value = false
  }
}

// 申请退宿
const handleCheckout = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要申请退宿吗？退宿后将无法继续使用当前床位。',
      '确认退宿',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    if (!user.value?.id) return
    
    await api.checkout(user.value.id)
    ElMessage.success('退宿申请已提交，请等待审核')
    
    // 重新加载数据
    await loadCurrentAllocation()
    await loadAllocationHistory()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '退宿申请失败')
    }
  }
}

onMounted(() => {
  loadCurrentAllocation()
  loadAllocationHistory()
})
</script>

<style scoped>
.bed-card {
  text-align: center;
  margin-bottom: 16px;
  border: 2px solid #f0f0f0;
  transition: all 0.3s;
}

.bed-card:hover {
  border-color: #409eff;
}

.bed-card.occupied {
  background-color: #f5f5f5;
  border-color: #dcdfe6;
}

.bed-number {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 8px;
  color: #333;
}

.bed-type {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.bed-status {
  margin-top: 8px;
}
</style>


