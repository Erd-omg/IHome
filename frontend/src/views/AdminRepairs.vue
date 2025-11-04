<template>
  <div style="padding:16px;">
    <el-card>
      <template #header>维修管理</template>

      <el-form :inline="true" :model="search" style="margin-bottom:12px;">
        <el-form-item label="学号"><el-input v-model="search.studentId" clearable /></el-form-item>
        <el-form-item label="宿舍ID"><el-input v-model="search.dormitoryId" clearable /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="search.repairType" clearable style="width:140px;">
            <el-option label="水电维修" value="水电维修" />
            <el-option label="空调维修" value="空调维修" />
            <el-option label="家具维修" value="家具维修" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="search.status" clearable style="width:120px;">
            <el-option label="待处理" value="待处理" />
            <el-option label="处理中" value="处理中" />
            <el-option label="已完成" value="已完成" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch">搜索</el-button>
          <el-button @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="rows" v-loading="loading" @sort-change="onSort">
        <el-table-column prop="studentId" label="学号" sortable="custom" />
        <el-table-column prop="dormitoryId" label="宿舍ID" sortable="custom" />
        <el-table-column prop="repairType" label="类型" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="urgencyLevel" label="紧急程度" />
        <el-table-column prop="status" label="状态" />
        <el-table-column prop="createdAt" label="创建时间" sortable="custom" />
        <el-table-column label="操作" width="240">
          <template #default="{ row }">
            <div style="display:flex; gap:8px; align-items:center;">
              <el-button size="small" @click="setStatus(row, '处理中')" v-if="row.status==='待处理'">标记处理中</el-button>
              <el-button size="small" type="success" @click="setStatus(row, '已完成')" v-if="row.status!=='已完成'">完成</el-button>
              <el-button size="small" type="danger" @click="deleteRepair(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'

const rows = ref<any[]>([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const sortBy = ref('')
const sortOrder = ref('')

const search = reactive({ studentId: '', dormitoryId: '', repairType: '', status: '' })

async function load() {
  loading.value = true
  try {
    const params: any = { page: page.value, size: size.value }
    if (search.studentId) params.studentId = search.studentId
    if (search.dormitoryId) params.dormitoryId = search.dormitoryId
    if (search.repairType) params.repairType = search.repairType
    if (search.status) params.status = search.status
    if (sortBy.value) params.sort = `${sortBy.value},${sortOrder.value}`
    const { data } = await api.adminRepairs(params)
    const d = data.data
    rows.value = d.content || []
    total.value = d.totalElements || 0
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
function onReset() { search.studentId=''; search.dormitoryId=''; search.repairType=''; search.status=''; page.value=1; load() }

async function setStatus(row: any, status: string) {
  try {
    await api.updateRepairStatusAdmin(row.id, status)
    ElMessage.success('状态已更新')
    load()
  } catch (e: any) {
    ElMessage.error(e.message || '更新失败')
  }
}

async function deleteRepair(row: any) {
  try {
    await ElMessageBox.confirm(`确定要删除维修工单吗？`, '确认删除', {
      type: 'warning'
    })
    
    await api.deleteRepair(row.id)
    ElMessage.success('删除成功')
    load()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

onMounted(load)
</script>







