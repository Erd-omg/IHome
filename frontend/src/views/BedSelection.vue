<template>
  <div class="bed-selection">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>床位选择</span>
          <el-button type="primary" @click="refreshData">刷新</el-button>
        </div>
      </template>

      <!-- 当前床位信息 -->
      <div v-if="currentBedInfo" class="current-bed">
        <el-alert
          title="您当前的床位信息"
          type="info"
          :closable="false"
        />
        <el-descriptions :column="2" border class="bed-info">
          <el-descriptions-item label="床位ID">{{ currentBedInfo.bedId }}</el-descriptions-item>
          <el-descriptions-item label="床位类型">{{ currentBedInfo.bedType }}</el-descriptions-item>
          <el-descriptions-item label="宿舍号">{{ currentBedInfo.roomNumber }}</el-descriptions-item>
          <el-descriptions-item label="建筑ID">{{ currentBedInfo.buildingId }}</el-descriptions-item>
          <el-descriptions-item label="入住日期">{{ currentBedInfo.checkInDate }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ currentBedInfo.status }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 床位选择 -->
      <div v-else class="bed-selection-content">
        <el-tabs v-model="activeTab" @tab-click="handleTabClick">
          <el-tab-pane label="推荐床位" name="recommended">
            <div class="bed-list">
              <el-row :gutter="20">
                <el-col :span="8" v-for="bed in recommendedBeds" :key="bed.id">
                  <el-card class="bed-card" :class="{ 'selected': selectedBedId === bed.id }">
                    <div class="bed-info">
                      <h3>{{ bed.id }}</h3>
                      <p><strong>类型:</strong> {{ bed.bedType }}</p>
                      <p><strong>宿舍:</strong> {{ bed.dormitoryId }}</p>
                      <p><strong>状态:</strong> 
                        <el-tag :type="bed.status === '可用' ? 'success' : 'danger'">
                          {{ bed.status }}
                        </el-tag>
                      </p>
                    </div>
                    <div class="bed-actions">
                      <el-button 
                        type="primary" 
                        size="small" 
                        @click="selectBed(bed.id)"
                        :disabled="bed.status !== '可用'"
                      >
                        选择此床位
                      </el-button>
                    </div>
                  </el-card>
                </el-col>
              </el-row>
            </div>
          </el-tab-pane>

          <el-tab-pane label="所有可用床位" name="available">
            <div class="filter-section">
              <el-form :inline="true" :model="filterForm">
                <el-form-item label="宿舍ID">
                  <el-input 
                    v-model="filterForm.dormitoryId" 
                    placeholder="请输入宿舍ID"
                    clearable
                  />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="loadAvailableBeds">搜索</el-button>
                </el-form-item>
              </el-form>
            </div>

            <div class="bed-list">
              <el-row :gutter="20">
                <el-col :span="8" v-for="bed in availableBeds" :key="bed.id">
                  <el-card class="bed-card" :class="{ 'selected': selectedBedId === bed.id }">
                    <div class="bed-info">
                      <h3>{{ bed.id }}</h3>
                      <p><strong>类型:</strong> {{ bed.bedType }}</p>
                      <p><strong>宿舍:</strong> {{ bed.dormitoryId }}</p>
                      <p><strong>状态:</strong> 
                        <el-tag :type="bed.status === '可用' ? 'success' : 'danger'">
                          {{ bed.status }}
                        </el-tag>
                      </p>
                    </div>
                    <div class="bed-actions">
                      <el-button 
                        type="primary" 
                        size="small" 
                        @click="selectBed(bed.id)"
                        :disabled="bed.status !== '可用'"
                      >
                        选择此床位
                      </el-button>
                    </div>
                  </el-card>
                </el-col>
              </el-row>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/api'

const activeTab = ref('recommended')
const currentBedInfo = ref(null)
const recommendedBeds = ref([])
const availableBeds = ref([])
const selectedBedId = ref(null)
const filterForm = ref({
  dormitoryId: ''
})

onMounted(() => {
  loadCurrentBedInfo()
  loadRecommendedBeds()
})

const loadCurrentBedInfo = async () => {
  try {
    const response = await http.get('/beds/current')
    currentBedInfo.value = response.data.data
  } catch (error) {
    // 如果没有当前床位，继续显示选择界面
  }
}

const loadRecommendedBeds = async () => {
  try {
    const response = await http.get('/beds/recommended')
    recommendedBeds.value = response.data.data
  } catch (error) {
    ElMessage.error('获取推荐床位失败')
  }
}

const loadAvailableBeds = async () => {
  try {
    const params = filterForm.value.dormitoryId ? { dormitoryId: filterForm.value.dormitoryId } : {}
    const response = await http.get('/beds/available', { params })
    availableBeds.value = response.data.data
  } catch (error) {
    ElMessage.error('获取可用床位失败')
  }
}

const handleTabClick = (tab) => {
  if (tab.name === 'available') {
    loadAvailableBeds()
  }
}

const selectBed = async (bedId) => {
  try {
    await ElMessageBox.confirm('确定要选择这个床位吗？', '确认选择', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await http.post('/beds/select', null, {
      params: { bedId }
    })

    ElMessage.success('床位选择成功')
    selectedBedId.value = bedId
    await loadCurrentBedInfo()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('床位选择失败')
    }
  }
}

const refreshData = () => {
  loadCurrentBedInfo()
  loadRecommendedBeds()
  if (activeTab.value === 'available') {
    loadAvailableBeds()
  }
}
</script>

<style scoped>
.bed-selection {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.current-bed {
  margin-bottom: 20px;
}

.bed-info {
  margin-top: 20px;
}

.bed-selection-content {
  margin-top: 20px;
}

.filter-section {
  margin-bottom: 20px;
  padding: 20px;
  background-color: #f5f5f5;
  border-radius: 4px;
}

.bed-list {
  margin-top: 20px;
}

.bed-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: all 0.3s;
}

.bed-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.bed-card.selected {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.bed-info h3 {
  margin: 0 0 10px 0;
  color: #303133;
}

.bed-info p {
  margin: 5px 0;
  color: #606266;
}

.bed-actions {
  margin-top: 15px;
  text-align: center;
}
</style>
