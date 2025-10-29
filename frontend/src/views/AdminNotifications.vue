<template>
  <div class="admin-notifications">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>通知管理</span>
          <div class="header-actions">
            <el-button type="primary" @click="showCreateDialog = true">
              发送系统通知
            </el-button>
            <el-button @click="cleanExpiredNotifications">
              清理过期通知
            </el-button>
          </div>
        </div>
      </template>

      <!-- 通知统计 -->
      <div class="notification-stats">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-statistic title="总通知数" :value="stats.total" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="未读通知" :value="stats.unread" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="今日新增" :value="stats.today" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="过期通知" :value="stats.expired" />
          </el-col>
        </el-row>
      </div>

      <!-- 筛选条件 -->
      <div class="filter-section">
        <el-form :model="filterForm" inline>
          <el-form-item label="通知类型">
            <el-select v-model="filterForm.type" placeholder="全部类型" clearable style="width:150px;">
              <el-option label="系统通知" value="system" />
              <el-option label="维修通知" value="repair" />
              <el-option label="缴费通知" value="payment" />
              <el-option label="调换通知" value="switch" />
              <el-option label="分配通知" value="allocation" />
            </el-select>
          </el-form-item>
          <el-form-item label="优先级">
            <el-select v-model="filterForm.priority" placeholder="全部优先级" clearable style="width:150px;">
              <el-option label="低" value="low" />
              <el-option label="普通" value="normal" />
              <el-option label="高" value="high" />
              <el-option label="紧急" value="urgent" />
            </el-select>
          </el-form-item>
          <el-form-item label="接收者类型">
            <el-select v-model="filterForm.receiverType" placeholder="全部用户" clearable style="width:150px;">
              <el-option label="全员" value="all" />
              <el-option label="学生" value="student" />
              <el-option label="管理员" value="admin" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadNotifications">查询</el-button>
            <el-button @click="resetFilter">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 通知列表 -->
      <el-table 
        :data="notifications" 
        v-loading="loading"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)">
              {{ getTypeText(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="100">
          <template #default="{ row }">
            <el-tag :type="getPriorityTagType(row.priority)">
              {{ getPriorityText(row.priority) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="receiverType" label="接收者类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getReceiverTagType(row)">
              {{ getReceiverText(row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="receiverId" label="接收者ID" width="120" />
        <el-table-column prop="isRead" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.isRead ? 'success' : 'warning'">
              {{ row.isRead ? '已读' : '未读' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <div style="display:flex; gap:8px; align-items:center;">
              <el-button size="small" @click="viewNotification(row)">查看</el-button>
              <el-button size="small" type="danger" @click="deleteNotification(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="loadNotifications"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <!-- 发送系统通知对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      title="发送系统通知"
      width="600px"
      :before-close="handleCloseDialog"
    >
      <el-form :model="notificationForm" :rules="notificationRules" ref="notificationFormRef" label-width="100px">
        <el-form-item label="通知标题" prop="title">
          <el-input v-model="notificationForm.title" placeholder="请输入通知标题" />
        </el-form-item>
        <el-form-item label="通知内容" prop="content">
          <el-input
            v-model="notificationForm.content"
            type="textarea"
            :rows="4"
            placeholder="请输入通知内容"
          />
        </el-form-item>
        <el-form-item label="通知类型" prop="type">
          <el-select v-model="notificationForm.type" placeholder="请选择通知类型">
            <el-option label="系统通知" value="system" />
            <el-option label="维修通知" value="repair" />
            <el-option label="缴费通知" value="payment" />
            <el-option label="调换通知" value="switch" />
            <el-option label="分配通知" value="allocation" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-select v-model="notificationForm.priority" placeholder="请选择优先级">
            <el-option label="低" value="low" />
            <el-option label="普通" value="normal" />
            <el-option label="高" value="high" />
            <el-option label="紧急" value="urgent" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="sendSystemNotification" :loading="sending">
          发送通知
        </el-button>
      </template>
    </el-dialog>

    <!-- 查看通知详情对话框 -->
    <el-dialog
      v-model="showViewDialog"
      title="通知详情"
      width="500px"
    >
      <div v-if="selectedNotification" class="notification-detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="标题">
            {{ selectedNotification.title }}
          </el-descriptions-item>
          <el-descriptions-item label="内容">
            {{ selectedNotification.content }}
          </el-descriptions-item>
          <el-descriptions-item label="类型">
            <el-tag :type="getTypeTagType(selectedNotification.type)">
              {{ getTypeText(selectedNotification.type) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="优先级">
            <el-tag :type="getPriorityTagType(selectedNotification.priority)">
              {{ getPriorityText(selectedNotification.priority) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="接收者">
            {{ getReceiverText(selectedNotification) }} - {{ selectedNotification.receiverId === 'ALL' || selectedNotification.receiverId === 'all' ? '全员' : selectedNotification.receiverId }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="selectedNotification.isRead ? 'success' : 'warning'">
              {{ selectedNotification.isRead ? '已读' : '未读' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatDateTime(selectedNotification.createTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="阅读时间" v-if="selectedNotification.readTime">
            {{ formatDateTime(selectedNotification.readTime) }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'

// 响应式数据
const loading = ref(false)
const sending = ref(false)
const showCreateDialog = ref(false)
const showViewDialog = ref(false)
const notifications = ref<any[]>([])
const selectedNotifications = ref<any[]>([])
const selectedNotification = ref<any>(null)

// 统计数据
const stats = reactive({
  total: 0,
  unread: 0,
  today: 0,
  expired: 0
})

// 分页数据
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 筛选表单
const filterForm = reactive({
  type: '',
  priority: '',
  receiverType: ''
})

// 通知表单
const notificationForm = reactive({
  title: '',
  content: '',
  type: 'system',
  priority: 'normal'
})

// 表单验证规则
const notificationRules = {
  title: [
    { required: true, message: '请输入通知标题', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入通知内容', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择通知类型', trigger: 'change' }
  ],
  priority: [
    { required: true, message: '请选择优先级', trigger: 'change' }
  ]
}

// 获取类型标签类型
const getTypeTagType = (type: string) => {
  const typeMap: Record<string, string> = {
    system: 'primary',
    repair: 'warning',
    payment: 'success',
    switch: 'info',
    allocation: 'primary'
  }
  return typeMap[type] || 'primary'
}

// 获取类型文本
const getTypeText = (type: string) => {
  const typeMap: Record<string, string> = {
    system: '系统通知',
    repair: '维修通知',
    payment: '缴费通知',
    switch: '调换通知',
    allocation: '分配通知'
  }
  return typeMap[type] || type
}

// 获取优先级标签类型
const getPriorityTagType = (priority: string) => {
  const priorityMap: Record<string, string> = {
    low: 'info',
    normal: 'primary',
    high: 'warning',
    urgent: 'danger'
  }
  return priorityMap[priority] || 'primary'
}

// 获取接收者文本
const getReceiverText = (row: any) => {
  if (row.receiverId === 'ALL' || row.receiverId === 'all') {
    return '全员'
  }
  return row.receiverType === 'student' ? '学生' : '管理员'
}

// 获取接收者标签类型
const getReceiverTagType = (row: any) => {
  if (row.receiverId === 'ALL' || row.receiverId === 'all') {
    return 'warning'
  }
  return row.receiverType === 'student' ? 'primary' : 'success'
}

// 获取优先级文本
const getPriorityText = (priority: string) => {
  const priorityMap: Record<string, string> = {
    low: '低',
    normal: '普通',
    high: '高',
    urgent: '紧急'
  }
  return priorityMap[priority] || '普通'
}

// 格式化日期时间
const formatDateTime = (dateTime: string) => {
  return new Date(dateTime).toLocaleString()
}

// 加载通知列表
const loadNotifications = async () => {
  loading.value = true
  try {
    const { data } = await api.adminNotifications({
      page: pagination.page,
      size: pagination.size,
      ...filterForm
    })
    const response = data.data
    notifications.value = response.content || []
    pagination.total = response.totalElements || 0
    
    // 重新加载统计数据
    loadStats()
  } catch (error) {
    console.error('加载通知列表失败:', error)
    ElMessage.error('加载通知列表失败')
  } finally {
    loading.value = false
  }
}

// 重置筛选条件
const resetFilter = () => {
  filterForm.type = ''
  filterForm.priority = ''
  filterForm.receiverType = ''
  loadNotifications()
}

// 处理选择变化
const handleSelectionChange = (selection: any[]) => {
  selectedNotifications.value = selection
}

// 处理分页大小变化
const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.page = 1
  loadNotifications()
}

// 发送系统通知
const sendSystemNotification = async () => {
  try {
    sending.value = true
    await api.sendSystemNotification(notificationForm)
    ElMessage.success('系统通知发送成功')
    showCreateDialog.value = false
    resetNotificationForm()
    loadNotifications()
  } catch (error) {
    console.error('发送系统通知失败:', error)
    ElMessage.error('发送系统通知失败')
  } finally {
    sending.value = false
  }
}

// 重置通知表单
const resetNotificationForm = () => {
  notificationForm.title = ''
  notificationForm.content = ''
  notificationForm.type = 'system'
  notificationForm.priority = 'normal'
}

// 关闭对话框
const handleCloseDialog = () => {
  showCreateDialog.value = false
  resetNotificationForm()
}

// 查看通知详情
const viewNotification = (notification: any) => {
  selectedNotification.value = notification
  showViewDialog.value = true
  
  // 如果通知的接收者为当前登录用户且未读，则自动标记为已读
  if (!notification.isRead) {
    // 获取当前用户信息
    const currentUser = JSON.parse(localStorage.getItem('user') || '{}')
    if (currentUser.id && notification.receiverId === currentUser.id) {
      // 标记为已读
      api.markAsRead(notification.id, notification.receiverId, notification.receiverType).catch(() => {
        // 静默失败
      })
    }
  }
}

// 删除通知
const deleteNotification = async (notification: any) => {
  try {
    await ElMessageBox.confirm('确定要删除这条通知吗？', '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await api.deleteNotificationAdmin(notification.id)
    ElMessage.success('删除成功')
    loadNotifications()
    loadStats()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除通知失败:', error)
      ElMessage.error(error.message || '删除通知失败')
    }
  }
}

// 清理过期通知
const cleanExpiredNotifications = async () => {
  try {
    await ElMessageBox.confirm('确定要清理所有过期通知吗？', '确认清理', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await api.cleanExpiredNotifications()
    ElMessage.success('过期通知清理完成')
    loadNotifications()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('清理过期通知失败:', error)
      ElMessage.error('清理过期通知失败')
    }
  }
}

// 加载统计数据
const loadStats = async () => {
  try {
    const { data } = await api.adminNotifications({ page: 1, size: 1000 })
    const notifications = data.data?.content || []
    
    // 计算统计数据
    stats.total = notifications.length
    stats.unread = notifications.filter(n => !n.isRead).length
    stats.today = notifications.filter(n => {
      const today = new Date().toDateString()
      const nDate = new Date(n.createTime).toDateString()
      return nDate === today
    }).length
    stats.expired = notifications.filter(n => {
      if (n.expireTime) {
        return new Date(n.expireTime) < new Date()
      }
      return false
    }).length
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

// 组件挂载时加载数据
onMounted(() => {
  loadNotifications()
  loadStats()
})
</script>

<style scoped>
.admin-notifications {
  padding: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.notification-stats {
  margin-bottom: 20px;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.filter-section {
  margin-bottom: 20px;
  padding: 16px;
  background-color: #fafafa;
  border-radius: 4px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.notification-detail {
  padding: 16px 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .header-actions {
    width: 100%;
    justify-content: flex-end;
  }
  
  .filter-section .el-form {
    flex-direction: column;
  }
  
  .filter-section .el-form-item {
    margin-right: 0;
    margin-bottom: 16px;
  }
}
</style>


