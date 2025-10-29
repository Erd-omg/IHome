<template>
  <div style="padding:16px;">
    <el-card>
      <template #header>缴费管理</template>

      <el-form :inline="true" :model="search" style="margin-bottom:12px;">
        <el-form-item label="学号"><el-input v-model="search.studentId" clearable /></el-form-item>
        <el-form-item label="缴费类型">
          <el-select v-model="search.paymentType" clearable style="width:140px;">
            <el-option label="住宿费" value="住宿费" />
            <el-option label="电费" value="电费" />
            <el-option label="水费" value="水费" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch">搜索</el-button>
          <el-button @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="rows" v-loading="loading" @sort-change="onSort">
        <el-table-column prop="studentId" label="学号" sortable="custom" />
        <el-table-column prop="amount" label="金额" sortable="custom">
          <template #default="{ row }">¥{{ Number(row.amount).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="paymentType" label="缴费类型" />
        <el-table-column prop="paymentTime" label="缴费时间" sortable="custom" />
      </el-table>

      <div style="display:flex; justify-content:center; margin-top:12px;">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="load"
          @size-change="onSize"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../api'

const rows = ref<any[]>([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const sortBy = ref('')
const sortOrder = ref('')

const search = reactive({ studentId: '', paymentType: '' })

async function load() {
  loading.value = true
  try {
    const params: any = { page: page.value, size: size.value }
    if (search.studentId) params.studentId = search.studentId
    if (search.paymentType) params.paymentType = search.paymentType
    if (sortBy.value) params.sort = `${sortBy.value},${sortOrder.value}`
    const { data } = await api.adminPayments(params)
    const d = data.data
    rows.value = d.content || []
    total.value = d.totalElements || 0
  } catch (e: any) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function onSort({ prop, order }: any) {
  sortBy.value = prop
  sortOrder.value = order === 'ascending' ? 'asc' : 'desc'
  load()
}

function onSize(s: number) { size.value = s; page.value = 1; load() }
function onSearch() { page.value = 1; load() }
function onReset() { search.studentId=''; search.paymentType=''; page.value=1; load() }

onMounted(load)
</script>







