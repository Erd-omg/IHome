<template>
  <div style="padding:16px;">
    <el-card>
      <template #header>
        <div style="display:flex; justify-content:space-between; align-items:center;">
          <span>宿舍管理</span>
          <div>
            <el-button type="primary" @click="showAddDialog = true">
              <el-icon><Plus /></el-icon>
              添加宿舍
            </el-button>
            <el-button @click="loadDormitories">刷新</el-button>
          </div>
        </div>
      </template>

      <!-- 搜索筛选 -->
      <el-form :model="searchForm" :inline="true" style="margin-bottom:16px;">
        <el-form-item label="楼栋">
          <el-select v-model="searchForm.buildingId" placeholder="请选择楼栋" clearable style="width:160px;">
            <el-option label="B001 - 一号男生公寓" value="B001" />
            <el-option label="B002 - 二号女生公寓" value="B002" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width:120px;">
            <el-option label="可用" value="可用" />
            <el-option label="已满" value="已满" />
            <el-option label="维修中" value="维修中" />
            <el-option label="停用" value="停用" />
          </el-select>
        </el-form-item>
        <el-form-item label="宿舍ID">
          <el-autocomplete
            v-model="searchForm.id"
            :fetch-suggestions="searchDormitories"
            placeholder="请输入宿舍ID或房间号"
            clearable
            style="width:200px;"
            @select="handleDormitorySelect"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 宿舍列表 -->
      <el-table :data="dormitories" v-loading="loading" @sort-change="handleSortChange">
        <el-table-column prop="id" label="宿舍ID" sortable="custom" width="120" />
        <el-table-column prop="buildingId" label="楼栋" sortable="custom" width="120" />
        <el-table-column prop="roomNumber" label="房间号" sortable="custom" width="100" />
        <el-table-column prop="bedCount" label="床位总数" sortable="custom" width="100" />
        <el-table-column prop="currentOccupancy" label="当前入住" sortable="custom" width="100" />
        <el-table-column label="使用率" width="120">
          <template #default="{ row }">
            <el-progress 
              :percentage="getUsageRate(row)" 
              :color="getUsageColor(getUsageRate(row))"
              :stroke-width="8"
            />
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="viewBeds(row)">查看</el-button>
            <el-button size="small" type="primary" @click="editDormitory(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteDormitory(row)">删除</el-button>
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

    <!-- 添加/编辑宿舍弹窗 -->
    <el-dialog 
      v-model="showAddDialog" 
      :title="editingDormitory ? '编辑宿舍' : '添加宿舍'"
      width="600px"
    >
      <el-form :model="dormitoryForm" :rules="dormitoryRules" ref="dormitoryFormRef" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="宿舍ID" prop="id">
              <el-input v-model="dormitoryForm.id" :disabled="editingDormitory" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="楼栋" prop="buildingId">
              <el-select v-model="dormitoryForm.buildingId" style="width:100%">
                <el-option label="B001 - 一号男生公寓" value="B001" />
                <el-option label="B002 - 二号女生公寓" value="B002" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="房间号" prop="roomNumber">
              <el-input v-model="dormitoryForm.roomNumber" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="床位总数" prop="bedCount">
              <el-input-number v-model="dormitoryForm.bedCount" :min="1" :max="8" :disabled="editingDormitory" style="width:100%" />
              <div style="font-size:12px; color:#909399; margin-top:4px;">编辑模式下此字段不可修改</div>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="状态" prop="status">
          <el-select v-model="dormitoryForm.status" style="width:200px;">
            <el-option label="可用" value="可用" />
            <el-option label="已满" value="已满" />
            <el-option label="维修中" value="维修中" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="saveDormitory" :loading="saving">保存</el-button>
      </template>
    </el-dialog>

    <!-- 床位详情弹窗 -->
    <el-dialog v-model="showBedsDialog" title="床位详情" width="900px">
      <div v-if="currentDormitory">
        <div style="margin-bottom:16px;">
          <el-tag type="info">宿舍ID: {{ currentDormitory.id }}</el-tag>
          <el-tag type="success" style="margin-left:8px;">房间号: {{ currentDormitory.roomNumber }}</el-tag>
        </div>
        <el-table :data="beds" border>
          <el-table-column prop="id" label="床位ID" width="140" />
          <el-table-column prop="bedNumber" label="床号" width="80" />
          <el-table-column prop="bedType" label="床型" width="100" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === '可用' ? 'success' : 'danger'" size="small">
                {{ row.status }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="occupantName" label="使用者姓名" width="120">
            <template #default="{ row }">
              <span v-if="row.occupantName">{{ row.occupantName }}</span>
              <span v-else style="color:#ccc;">-</span>
            </template>
          </el-table-column>
          <el-table-column prop="occupantId" label="使用者学号" width="120">
            <template #default="{ row }">
              <span v-if="row.occupantId">{{ row.occupantId }}</span>
              <span v-else style="color:#ccc;">-</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button 
                size="small" 
                type="primary" 
                text 
                @click="viewBedDetail(row)"
              >
                详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>

    <!-- 床位详情子弹窗 -->
    <el-dialog v-model="showBedDetailDialog" title="床位详细信息" width="600px">
      <el-descriptions :column="1" border v-if="currentBed">
        <el-descriptions-item label="床位ID">{{ currentBed.id }}</el-descriptions-item>
        <el-descriptions-item label="床号">{{ currentBed.bedNumber }}</el-descriptions-item>
        <el-descriptions-item label="床型">{{ currentBed.bedType }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentBed.status === '可用' ? 'success' : 'danger'">
            {{ currentBed.status }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="使用者姓名">
          {{ currentBed.occupantName || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="使用者学号">
          {{ currentBed.occupantId || '-' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { api } from '../api'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const dormitories = ref<any[]>([])
const beds = ref<any[]>([])
const loading = ref(false)
const saving = ref(false)
const showAddDialog = ref(false)
const showBedsDialog = ref(false)
const showBedDetailDialog = ref(false)
const editingDormitory = ref<any>(null)
const currentDormitory = ref<any>(null)
const currentBed = ref<any>(null)
const dormitoryFormRef = ref<FormInstance>()

// 搜索表单
const searchForm = reactive({
  buildingId: '',
  status: '',
  id: ''
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

// 宿舍表单
const dormitoryForm = reactive({
  id: '',
  buildingId: '',
  roomNumber: '',
  bedCount: 4,
  status: '可用'
})

// 表单验证规则
const dormitoryRules: FormRules = {
  id: [
    { required: true, message: '请输入宿舍ID', trigger: 'blur' }
  ],
  buildingId: [
    { required: true, message: '请选择楼栋', trigger: 'change' }
  ],
  roomNumber: [
    { required: true, message: '请输入房间号', trigger: 'blur' }
  ],
  bedCount: [
    { required: true, message: '请输入床位总数', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

// 计算使用率
const getUsageRate = (row: any) => {
  if (!row.bedCount) return 0
  return Math.round((row.currentOccupancy / row.bedCount) * 100)
}

// 获取使用率颜色
const getUsageColor = (percentage: number) => {
  if (percentage >= 90) return '#f56c6c'
  if (percentage >= 70) return '#e6a23c'
  return '#67c23a'
}

// 获取状态标签类型
const getStatusTagType = (status: string) => {
  switch (status) {
    case '可用': return 'success'
    case '已满': return 'danger'
    case '维修中': return 'warning'
    default: return 'info'
  }
}

// 加载宿舍列表
const loadDormitories = async () => {
  loading.value = true
  try {
    const params: any = {
      page: pagination.page,
      size: pagination.size
    }
    
    // 添加搜索条件
    if (searchForm.buildingId) {
      params.buildingId = searchForm.buildingId
    }
    if (searchForm.status) {
      params.status = searchForm.status
    }
    if (searchForm.id) {
      params.id = searchForm.id
    }
    
    // 添加排序参数
    if (sortParams.sortBy) {
      params.sort = `${sortParams.sortBy},${sortParams.sortOrder}`
    }
    
    const response = await api.adminDormitories(params)
    const data = response.data.data
    
    if (data && data.content) {
      dormitories.value = data.content
      pagination.total = data.totalElements
    } else {
      dormitories.value = []
      pagination.total = 0
    }
  } catch (error) {
    console.error('加载宿舍列表失败:', error)
    ElMessage.error('加载宿舍列表失败')
    dormitories.value = []
  } finally {
    loading.value = false
  }
}

// 搜索宿舍（用于autocomplete）
const searchDormitories = async (queryString: string, cb: (results: any[]) => void) => {
  if (!queryString) {
    cb([])
    return
  }
  
  try {
    const response = await api.adminDormitories({ id: queryString, page: 1, size: 10 })
    const data = response.data.data
    const results = (data?.content || []).map((dorm: any) => ({
      value: dorm.id,
      label: dorm.id
    }))
    cb(results)
  } catch (error) {
    console.error('搜索宿舍失败:', error)
    cb([])
  }
}

// 处理宿舍选择
const handleDormitorySelect = (item: any) => {
  searchForm.id = item.value
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  loadDormitories()
}

// 重置搜索
const handleReset = () => {
  searchForm.buildingId = ''
  searchForm.status = ''
  searchForm.id = ''
  pagination.page = 1
  loadDormitories()
}

// 排序变化
const handleSortChange = ({ prop, order }: { prop: string; order: string }) => {
  sortParams.sortBy = prop
  sortParams.sortOrder = order === 'ascending' ? 'asc' : 'desc'
  loadDormitories()
}

// 页码变化
const handleCurrentChange = (page: number) => {
  pagination.page = page
  loadDormitories()
}

// 每页条数变化
const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.page = 1
  loadDormitories()
}

// 查看床位
const viewBeds = async (row: any) => {
  currentDormitory.value = row
  try {
    const response = await api.listBeds(row.id)
    const bedList = response.data.data || []
    
    // 获取每个床位的使用者信息
    beds.value = []
    for (const bed of bedList) {
      const bedInfo: any = { ...bed }
      try {
        // 如果床位被占用，通过分配记录查找使用者信息
        if (bed.status === '已占用') {
          try {
            const allocationRes = await api.adminAllocations({ bedId: bed.id, status: '在住', size: 100 })
            const allocations = allocationRes.data.data?.content || []
            if (allocations && allocations.length > 0) {
              const allocation = allocations.find((a: any) => a.bedId === bed.id) || allocations[0]
              const studentRes = await api.getStudentAdmin(allocation.studentId)
              const studentData = studentRes.data.data
              bedInfo.occupantName = studentData?.name || ''
              bedInfo.occupantId = studentData?.id || ''
            } else {
              bedInfo.occupantName = ''
              bedInfo.occupantId = ''
            }
          } catch (e) {
            console.error('获取使用者信息失败:', e)
            bedInfo.occupantName = ''
            bedInfo.occupantId = ''
          }
        } else {
          bedInfo.occupantName = ''
          bedInfo.occupantId = ''
        }
      } catch (e) {
        console.error('处理床位信息失败:', e)
        bedInfo.occupantName = ''
        bedInfo.occupantId = ''
      }
      beds.value.push(bedInfo)
    }
    
    showBedsDialog.value = true
  } catch (error) {
    console.error('加载床位信息失败:', error)
    ElMessage.error('加载床位信息失败')
  }
}

// 查看床位详情
const viewBedDetail = (bed: any) => {
  currentBed.value = bed
  showBedDetailDialog.value = true
}

// 编辑宿舍
const editDormitory = (row: any) => {
  editingDormitory.value = row
  Object.assign(dormitoryForm, row)
  showAddDialog.value = true
}

// 删除宿舍
const deleteDormitory = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除宿舍 ${row.id} 吗？此操作不可恢复。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await api.deleteDormitory(row.id)
    ElMessage.success('宿舍删除成功')
    await loadDormitories()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 保存宿舍
const saveDormitory = async () => {
  if (!dormitoryFormRef.value) return
  
  try {
    await dormitoryFormRef.value.validate()
    saving.value = true
    
    if (editingDormitory.value) {
      // 编辑宿舍
      await api.updateDormitory(dormitoryForm.id, dormitoryForm)
      ElMessage.success('宿舍更新成功')
    } else {
      // 添加宿舍
      await api.createDormitory(dormitoryForm)
      ElMessage.success('宿舍添加成功')
    }
    
    showAddDialog.value = false
    resetForm()
    await loadDormitories()
  } catch (error: any) {
    if (error !== false) { // 不是表单验证错误
      ElMessage.error(error.message || '保存失败')
    }
  } finally {
    saving.value = false
  }
}

// 重置表单
const resetForm = () => {
  editingDormitory.value = null
  Object.assign(dormitoryForm, {
    id: '',
    buildingId: '',
    roomNumber: '',
    bedCount: 4,
    status: '可用'
  })
}

onMounted(() => {
  loadDormitories()
})
</script>

<style scoped>
.bed-card {
  text-align: center;
  margin-bottom: 16px;
  border: 2px solid #f0f0f0;
  transition: all 0.3s;
}

.bed-card:hover {
  border-color: #409eff;
}

.bed-card.occupied {
  background-color: #f5f5f5;
  border-color: #dcdfe6;
}

.bed-number {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 8px;
  color: #333;
}

.bed-type {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.bed-status {
  margin-top: 8px;
}
</style>


