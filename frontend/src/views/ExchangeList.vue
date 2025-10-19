<template>
  <div style="padding:24px;">
    <el-page-header content="我的调换申请" @back="$router.back()" />
    
    <!-- 操作栏 -->
    <el-card style="margin-top:16px;">
      <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:16px;">
        <div style="display:flex; gap:12px; align-items:center;">
          <el-select v-model="status" placeholder="筛选状态" clearable style="width:160px;">
            <el-option label="未处理" value="未处理" />
            <el-option label="已通过" value="已通过" />
            <el-option label="已驳回" value="已驳回" />
          </el-select>
          <el-button type="primary" @click="loadSwitches">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </div>
        <el-button type="success" @click="goCreate">
          <el-icon><Plus /></el-icon>
          发起新申请
        </el-button>
      </div>
    </el-card>

    <!-- 申请列表 -->
    <el-card style="margin-top:16px;">
      <template #header>
        <div style="display:flex; justify-content:space-between; align-items:center;">
          <span>申请记录</span>
          <el-button size="small" @click="loadSwitches">刷新</el-button>
        </div>
      </template>
      
      <div v-if="switches.length > 0">
        <div v-for="item in switches" :key="item.id" class="switch-item">
          <div class="switch-header">
            <div class="switch-info">
              <div class="switch-title">调换申请 #{{ item.id }}</div>
              <div class="switch-time">{{ formatDate(item.createdAt) }}</div>
            </div>
            <el-tag :type="getStatusTagType(item.status)" size="large">
              {{ item.status }}
            </el-tag>
          </div>
          
          <div class="switch-content">
            <el-row :gutter="20">
              <el-col :span="8">
                <div class="info-item">
                  <div class="info-label">申请人</div>
                  <div class="info-value">{{ item.applicant }}</div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="info-item">
                  <div class="info-label">当前宿舍</div>
                  <div class="info-value">{{ item.currentDorm }}</div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="info-item">
                  <div class="info-label">目标宿舍</div>
                  <div class="info-value">{{ item.targetDorm }}</div>
                </div>
              </el-col>
            </el-row>
            
            <div class="reason-section">
              <div class="info-label">调换原因</div>
              <div class="reason-text">{{ item.reason }}</div>
            </div>
            
            <div v-if="item.urgency" class="urgency-section">
              <el-tag :type="getUrgencyTagType(item.urgency)" size="small">
                紧急程度：{{ item.urgency }}
              </el-tag>
            </div>
          </div>
          
          <div class="switch-footer">
            <div class="switch-actions">
              <el-button size="small" @click="viewDetail(item)">查看详情</el-button>
              <el-button 
                size="small" 
                type="danger" 
                @click="cancelSwitch(item)"
                v-if="item.status === '未处理'"
              >
                取消申请
              </el-button>
            </div>
          </div>
        </div>
      </div>
      
      <el-empty v-else description="暂无调换申请记录" />
      
      <!-- 分页 -->
      <div style="display:flex; justify-content:center; margin-top:24px;">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { api } from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const router = useRouter()
const store = useStore()
const user = computed(() => store.state.user)

const switches = ref<any[]>([])
const loading = ref(false)
const status = ref('')

// 分页参数
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 格式化日期
const formatDate = (date: string) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

// 获取状态标签类型
const getStatusTagType = (status: string) => {
  switch (status) {
    case '未处理': return 'warning'
    case '已通过': return 'success'
    case '已驳回': return 'danger'
    default: return 'info'
  }
}

// 获取紧急程度标签类型
const getUrgencyTagType = (urgency: string) => {
  switch (urgency) {
    case '非常紧急': return 'danger'
    case '紧急': return 'warning'
    case '一般': return 'info'
    default: return ''
  }
}

// 加载调换申请列表
const loadSwitches = async () => {
  if (!user.value?.id) return
  
  loading.value = true
  try {
    const params: any = {
      page: pagination.page,
      size: pagination.size,
      studentId: user.value.id
    }
    
    if (status.value) {
      params.status = status.value
    }
    
    const response = await api.listMySwitches(user.value.id, params)
    const data = response.data.data
    
    if (data && data.content) {
      switches.value = data.content
      pagination.total = data.totalElements
    } else {
      switches.value = []
      pagination.total = 0
    }
  } catch (error) {
    console.error('加载调换申请失败:', error)
    ElMessage.error('加载申请列表失败')
    switches.value = []
  } finally {
    loading.value = false
  }
}

// 查看详情
const viewDetail = (item: any) => {
  ElMessage.info(`查看申请详情: ${item.id}`)
  // 这里可以打开详情弹窗或跳转到详情页面
}

// 取消申请
const cancelSwitch = async (item: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要取消调换申请 #${item.id} 吗？`,
      '确认取消',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 这里调用取消申请的API
    // await api.cancelSwitch(item.id)
    ElMessage.success('申请已取消')
    await loadSwitches()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '取消失败')
    }
  }
}

// 重置筛选
const handleReset = () => {
  status.value = ''
  pagination.page = 1
  loadSwitches()
}

// 页码变化
const handleCurrentChange = (page: number) => {
  pagination.page = page
  loadSwitches()
}

// 每页条数变化
const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.page = 1
  loadSwitches()
}

// 发起新申请
const goCreate = () => {
  router.push('/exchange/new')
}

onMounted(() => {
  loadSwitches()
})
</script>

<style scoped>
.switch-item {
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  margin-bottom: 16px;
  background-color: #fff;
  transition: all 0.3s;
}

.switch-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
}

.switch-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
  background-color: #fafafa;
}

.switch-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.switch-time {
  font-size: 12px;
  color: #666;
}

.switch-content {
  padding: 20px;
}

.info-item {
  margin-bottom: 12px;
}

.info-label {
  font-size: 12px;
  color: #666;
  margin-bottom: 4px;
}

.info-value {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.reason-section {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.reason-text {
  color: #666;
  line-height: 1.6;
  margin-top: 8px;
}

.urgency-section {
  margin-top: 12px;
}

.switch-footer {
  padding: 12px 20px;
  border-top: 1px solid #f0f0f0;
  background-color: #fafafa;
}

.switch-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>


