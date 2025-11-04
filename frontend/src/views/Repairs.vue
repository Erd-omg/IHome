<template>
  <div style="padding:24px;">
    <div style="margin-bottom: 16px;">
      <el-button type="text" @click="$router.back()" style="padding: 0;">
        <span style="font-size: 16px;">← 返回</span>
      </el-button>
    </div>
    <h2 style="margin: 0; font-size: 20px; font-weight: 600;">维修管理</h2>
    <el-card style="margin-top:16px;">
      <template #header>维修申请</template>
      <el-form :model="form" label-width="100px" style="max-width:520px;">
        <el-form-item label="学号">
          <el-input v-model="form.studentId" :disabled="true" />
        </el-form-item>
        <el-form-item label="宿舍ID"><el-input v-model="form.dormitoryId" /></el-form-item>
        <el-form-item label="维修类型">
          <el-select v-model="form.repairType" placeholder="请选择维修类型" style="width: 100%;">
            <el-option label="水电维修" value="水电维修" />
            <el-option label="空调维修" value="空调维修" />
            <el-option label="家具维修" value="家具维修" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" /></el-form-item>
        <el-form-item label="紧急程度">
          <el-select v-model="form.urgencyLevel" style="width: 200px;">
            <el-option label="一般" value="一般" />
            <el-option label="紧急" value="紧急" />
            <el-option label="非常紧急" value="非常紧急" />
          </el-select>
        </el-form-item>
        <el-form-item><el-button type="primary" @click="submit">提交</el-button></el-form-item>
      </el-form>
    </el-card>

    <el-card style="margin-top:16px;">
      <template #header>维修记录查询</template>
      
      <!-- 搜索表单 -->
      <el-form :model="searchForm" :inline="true" style="margin-bottom: 16px;">
        <el-form-item label="学号">
          <el-input v-model="searchForm.studentId" placeholder="请输入学号" clearable />
        </el-form-item>
        <el-form-item label="宿舍ID">
          <el-input v-model="searchForm.dormitoryId" placeholder="请输入宿舍ID" clearable />
        </el-form-item>
        <el-form-item label="维修类型">
          <el-select v-model="searchForm.repairType" placeholder="请选择" clearable style="width: 150px;">
            <el-option label="水电维修" value="水电维修" />
            <el-option label="空调维修" value="空调维修" />
            <el-option label="家具维修" value="家具维修" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 150px;">
            <el-option label="待处理" value="待处理" />
            <el-option label="处理中" value="处理中" />
            <el-option label="已完成" value="已完成" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格 -->
      <el-table 
        :data="list" 
        style="width:100%;" 
        v-loading="loading"
        @sort-change="handleSortChange"
      >
        <el-table-column prop="studentId" label="学号" sortable="custom" />
        <el-table-column prop="dormitoryId" label="宿舍ID" sortable="custom" />
        <el-table-column prop="repairType" label="维修类型" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="urgencyLevel" label="紧急程度">
          <template #default="{ row }">
            <el-tag 
              :type="getUrgencyTagType(row.urgencyLevel)"
              size="small"
            >
              {{ row.urgencyLevel }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag 
              :type="getStatusTagType(row.status)"
              size="small"
            >
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" sortable="custom">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="viewDetail(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div style="display:flex; justify-content:center; margin-top:16px;">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 维修详情弹窗 -->
    <el-dialog v-model="showRepairDetail" title="维修详情" width="600px">
      <el-descriptions :column="2" border v-if="currentRepair">
        <el-descriptions-item label="工单ID">
          {{ currentRepair.id }}
        </el-descriptions-item>
        <el-descriptions-item label="学号">
          {{ currentRepair.studentId }}
        </el-descriptions-item>
        <el-descriptions-item label="宿舍ID">
          {{ currentRepair.dormitoryId }}
        </el-descriptions-item>
        <el-descriptions-item label="维修类型">
          <el-tag>{{ currentRepair.repairType }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="紧急程度" :span="2">
          <el-tag :type="getUrgencyTagType(currentRepair.urgencyLevel)">
            {{ currentRepair.urgencyLevel }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态" :span="2">
          <el-tag :type="getStatusTagType(currentRepair.status)">
            {{ currentRepair.status }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">
          {{ currentRepair.description }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ formatDate(currentRepair.createdAt) }}
        </el-descriptions-item>
        <el-descriptions-item label="更新时间" v-if="currentRepair.updatedAt">
          {{ formatDate(currentRepair.updatedAt) }}
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="showRepairDetail = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api, type PaginationParams } from '../api'
import { useStore } from 'vuex'

const store = useStore()
const currentUser = computed(() => store.state.user)

const form = reactive({ 
  studentId: '',
  dormitoryId: '', 
  repairType: '', 
  description: '', 
  urgencyLevel: '一般' 
})

const list = ref<any[]>([])
const loading = ref(false)
const showRepairDetail = ref(false)
const currentRepair = ref<any>(null)

// 搜索表单
const searchForm = reactive({
  studentId: '',
  dormitoryId: '',
  repairType: '',
  status: ''
})

// 分页参数
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 排序参数
const sortParams = reactive({
  sortBy: '',
  sortOrder: ''
})

// 格式化日期
const formatDate = (date: string) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

// 获取紧急程度标签类型
const getUrgencyTagType = (level: string) => {
  switch (level) {
    case '紧急': return 'danger'
    case '一般': return 'warning'
    case '非常紧急': return 'danger'
    default: return ''
  }
}

// 获取状态标签类型
const getStatusTagType = (status: string) => {
  switch (status) {
    case '待处理': return 'warning'
    case '处理中': return 'primary'
    case '已完成': return 'success'
    default: return ''
  }
}

// 提交维修申请
async function submit() {
  if (!form.dormitoryId || !form.repairType || !form.description) {
    ElMessage.warning('请填写完整信息')
    return
  }
  
  try {
    await api.createRepair(form)
    ElMessage.success('维修申请提交成功')
    // 重置表单但保持学号和宿舍ID
    const currentStudentId = form.studentId
    const currentDormitoryId = form.dormitoryId
    form.studentId = currentStudentId
    form.dormitoryId = currentDormitoryId
    form.repairType = ''
    form.description = ''
    form.urgencyLevel = '一般'
    await loadRepairs()
  } catch (error: any) {
    ElMessage.error(error.message || '提交失败')
  }
}

// 加载维修记录
async function loadRepairs() {
  loading.value = true
  try {
    const params: PaginationParams = {
      page: pagination.page,
      size: pagination.size
    }
    
    // 添加搜索条件
    if (searchForm.studentId) {
      params.studentId = searchForm.studentId
    }
    if (searchForm.dormitoryId) {
      params.dormitoryId = searchForm.dormitoryId
    }
    if (searchForm.repairType) {
      params.repairType = searchForm.repairType
    }
    if (searchForm.status) {
      params.status = searchForm.status
    }
    
    // 添加排序参数
    if (sortParams.sortBy) {
      params.sort = `${sortParams.sortBy},${sortParams.sortOrder}`
    }
    
    const response = await api.listRepairs(params)
    const data = response.data.data
    
    if (Array.isArray(data)) {
      // 如果后端返回的是数组（非分页格式）
      list.value = data
      pagination.total = data.length
    } else if (data && data.content) {
      // 如果后端返回的是分页格式
      list.value = data.content
      pagination.total = data.totalElements
    } else {
      list.value = []
      pagination.total = 0
    }
  } catch (error: any) {
    ElMessage.error(error.message || '加载失败')
    list.value = []
  } finally {
    loading.value = false
  }
}

// 搜索
function handleSearch() {
  pagination.page = 1
  loadRepairs()
}

// 重置搜索
function handleReset() {
  searchForm.studentId = ''
  searchForm.dormitoryId = ''
  searchForm.repairType = ''
  searchForm.status = ''
  pagination.page = 1
  loadRepairs()
}

// 排序变化
function handleSortChange({ prop, order }: { prop: string; order: string }) {
  sortParams.sortBy = prop
  sortParams.sortOrder = order === 'ascending' ? 'asc' : 'desc'
  loadRepairs()
}

// 页码变化
function handleCurrentChange(page: number) {
  pagination.page = page
  loadRepairs()
}

// 每页条数变化
function handleSizeChange(size: number) {
  pagination.size = size
  pagination.page = 1
  loadRepairs()
}

// 查看详情
function viewDetail(row: any) {
  showRepairDetail.value = true
  currentRepair.value = row
}


// 初始化表单
const initForm = async () => {
  if (currentUser.value?.id) {
    form.studentId = currentUser.value.id
    
    // 加载当前住宿信息获取宿舍ID
    try {
      const response = await api.getCurrentAllocation(currentUser.value.id)
      const allocation = response.data.data
      console.log('当前住宿信息:', allocation)
      
      if (allocation) {
        // 优先使用dormitoryId字段
        if (allocation.dormitoryId) {
          form.dormitoryId = allocation.dormitoryId
          console.log('设置默认宿舍ID:', form.dormitoryId)
        } else {
          console.warn('当前住宿信息中没有dormitoryId字段')
        }
      }
    } catch (error) {
      console.error('加载当前住宿信息失败:', error)
    }
  }
}

// 页面加载时获取数据
onMounted(() => {
  initForm()
  loadRepairs()
})
</script>


