<template>
  <div style="padding:16px;">
    <el-card>
      <template #header>调换申请审核</template>
      <div style="display:flex; gap:8px; margin-bottom:12px;">
        <el-select v-model="status" placeholder="状态" clearable style="width:160px;">
          <el-option label="待审核" value="待审核" />
          <el-option label="已通过" value="已通过" />
          <el-option label="已拒绝" value="已拒绝" />
        </el-select>
        <el-button type="primary" @click="load">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>
      <el-table :data="rows" v-loading="loading" @sort-change="handleSortChange">
        <el-table-column prop="applicantId" label="申请人学号" width="140" sortable="custom" />
        <el-table-column prop="applicantName" label="申请人" width="120">
          <template #default="{ row }">
            {{ row.applicantName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="currentBedId" label="当前床位ID" width="140" />
        <el-table-column prop="targetBedId" label="目标床位ID" width="140" />
        <el-table-column prop="reason" label="调换原因" min-width="150" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280">
          <template #default="{ row }">
            <div style="display:flex; gap:8px; align-items:center;">
              <el-button 
                size="small" 
                type="success" 
                @click="update(row, '已通过')" 
                :disabled="row.status !== '待审核'"
              >
                同意
              </el-button>
              <el-button 
                size="small" 
                type="danger" 
                @click="update(row, '已拒绝')" 
                :disabled="row.status !== '待审核'"
              >
                拒绝
              </el-button>
              <el-button 
                size="small" 
                type="warning" 
                @click="deleteExchange(row)"
              >
                删除
              </el-button>
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
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'

const rows = ref<any[]>([])
const students = ref<any[]>([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const status = ref('')
const sortParams = ref({ sortBy: '', sortOrder: '' })

async function load() {
  loading.value = true
  try {
    const params: any = { page: page.value, size: size.value }
    if (status.value) params.status = status.value
    if (sortParams.value.sortBy) {
      params.sort = `${sortParams.value.sortBy},${sortParams.value.sortOrder}`
    }
    const { data } = await api.adminListExchanges(params)
    const d = data.data
    rows.value = d.content || []
    total.value = d.totalElements || 0
    
    // 加载学生信息
    if (rows.value.length > 0) {
      await loadStudentNames()
    }
  } finally {
    loading.value = false
  }
}

async function loadStudentNames() {
  const studentIds = [...new Set(rows.value.map((row: any) => row.applicantId))]
  for (const studentId of studentIds) {
    if (!students.value.find(s => s.id === studentId)) {
      try {
        const { data } = await api.getStudentAdmin(studentId)
        if (data.data) {
          students.value.push(data.data)
        }
      } catch (e) {
        console.error('加载学生信息失败:', e)
      }
    }
  }
  
  // 将学生信息添加到行中
  rows.value = rows.value.map((row: any) => {
    const student = students.value.find(s => s.id === row.applicantId)
    return { ...row, applicantName: student?.name || '-' }
  })
}

function handleReset() {
  status.value = ''
  page.value = 1
  load()
}

const getStatusTagType = (status: string) => {
  switch(status) {
    case '待审核': return 'warning'
    case '已通过': return 'success'
    case '已拒绝': return 'danger'
    default: return 'info'
  }
}

async function update(row: any, s: string) {
  try {
    await ElMessageBox.confirm(
      `确定要${s === '已通过' ? '同意' : '拒绝'}该调换申请吗？`,
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await api.adminUpdateExchangeStatus(row.id, s)
    ElMessage.success('状态更新成功')
    load()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '更新失败')
    }
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

// 处理排序变化
function handleSortChange({ prop, order }: { prop: string; order: string | null }) {
  if (order) {
    sortParams.value.sortBy = prop
    sortParams.value.sortOrder = order === 'ascending' ? 'asc' : 'desc'
  } else {
    sortParams.value.sortBy = ''
    sortParams.value.sortOrder = ''
  }
  page.value = 1
  load()
}

onMounted(load)
</script>







