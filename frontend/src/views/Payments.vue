<template>
  <div style="padding:24px;">
    <el-page-header content="缴费管理" @back="$router.back()" />
    <el-card style="margin-top:16px;">
      <template #header>在线缴费</template>
      <el-form :model="form" label-width="100px" style="max-width:480px;">
        <el-form-item label="学号"><el-input v-model="form.studentId" /></el-form-item>
        <el-form-item label="金额"><el-input v-model.number="form.amount" type="number" /></el-form-item>
        <el-form-item label="方式"><el-input v-model="form.paymentMethod" placeholder="住宿费/电费..." /></el-form-item>
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
        <el-form-item label="缴费方式">
          <el-select v-model="searchForm.paymentMethod" placeholder="请选择" clearable>
            <el-option label="住宿费" value="住宿费" />
            <el-option label="电费" value="电费" />
            <el-option label="水费" value="水费" />
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
        <el-table-column prop="amount" label="金额" sortable="custom">
          <template #default="{ row }">
            ¥{{ row.amount?.toFixed(2) || '0.00' }}
          </template>
        </el-table-column>
        <el-table-column prop="paymentMethod" label="方式" />
        <el-table-column prop="paymentTime" label="时间" sortable="custom">
          <template #default="{ row }">
            {{ formatDate(row.paymentTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { api, type PaginationParams } from '../api'

const form = ref({ studentId: '', amount: 0, paymentMethod: '' })
const list = ref<any[]>([])
const loading = ref(false)

// 搜索表单
const searchForm = reactive({
  studentId: '',
  paymentMethod: ''
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
  if (!form.value.studentId || !form.value.amount || !form.value.paymentMethod) {
    ElMessage.warning('请填写完整信息')
    return
  }
  
  try {
    await api.createPayment(form.value)
    ElMessage.success('缴费记录创建成功')
    form.value = { studentId: '', amount: 0, paymentMethod: '' }
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
    if (searchForm.paymentMethod) {
      params.paymentMethod = searchForm.paymentMethod
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
  searchForm.paymentMethod = ''
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

// 查看详情
function viewDetail(row: any) {
  ElMessage.info(`查看缴费记录详情: ${row.studentId}`)
  // 这里可以打开详情弹窗或跳转到详情页面
}

// 页面加载时获取数据
onMounted(() => {
  loadPayments()
})
</script>


