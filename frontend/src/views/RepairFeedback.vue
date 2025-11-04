<template>
  <div class="repair-feedback">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>维修反馈</span>
          <el-button type="primary" @click="showFeedbackDialog">提交反馈</el-button>
        </div>
      </template>

      <!-- 我的反馈列表 -->
      <div class="feedback-list">
        <el-table :data="myFeedbacks" style="width: 100%">
          <el-table-column prop="repairOrderId" label="维修单号" width="120" />
          <el-table-column prop="rating" label="评分" width="100">
            <template #default="scope">
              <el-rate 
                v-model="scope.row.rating" 
                disabled 
                show-score 
                text-color="#ff9900"
              />
            </template>
          </el-table-column>
          <el-table-column prop="content" label="反馈内容" min-width="200" />
          <el-table-column prop="reply" label="管理员回复" min-width="200">
            <template #default="scope">
              <span v-if="scope.row.reply">{{ scope.row.reply }}</span>
              <el-tag v-else type="info">暂无回复</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="提交时间" width="180">
            <template #default="scope">
              {{ formatDate(scope.row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="scope">
              <el-button 
                type="primary" 
                size="small" 
                @click="viewFeedbackDetail(scope.row)"
              >
                查看详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 反馈提交对话框 -->
      <el-dialog v-model="feedbackDialogVisible" title="提交维修反馈" width="600px">
        <el-form :model="feedbackForm" :rules="feedbackRules" ref="feedbackFormRef" label-width="100px">
          <el-form-item label="维修单号" prop="repairOrderId">
            <el-select v-model="feedbackForm.repairOrderId" placeholder="请选择维修单号" style="width: 100%">
              <el-option
                v-for="order in completedRepairOrders"
                :key="order.id"
                :label="`${order.id} - ${order.description}`"
                :value="order.id"
              />
            </el-select>
          </el-form-item>
          
          <el-form-item label="评分" prop="rating">
            <el-rate 
              v-model="feedbackForm.rating" 
              show-text 
              :texts="['很差', '较差', '一般', '满意', '非常满意']"
            />
          </el-form-item>
          
          <el-form-item label="反馈内容" prop="content">
            <el-input
              v-model="feedbackForm.content"
              type="textarea"
              :rows="4"
              placeholder="请详细描述您的反馈意见..."
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
        </el-form>
        
        <template #footer>
          <el-button @click="feedbackDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitFeedback" :loading="submitting">提交反馈</el-button>
        </template>
      </el-dialog>

      <!-- 反馈详情对话框 -->
      <el-dialog v-model="detailDialogVisible" title="反馈详情" width="600px">
        <div v-if="selectedFeedback">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="维修单号">{{ selectedFeedback.repairOrderId }}</el-descriptions-item>
            <el-descriptions-item label="评分">
              <el-rate 
                v-model="selectedFeedback.rating" 
                disabled 
                show-score 
                text-color="#ff9900"
              />
            </el-descriptions-item>
            <el-descriptions-item label="提交时间" :span="2">
              {{ formatDate(selectedFeedback.createdAt) }}
            </el-descriptions-item>
            <el-descriptions-item label="反馈内容" :span="2">
              {{ selectedFeedback.content }}
            </el-descriptions-item>
            <el-descriptions-item label="管理员回复" :span="2">
              <div v-if="selectedFeedback.reply">
                {{ selectedFeedback.reply }}
              </div>
              <el-tag v-else type="info">暂无回复</el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/api'

const myFeedbacks = ref([])
const completedRepairOrders = ref([])
const feedbackDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const selectedFeedback = ref(null)
const submitting = ref(false)

const feedbackForm = ref({
  repairOrderId: null,
  rating: 5,
  content: ''
})

const feedbackFormRef = ref()

const feedbackRules = {
  repairOrderId: [
    { required: true, message: '请选择维修单号', trigger: 'change' }
  ],
  rating: [
    { required: true, message: '请选择评分', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入反馈内容', trigger: 'blur' },
    { min: 10, message: '反馈内容至少10个字符', trigger: 'blur' }
  ]
}

onMounted(() => {
  loadMyFeedbacks()
  loadCompletedRepairOrders()
})

const loadMyFeedbacks = async () => {
  try {
    const response = await http.get('/repair-feedback/my-feedback')
    myFeedbacks.value = response.data.data
  } catch (error) {
    ElMessage.error('获取反馈列表失败')
  }
}

const loadCompletedRepairOrders = async () => {
  try {
    const response = await http.get('/repairs/my-orders')
    completedRepairOrders.value = response.data.data.filter(order => order.status === '已完成')
  } catch (error) {
    ElMessage.error('获取维修订单失败')
  }
}

const showFeedbackDialog = () => {
  if (completedRepairOrders.value.length === 0) {
    ElMessage.warning('暂无已完成的维修订单')
    return
  }
  feedbackDialogVisible.value = true
}

const submitFeedback = async () => {
  if (!feedbackFormRef.value) return
  
  try {
    await feedbackFormRef.value.validate()
  } catch {
    return
  }

  submitting.value = true
  try {
    await http.post('/repair-feedback/submit', feedbackForm.value)
    ElMessage.success('反馈提交成功')
    feedbackDialogVisible.value = false
    feedbackForm.value = {
      repairOrderId: null,
      rating: 5,
      content: ''
    }
    loadMyFeedbacks()
  } catch (error) {
    ElMessage.error('反馈提交失败')
  } finally {
    submitting.value = false
  }
}

const viewFeedbackDetail = (feedback) => {
  selectedFeedback.value = feedback
  detailDialogVisible.value = true
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  return new Date(dateString).toLocaleString('zh-CN')
}
</script>

<style scoped>
.repair-feedback {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.feedback-list {
  margin-top: 20px;
}
</style>
