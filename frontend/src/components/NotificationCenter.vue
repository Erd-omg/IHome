<template>
  <div class="notification-center">
    <!-- 通知图标和未读数量 -->
    <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="notification-badge">
      <el-button 
        type="text" 
        @click="showNotificationList = !showNotificationList"
        class="notification-trigger"
      >
        <el-icon size="20">
          <Bell />
        </el-icon>
      </el-button>
    </el-badge>

    <!-- 通知列表弹窗 -->
    <el-drawer
      v-model="showNotificationList"
      title="消息通知"
      direction="rtl"
      size="400px"
      :before-close="handleClose"
    >
      <div class="notification-content">
        <!-- 操作按钮 -->
        <div class="notification-actions">
          <el-button 
            size="small" 
            @click="markAllAsRead"
            :disabled="unreadCount === 0"
          >
            全部标记为已读
          </el-button>
          <el-button 
            size="small" 
            @click="refreshNotifications"
            :loading="loading"
          >
            刷新
          </el-button>
        </div>

        <!-- 通知列表 -->
        <div class="notification-list">
          <div 
            v-for="notification in notifications" 
            :key="notification.id"
            class="notification-item"
            :class="{ 'unread': !notification.isRead }"
            @click="markAsRead(notification)"
          >
            <div class="notification-header">
              <div class="notification-title">
                <el-icon v-if="getNotificationIcon(notification.type)" class="notification-icon">
                  <component :is="getNotificationIcon(notification.type)" />
                </el-icon>
                {{ notification.title }}
                <el-tag 
                  v-if="notification.priority === 'high' || notification.priority === 'urgent'"
                  :type="notification.priority === 'urgent' ? 'danger' : 'warning'"
                  size="small"
                  class="priority-tag"
                >
                  {{ getPriorityText(notification.priority) }}
                </el-tag>
              </div>
              <div class="notification-time">
                {{ formatTime(notification.createTime) }}
              </div>
            </div>
            <div class="notification-content-text">
              {{ notification.content }}
            </div>
            <div class="notification-actions-item">
              <el-button 
                v-if="!notification.isRead"
                size="small" 
                type="primary" 
                text
                @click.stop="markAsRead(notification)"
              >
                标记已读
              </el-button>
              <el-button 
                size="small" 
                type="danger" 
                text
                @click.stop="deleteNotification(notification)"
              >
                删除
              </el-button>
            </div>
          </div>

          <!-- 空状态 -->
          <el-empty 
            v-if="notifications.length === 0 && !loading"
            description="暂无通知"
            :image-size="100"
          />
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Bell, Warning, InfoFilled, Tools, CreditCard, Switch } from '@element-plus/icons-vue'
import api from '../api'
import { useStore } from 'vuex'

const store = useStore()

// 响应式数据
const showNotificationList = ref(false)
const notifications = ref<any[]>([])
const unreadCount = ref(0)
const loading = ref(false)

// 计算属性
const currentUser = computed(() => store.state.user)
const receiverId = computed(() => currentUser.value?.id || '')
const receiverType = computed(() => currentUser.value?.role === 'ADMIN' ? 'admin' : 'student')

// 获取通知图标
const getNotificationIcon = (type: string) => {
  const iconMap: Record<string, any> = {
    system: InfoFilled,
    repair: Tools,
    payment: CreditCard,
    switch: Switch,
    allocation: InfoFilled
  }
  return iconMap[type] || InfoFilled
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

// 格式化时间
const formatTime = (time: string) => {
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  if (diff < 60000) { // 1分钟内
    return '刚刚'
  } else if (diff < 3600000) { // 1小时内
    return `${Math.floor(diff / 60000)}分钟前`
  } else if (diff < 86400000) { // 1天内
    return `${Math.floor(diff / 3600000)}小时前`
  } else {
    return date.toLocaleDateString()
  }
}

// 加载通知列表
const loadNotifications = async () => {
  if (!receiverId.value) return
  
  loading.value = true
  try {
    // 使用统一的通知接口
    const { data } = await api.getMyNotifications()
    notifications.value = data.data || []
  } catch (error) {
    console.error('加载通知失败:', error)
    ElMessage.error('加载通知失败')
  } finally {
    loading.value = false
  }
}

// 加载未读数量
const loadUnreadCount = async () => {
  if (!receiverId.value) return
  
  try {
    const { data } = await api.getUnreadCount(receiverId.value, receiverType.value)
    unreadCount.value = data.data || 0
  } catch (error) {
    console.error('加载未读数量失败:', error)
  }
}

// 标记为已读
const markAsRead = async (notification: any) => {
  if (notification.isRead) return
  
  try {
    await api.markAsRead(notification.id, receiverId.value, receiverType.value)
    notification.isRead = true
    notification.readTime = new Date().toISOString()
    unreadCount.value = Math.max(0, unreadCount.value - 1)
  } catch (error) {
    console.error('标记已读失败:', error)
    ElMessage.error('标记已读失败')
  }
}

// 全部标记为已读
const markAllAsRead = async () => {
  if (unreadCount.value === 0) return
  
  try {
    await api.markAllAsRead(receiverId.value, receiverType.value)
    notifications.value.forEach(notification => {
      notification.isRead = true
      notification.readTime = new Date().toISOString()
    })
    unreadCount.value = 0
    ElMessage.success('全部标记为已读')
  } catch (error) {
    console.error('全部标记已读失败:', error)
    ElMessage.error('全部标记已读失败')
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
    
    await api.deleteNotification(notification.id, receiverId.value, receiverType.value)
    const index = notifications.value.findIndex(n => n.id === notification.id)
    if (index > -1) {
      notifications.value.splice(index, 1)
      if (!notification.isRead) {
        unreadCount.value = Math.max(0, unreadCount.value - 1)
      }
    }
    ElMessage.success('删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除通知失败:', error)
      ElMessage.error('删除通知失败')
    }
  }
}

// 刷新通知
const refreshNotifications = async () => {
  await Promise.all([loadNotifications(), loadUnreadCount()])
}

// 关闭抽屉
const handleClose = () => {
  showNotificationList.value = false
}

// 组件挂载时加载数据
onMounted(() => {
  if (receiverId.value) {
    refreshNotifications()
  }
})

// 监听用户变化
watch(() => currentUser.value, (newUser) => {
  if (newUser?.id) {
    refreshNotifications()
  }
}, { immediate: true })
</script>

<style scoped>
.notification-center {
  position: relative;
}

.notification-badge {
  margin-right: 8px;
}

.notification-trigger {
  padding: 8px;
  border: none;
  background: none;
  cursor: pointer;
  color: #606266;
  transition: color 0.3s;
}

.notification-trigger:hover {
  color: #409eff;
}

.notification-content {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.notification-actions {
  padding: 16px;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  gap: 8px;
}

.notification-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.notification-item {
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.3s;
  border-radius: 4px;
  margin-bottom: 8px;
}

.notification-item:hover {
  background-color: #f5f7fa;
}

.notification-item.unread {
  background-color: #f0f9ff;
  border-left: 3px solid #409eff;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}

.notification-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
  color: #303133;
  flex: 1;
}

.notification-icon {
  color: #409eff;
}

.priority-tag {
  margin-left: 8px;
}

.notification-time {
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
}

.notification-content-text {
  color: #606266;
  line-height: 1.5;
  margin-bottom: 8px;
}

.notification-actions-item {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .notification-actions {
    flex-direction: column;
  }
  
  .notification-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .notification-time {
    margin-top: 4px;
  }
}
</style>


