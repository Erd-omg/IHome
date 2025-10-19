<template>
  <div style="padding:16px;">
    <el-card>
      <template #header>
        <div style="display:flex; justify-content:space-between; align-items:center;">
          <span>住宿分配管理</span>
          <div>
            <el-button type="primary" @click="showAllocateDialog = true">
              <el-icon><Plus /></el-icon>
              分配宿舍
            </el-button>
            <el-button @click="loadAllocations">刷新</el-button>
          </div>
        </div>
      </template>

      <!-- 搜索筛选 -->
      <el-form :model="searchForm" :inline="true" style="margin-bottom:16px;">
        <el-form-item label="学号">
          <el-input v-model="searchForm.studentId" placeholder="请输入学号" clearable style="width:160px;" />
        </el-form-item>
        <el-form-item label="床位ID">
          <el-input v-model="searchForm.bedId" placeholder="请输入床位ID" clearable style="width:160px;" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width:120px;">
            <el-option label="在住" value="在住" />
            <el-option label="已退宿" value="已退宿" />
          </el-select>
        </el-form-item>
        <el-form-item label="入住时间">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width:240px;"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 分配列表 -->
      <el-table :data="allocations" v-loading="loading" @sort-change="handleSortChange">
        <el-table-column prop="id" label="分配ID" sortable="custom" width="100" />
        <el-table-column prop="studentId" label="学号" sortable="custom" width="120" />
        <el-table-column prop="bedId" label="床位ID" sortable="custom" width="140" />
        <el-table-column prop="checkInDate" label="入住时间" sortable="custom" width="120">
          <template #default="{ row }">
            {{ formatDate(row.checkInDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="checkOutDate" label="退宿时间" sortable="custom" width="120">
          <template #default="{ row }">
            {{ row.checkOutDate ? formatDate(row.checkOutDate) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button 
              size="small" 
              type="warning" 
              @click="handleCheckout(row)"
              v-if="row.status === '在住'"
            >
              办理退宿
            </el-button>
            <el-button size="small" type="info" @click="viewHistory(row)">查看历史</el-button>
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

    <!-- 分配宿舍弹窗 -->
    <el-dialog v-model="showAllocateDialog" title="分配宿舍" width="600px">
      <el-form :model="allocateForm" :rules="allocateRules" ref="allocateFormRef" label-width="100px">
        <el-form-item label="学号" prop="studentId">
          <el-input v-model="allocateForm.studentId" placeholder="请输入学号" />
        </el-form-item>
        <el-form-item label="床位ID" prop="bedId">
          <el-input v-model="allocateForm.bedId" placeholder="请输入床位ID" />
        </el-form-item>
        <el-form-item label="入住时间" prop="checkInDate">
          <el-date-picker
            v-model="allocateForm.checkInDate"
            type="date"
            placeholder="选择入住时间"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width:100%;"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="allocateForm.remark" type="textarea" :rows="3" placeholder="请输入备注信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAllocateDialog = false">取消</el-button>
        <el-button type="primary" @click="saveAllocation" :loading="saving">分配</el-button>
      </template>
    </el-dialog>

    <!-- 退宿确认弹窗 -->
    <el-dialog v-model="showCheckoutDialog" title="办理退宿" width="500px">
      <div v-if="checkoutAllocation">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="学号">{{ checkoutAllocation.studentId }}</el-descriptions-item>
          <el-descriptions-item label="床位ID">{{ checkoutAllocation.bedId }}</el-descriptions-item>
          <el-descriptions-item label="入住时间">{{ formatDate(checkoutAllocation.checkInDate) }}</el-descriptions-item>
        </el-descriptions>
        <el-form :model="checkoutForm" label-width="100px" style="margin-top:20px;">
          <el-form-item label="退宿时间">
            <el-date-picker
              v-model="checkoutForm.checkOutDate"
              type="date"
              placeholder="选择退宿时间"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              style="width:100%;"
            />
          </el-form-item>
          <el-form-item label="退宿原因">
            <el-input v-model="checkoutForm.reason" type="textarea" :rows="3" placeholder="请输入退宿原因" />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="showCheckoutDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmCheckout" :loading="checkingOut">确认退宿</el-button>
      </template>
    </el-dialog>

    <!-- 住宿历史弹窗 -->
    <el-dialog v-model="showHistoryDialog" title="住宿历史" width="800px">
      <div v-if="historyAllocation">
        <div style="margin-bottom:16px;">
          <el-tag type="info">学号：{{ historyAllocation.studentId }}</el-tag>
        </div>
        <el-table :data="allocationHistory" v-loading="loadingHistory">
          <el-table-column prop="bedId" label="床位ID" width="140" />
          <el-table-column prop="checkInDate" label="入住时间" width="120">
            <template #default="{ row }">
              {{ formatDate(row.checkInDate) }}
            </template>
          </el-table-column>
          <el-table-column prop="checkOutDate" label="退宿时间" width="120">
            <template #default="{ row }">
              {{ row.checkOutDate ? formatDate(row.checkOutDate) : '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusTagType(row.status)">
                {{ row.status }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { api } from '../api'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const allocations = ref<any[]>([])
const allocationHistory = ref<any[]>([])
const loading = ref(false)
const loadingHistory = ref(false)
const saving = ref(false)
const checkingOut = ref(false)
const showAllocateDialog = ref(false)
const showCheckoutDialog = ref(false)
const showHistoryDialog = ref(false)
const checkoutAllocation = ref<any>(null)
const historyAllocation = ref<any>(null)
const allocateFormRef = ref<FormInstance>()

// 搜索表单
const searchForm = reactive({
  studentId: '',
  bedId: '',
  status: '',
  dateRange: null as [string, string] | null
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

// 分配表单
const allocateForm = reactive({
  studentId: '',
  bedId: '',
  checkInDate: '',
  remark: ''
})

// 退宿表单
const checkoutForm = reactive({
  checkOutDate: '',
  reason: ''
})

// 表单验证规则
const allocateRules: FormRules = {
  studentId: [
    { required: true, message: '请输入学号', trigger: 'blur' }
  ],
  bedId: [
    { required: true, message: '请输入床位ID', trigger: 'blur' }
  ],
  checkInDate: [
    { required: true, message: '请选择入住时间', trigger: 'change' }
  ]
}

// 格式化日期
const formatDate = (date: string) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString('zh-CN')
}

// 获取状态标签类型
const getStatusTagType = (status: string) => {
  switch (status) {
    case '在住': return 'success'
    case '已退宿': return 'info'
    default: return 'warning'
  }
}

// 加载分配列表
const loadAllocations = async () => {
  loading.value = true
  try {
    const params: any = {
      page: pagination.page,
      size: pagination.size
    }
    
    // 添加搜索条件
    if (searchForm.studentId) {
      params.studentId = searchForm.studentId
    }
    if (searchForm.bedId) {
      params.bedId = searchForm.bedId
    }
    if (searchForm.status) {
      params.status = searchForm.status
    }
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }
    
    // 添加排序参数
    if (sortParams.sortBy) {
      params.sort = `${sortParams.sortBy},${sortParams.sortOrder}`
    }
    
    const response = await api.adminAllocations(params)
    const data = response.data.data
    
    if (data && data.content) {
      allocations.value = data.content
      pagination.total = data.totalElements
    } else {
      allocations.value = []
      pagination.total = 0
    }
  } catch (error) {
    console.error('加载分配列表失败:', error)
    ElMessage.error('加载分配列表失败')
    allocations.value = []
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  loadAllocations()
}

// 重置搜索
const handleReset = () => {
  searchForm.studentId = ''
  searchForm.bedId = ''
  searchForm.status = ''
  searchForm.dateRange = null
  pagination.page = 1
  loadAllocations()
}

// 排序变化
const handleSortChange = ({ prop, order }: { prop: string; order: string }) => {
  sortParams.sortBy = prop
  sortParams.sortOrder = order === 'ascending' ? 'asc' : 'desc'
  loadAllocations()
}

// 页码变化
const handleCurrentChange = (page: number) => {
  pagination.page = page
  loadAllocations()
}

// 每页条数变化
const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.page = 1
  loadAllocations()
}

// 办理退宿
const handleCheckout = (row: any) => {
  checkoutAllocation.value = row
  checkoutForm.checkOutDate = new Date().toISOString().split('T')[0]
  checkoutForm.reason = ''
  showCheckoutDialog.value = true
}

// 确认退宿
const confirmCheckout = async () => {
  if (!checkoutAllocation.value) return
  
  try {
    checkingOut.value = true
    
    // 这里调用退宿的API
    // await api.checkoutStudent(checkoutAllocation.value.id, checkoutForm)
    ElMessage.success('退宿办理成功')
    showCheckoutDialog.value = false
    await loadAllocations()
  } catch (error: any) {
    ElMessage.error(error.message || '退宿办理失败')
  } finally {
    checkingOut.value = false
  }
}

// 查看历史
const viewHistory = async (row: any) => {
  historyAllocation.value = row
  loadingHistory.value = true
  
  try {
    const response = await api.getAllocationHistory(row.studentId)
    allocationHistory.value = response.data.data || []
    showHistoryDialog.value = true
  } catch (error) {
    console.error('加载住宿历史失败:', error)
    ElMessage.error('加载住宿历史失败')
  } finally {
    loadingHistory.value = false
  }
}

// 保存分配
const saveAllocation = async () => {
  if (!allocateFormRef.value) return
  
  try {
    await allocateFormRef.value.validate()
    saving.value = true
    
    // 这里调用分配宿舍的API
    // await api.allocateStudent(allocateForm)
    ElMessage.success('宿舍分配成功')
    showAllocateDialog.value = false
    resetAllocateForm()
    await loadAllocations()
  } catch (error: any) {
    if (error !== false) { // 不是表单验证错误
      ElMessage.error(error.message || '分配失败')
    }
  } finally {
    saving.value = false
  }
}

// 重置分配表单
const resetAllocateForm = () => {
  Object.assign(allocateForm, {
    studentId: '',
    bedId: '',
    checkInDate: '',
    remark: ''
  })
}

onMounted(() => {
  loadAllocations()
})
</script>


