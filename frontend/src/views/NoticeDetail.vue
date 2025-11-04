<template>
  <div style="padding:24px;">
    <div style="margin-bottom: 16px;">
      <el-button type="text" @click="$router.back()" style="padding: 0;">
        <span style="font-size: 16px;">← 返回</span>
      </el-button>
    </div>
    <h2 style="margin: 0; font-size: 20px; font-weight: 600;">公告详情</h2>
    
    <el-card style="margin-top:16px;" v-loading="loading">
      <template #header>
        <div class="notice-header">
          <h2 class="notice-title">{{ notice.title }}</h2>
          <div class="notice-meta">
            <el-tag type="info" size="small">公告</el-tag>
            <span class="publish-time">{{ formatDate(notice.createTime) }}</span>
          </div>
        </div>
      </template>
      
      <div class="notice-content">
        <div v-if="notice.content" class="content-text">
          {{ notice.content }}
        </div>
        <el-empty v-else description="公告内容为空" />
      </div>
      
      <div class="notice-footer">
        <div class="actions">
          <el-button @click="$router.back()">返回列表</el-button>
          <el-button type="primary" @click="shareNotice" v-if="notice.id">分享</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { api } from '../api'
import { ElMessage } from 'element-plus'

const route = useRoute()
const notice = ref<any>({})
const loading = ref(false)

// 格式化日期
const formatDate = (date: string) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

// 加载公告详情
const loadNotice = async () => {
  const id = route.params.id
  if (!id) return
  
  loading.value = true
  try {
    const response = await api.getNotice(id as string)
    notice.value = response.data.data || {}
  } catch (error) {
    console.error('加载公告详情失败:', error)
    ElMessage.error('加载公告详情失败')
  } finally {
    loading.value = false
  }
}

// 分享公告
const shareNotice = () => {
  if (navigator.share) {
    navigator.share({
      title: notice.value.title,
      text: notice.value.content,
      url: window.location.href
    }).catch(err => {
      console.log('分享失败:', err)
      copyToClipboard()
    })
  } else {
    copyToClipboard()
  }
}

// 复制链接到剪贴板
const copyToClipboard = () => {
  navigator.clipboard.writeText(window.location.href).then(() => {
    ElMessage.success('链接已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败，请手动复制链接')
  })
}

onMounted(() => {
  loadNotice()
})
</script>

<style scoped>
.notice-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.notice-title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #333;
  line-height: 1.4;
  flex: 1;
}

.notice-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
  white-space: nowrap;
}

.publish-time {
  font-size: 14px;
  color: #666;
}

.notice-content {
  margin: 24px 0;
  min-height: 200px;
}

.content-text {
  font-size: 16px;
  line-height: 1.8;
  color: #333;
  white-space: pre-wrap;
  word-break: break-word;
}

.notice-footer {
  margin-top: 32px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

@media (max-width: 768px) {
  .notice-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .notice-meta {
    align-items: flex-start;
  }
  
  .notice-title {
    font-size: 20px;
  }
}
</style>


