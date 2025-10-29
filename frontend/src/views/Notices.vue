<template>
  <div style="padding:24px;">
    <div style="margin-bottom: 16px;">
      <el-button type="text" @click="$router.back()" style="padding: 0;">
        <span style="font-size: 16px;">← 返回</span>
      </el-button>
    </div>
    <h2 style="margin: 0; font-size: 20px; font-weight: 600;">通知公告</h2>
    
    <!-- 搜索筛选 -->
    <el-card style="margin-top:16px;">
      <el-form :model="searchForm" :inline="true" style="margin-bottom: 16px;">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="请输入标题关键词" clearable />
        </el-form-item>
        <el-form-item label="发布时间">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 公告列表 -->
    <el-card style="margin-top:16px;">
      <template #header>
        <div style="display:flex; justify-content:space-between; align-items:center;">
          <span>公告列表</span>
          <div>
            <el-button size="small" @click="loadNotices">刷新</el-button>
          </div>
        </div>
      </template>
      
      <div v-if="notices.length > 0">
        <div v-for="notice in notices" :key="notice.id" class="notice-item" @click="viewNotice(notice.id)">
          <div class="notice-header">
            <div class="notice-title">{{ notice.title }}</div>
            <div class="notice-time">{{ formatDate(notice.createTime) }}</div>
          </div>
          <div class="notice-content">{{ notice.content }}</div>
          <div class="notice-footer">
            <el-tag size="small" type="info">公告</el-tag>
            <el-link type="primary" size="small">查看详情 →</el-link>
          </div>
        </div>
      </div>
      
      <el-empty v-else description="暂无公告" />
      
      <!-- 分页 -->
      <div style="display:flex; justify-content:center; margin-top:24px;">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50]"
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
import { useRouter } from 'vue-router'
import { api } from '../api'
import { ElMessage } from 'element-plus'

const router = useRouter()

const notices = ref<any[]>([])
const loading = ref(false)

// 搜索表单
const searchForm = reactive({
  keyword: '',
  dateRange: null as [string, string] | null
})

// 分页参数
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 格式化日期
const formatDate = (date: string) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

// 加载公告列表
const loadNotices = async () => {
  loading.value = true
  try {
    const params: any = {
      page: pagination.page,
      size: pagination.size
    }
    
    // 添加搜索条件
    if (searchForm.keyword) {
      params.keyword = searchForm.keyword
    }
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }
    
    const response = await api.listNotices(params)
    const data = response.data.data
    
    if (data && data.content) {
      notices.value = data.content
      pagination.total = data.totalElements
    } else {
      notices.value = []
      pagination.total = 0
    }
  } catch (error) {
    console.error('加载公告列表失败:', error)
    ElMessage.error('加载公告失败')
    notices.value = []
  } finally {
    loading.value = false
  }
}

// 查看公告详情
const viewNotice = (id: string | number) => {
  router.push(`/notices/${id}`)
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  loadNotices()
}

// 重置搜索
const handleReset = () => {
  searchForm.keyword = ''
  searchForm.dateRange = null
  pagination.page = 1
  loadNotices()
}

// 页码变化
const handleCurrentChange = (page: number) => {
  pagination.page = page
  loadNotices()
}

// 每页条数变化
const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.page = 1
  loadNotices()
}

onMounted(() => {
  loadNotices()
})
</script>

<style scoped>
.notice-item {
  padding: 20px;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  margin-bottom: 16px;
  cursor: pointer;
  transition: all 0.3s;
  background-color: #fff;
}

.notice-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
  transform: translateY(-1px);
}

.notice-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.notice-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  flex: 1;
  margin-right: 16px;
  line-height: 1.4;
}

.notice-time {
  font-size: 12px;
  color: #999;
  white-space: nowrap;
}

.notice-content {
  color: #666;
  line-height: 1.6;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.notice-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>


