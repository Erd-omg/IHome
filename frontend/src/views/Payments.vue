<template>
  <div style="padding:24px;">
    <div style="margin-bottom: 16px;">
      <el-button type="text" @click="$router.back()" style="padding: 0;">
        <span style="font-size: 16px;">← 返回</span>
      </el-button>
    </div>
    <h2 style="margin: 0; font-size: 20px; font-weight: 600;">缴费管理</h2>
    
    <!-- 电费提醒设置 -->
    <el-card style="margin-top:16px;">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span>电费提醒设置</span>
          <el-button size="small" type="primary" @click="showElectricityDialog = true">设置提醒</el-button>
        </div>
      </template>
      <div style="color: #909399; font-size: 14px;">
        当前设置：余额低于 <strong style="color: #409eff;">{{ electricityThreshold || '未设置' }}</strong> 元时提醒
      </div>
    </el-card>

    <!-- 在线缴费 -->
    <el-card style="margin-top:16px;">
      <template #header>在线缴费</template>
      <el-form :model="form" label-width="100px" style="max-width:480px;">
        <el-form-item label="学号">
          <el-input v-model="form.studentId" :disabled="true" />
        </el-form-item>
        <el-form-item label="金额（元）">
          <el-input-number 
            v-model="form.amount" 
            :precision="2" 
            :step="0.01" 
            :min="0" 
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="缴费类型">
          <el-select v-model="form.paymentType" placeholder="请选择缴费类型" style="width: 100%">
            <el-option label="住宿费" value="住宿费" />
            <el-option label="电费" value="电费" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item><el-button type="primary" @click="createPayment">提交</el-button></el-form-item>
      </el-form>
    </el-card>

    <el-card style="margin-top:16px;">
      <template #header>历史记录查询</template>
      
      <!-- 搜索表单 -->
      <el-form :model="searchForm" :inline="true" style="margin-bottom: 16px;">
        <el-form-item label="学号">
          <el-input v-model="searchForm.studentId" placeholder="请输入学号" clearable />
        </el-form-item>
        <el-form-item label="缴费类型">
          <el-select v-model="searchForm.paymentType" placeholder="请选择" clearable style="width: 150px;">
            <el-option label="住宿费" value="住宿费" />
            <el-option label="电费" value="电费" />
            <el-option label="其他" value="其他" />
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
        <el-table-column prop="paymentType" label="缴费类型" />
        <el-table-column prop="amount" label="金额" sortable="custom">
          <template #default="{ row }">
            ¥{{ row.amount?.toFixed(2) || '0.00' }}
          </template>
        </el-table-column>
        <el-table-column prop="paymentTime" label="时间" sortable="custom">
          <template #default="{ row }">
            {{ formatDate(row.paymentTime) }}
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

    <!-- 电费提醒设置对话框 -->
    <el-dialog 
      v-model="showElectricityDialog" 
      title="电费提醒设置" 
      width="400px"
      @closed="showElectricityDialog = false"
    >
      <el-form label-width="120px">
        <el-form-item label="提醒阈值（元）">
          <el-input-number 
            v-model="electricityThreshold" 
            :precision="2" 
            :step="0.01" 
            :min="0" 
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item>
          <span style="color: #909399; font-size: 12px;">
            当电费余额低于设置值时，系统将发送提醒通知
          </span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showElectricityDialog = false">取消</el-button>
        <el-button type="primary" @click="setElectricityReminder">确定</el-button>
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

// 获取当前用户信息
const currentUser = computed(() => store.state.user)

const form = ref({ studentId: '', amount: 0, paymentType: '', paymentMethod: '在线支付' })
const list = ref<any[]>([])
const loading = ref(false)
const showElectricityDialog = ref(false)
const electricityThreshold = ref(50)

// 初始化表单学号
const initForm = () => {
  if (currentUser.value?.id) {
    form.value.studentId = currentUser.value.id
  }
}

// 搜索表单
const searchForm = reactive({
  studentId: '',
  paymentType: ''
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

// 创建缴费记录
async function createPayment() {
  if (!form.value.studentId || !form.value.amount || !form.value.paymentType) {
    ElMessage.warning('请填写完整信息')
    return
  }
  
  try {
    await api.createPayment(form.value)
    ElMessage.success('缴费记录创建成功')
    // 重置表单但保持学号
    const currentStudentId = form.value.studentId
    form.value = { studentId: currentStudentId, amount: 0, paymentType: '', paymentMethod: '在线支付' }
    await loadPayments()
  } catch (error: any) {
    ElMessage.error(error.message || '创建失败')
  }
}

// 加载缴费记录
async function loadPayments() {
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
    if (searchForm.paymentType) {
      params.paymentType = searchForm.paymentType
    }
    
    // 添加排序参数
    if (sortParams.sortBy) {
      params.sort = `${sortParams.sortBy},${sortParams.sortOrder}`
    }
    
    const response = await api.listPayments(params)
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
  loadPayments()
}

// 重置搜索
function handleReset() {
  searchForm.studentId = ''
  searchForm.paymentType = ''
  pagination.page = 1
  loadPayments()
}

// 排序变化
function handleSortChange({ prop, order }: { prop: string; order: string }) {
  sortParams.sortBy = prop
  sortParams.sortOrder = order === 'ascending' ? 'asc' : 'desc'
  loadPayments()
}

// 页码变化
function handleCurrentChange(page: number) {
  pagination.page = page
  loadPayments()
}

// 每页条数变化
function handleSizeChange(size: number) {
  pagination.size = size
  pagination.page = 1
  loadPayments()
}

// 设置电费提醒
async function setElectricityReminder() {
  try {
    // 这里应该调用后端API保存设置
    // await api.setElectricityThreshold(electricityThreshold.value)
    ElMessage.success('电费提醒设置成功')
    showElectricityDialog.value = false
  } catch (error: any) {
    ElMessage.error('设置失败: ' + (error.message || '未知错误'))
  }
}

// 页面加载时获取数据
onMounted(() => {
  initForm()
  loadPayments()
})
</script>


