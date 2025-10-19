<template>
  <div style="padding:16px;">
    <el-card>
      <template #header>调换申请审核</template>
      <div style="display:flex; gap:8px; margin-bottom:12px;">
        <el-select v-model="status" placeholder="状态" clearable style="width:160px;">
          <el-option label="未处理" value="未处理" />
          <el-option label="已通过" value="已通过" />
          <el-option label="已驳回" value="已驳回" />
        </el-select>
        <el-button type="primary" @click="load">查询</el-button>
      </div>
      <el-table :data="rows" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="studentId" label="学号" width="140" />
        <el-table-column prop="applicant" label="申请人" width="140" />
        <el-table-column prop="currentDorm" label="当前宿舍" width="140" />
        <el-table-column prop="targetDorm" label="申请宿舍" width="140" />
        <el-table-column prop="reason" label="调换原因" />
        <el-table-column prop="status" label="状态" width="100" />
        <el-table-column label="操作" width="250">
          <template #default="{ row }">
            <el-button size="small" type="success" @click="update(row, '已通过')" :disabled="row.status==='已通过'">同意</el-button>
            <el-button size="small" type="danger" @click="update(row, '已拒绝')" :disabled="row.status==='已拒绝'">拒绝</el-button>
            <el-button size="small" type="warning" @click="deleteExchange(row)">删除</el-button>
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
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'

const rows = ref<any[]>([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const status = ref('')

async function load() {
  loading.value = true
  try {
    const params: any = { page: page.value, size: size.value }
    if (status.value) params.status = status.value
    const { data } = await api.adminExchanges(params)
    const d = data.data
    rows.value = d.content || []
    total.value = d.totalElements || 0
  } finally {
    loading.value = false
  }
}

async function update(row: any, s: string) {
  try {
    await api.updateExchangeStatus(row.id, s)
    ElMessage.success('状态更新成功')
    load()
  } catch (error: any) {
    ElMessage.error(error.message || '更新失败')
  }
}

async function deleteExchange(row: any) {
  try {
    await ElMessageBox.confirm(`确定要删除调换申请吗？`, '确认删除', {
      type: 'warning'
    })
    
    await api.deleteExchange(row.id)
    ElMessage.success('删除成功')
    load()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

function onSize(s: number) { size.value = s; page.value = 1; load() }

onMounted(load)
</script>







