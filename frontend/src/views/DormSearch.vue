<template>
  <div style="padding:24px;">
    <el-page-header content="宿舍信息查询" @back="$router.back()" />
    <el-card style="margin-top:12px;">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="宿舍号">
          <el-input v-model="searchForm.dormNo" placeholder="请输入宿舍号" clearable />
        </el-form-item>
        <el-form-item label="楼栋">
          <el-select v-model="searchForm.buildingId" placeholder="请选择楼栋" clearable>
            <el-option label="A栋" value="A01" />
            <el-option label="B栋" value="B01" />
            <el-option label="C栋" value="C01" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="可用" value="可用" />
            <el-option label="已满" value="已满" />
            <el-option label="维修中" value="维修中" />
          </el-select>
        </el-form-item>
        <el-form-item label="创建时间">
          <el-date-picker 
            v-model="searchForm.createRange" 
            type="daterange" 
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            clearable
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <el-card style="margin-top:12px;">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span>宿舍列表</span>
          <el-button type="primary" @click="loadDormitories">刷新</el-button>
        </div>
      </template>
      
      <el-table 
        :data="rows" 
        style="width:100%;" 
        v-loading="loading"
        @sort-change="handleSortChange"
      >
        <el-table-column prop="id" label="宿舍号" sortable="custom" />
        <el-table-column prop="buildingId" label="楼栋" sortable="custom" />
        <el-table-column prop="name" label="房间名称" />
        <el-table-column prop="bedCount" label="床位总数" sortable="custom" />
        <el-table-column prop="currentOccupancy" label="当前入住" sortable="custom" />
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
        <el-table-column label="空缺床位">
          <template #default="{ row }">
            {{ row.bedCount - row.currentOccupancy }}
          </template>
        </el-table-column>
        <el-table-column label="入住率">
          <template #default="{ row }">
            {{ row.bedCount > 0 ? Math.round((row.currentOccupancy / row.bedCount) * 100) : 0 }}%
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="viewDetail(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
      
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

const rows = ref<any[]>([])
const loading = ref(false)

// 搜索表单
const searchForm = reactive({
  dormNo: '',
  buildingId: '',
  status: '',
  createRange: [] as any
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

// 获取状态标签类型
const getStatusTagType = (status: string) => {
  switch (status) {
    case '可用': return 'success'
    case '已满': return 'warning'
    case '维修中': return 'danger'
    default: return ''
  }
}

// 加载宿舍列表
async function loadDormitories() {
  loading.value = true
  try {
    const params: PaginationParams = {
      page: pagination.page,
      size: pagination.size
    }
    
    // 添加搜索条件
    if (searchForm.dormNo) {
      params.name = searchForm.dormNo
    }
    if (searchForm.buildingId) {
      params.buildingId = searchForm.buildingId
    }
    if (searchForm.status) {
      params.status = searchForm.status
    }
    if (searchForm.createRange && searchForm.createRange.length === 2) {
      params.startDate = searchForm.createRange[0]
      params.endDate = searchForm.createRange[1]
    }
    
    // 添加排序参数
    if (sortParams.sortBy) {
      params.sort = `${sortParams.sortBy},${sortParams.sortOrder}`
    }
    
    const response = await api.listDorms(params)
    const data = response.data.data
    
    if (Array.isArray(data)) {
      // 如果后端返回的是数组（非分页格式）
      rows.value = data
      pagination.total = data.length
    } else if (data && data.content) {
      // 如果后端返回的是分页格式
      rows.value = data.content
      pagination.total = data.totalElements
    } else {
      rows.value = []
      pagination.total = 0
    }
  } catch (error: any) {
    ElMessage.error(error.message || '加载失败')
    rows.value = []
  } finally {
    loading.value = false
  }
}

// 搜索
function handleSearch() {
  pagination.page = 1
  loadDormitories()
}

// 重置搜索
function handleReset() {
  searchForm.dormNo = ''
  searchForm.buildingId = ''
  searchForm.status = ''
  searchForm.createRange = []
  pagination.page = 1
  loadDormitories()
}

// 排序变化
function handleSortChange({ prop, order }: { prop: string; order: string }) {
  sortParams.sortBy = prop
  sortParams.sortOrder = order === 'ascending' ? 'asc' : 'desc'
  loadDormitories()
}

// 页码变化
function handleCurrentChange(page: number) {
  pagination.page = page
  loadDormitories()
}

// 每页条数变化
function handleSizeChange(size: number) {
  pagination.size = size
  pagination.page = 1
  loadDormitories()
}

// 查看详情
function viewDetail(row: any) {
  ElMessage.info(`查看宿舍详情: ${row.id}`)
  // 这里可以打开详情弹窗或跳转到详情页面
}


// 页面加载时获取数据
onMounted(() => {
  loadDormitories()
})
</script>


