<template>
  <div class="exchange-recommendations">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>调换推荐</span>
          <el-button type="primary" @click="refreshRecommendations">刷新推荐</el-button>
        </div>
      </template>

      <div v-if="recommendations.length === 0" class="no-recommendations">
        <el-empty description="暂无调换推荐" />
      </div>

      <div v-else class="recommendations-list">
        <el-row :gutter="20">
          <el-col :span="12" v-for="recommendation in recommendations" :key="recommendation.targetStudentId">
            <el-card class="recommendation-card">
              <div class="recommendation-header">
                <h3>{{ recommendation.targetStudentName }}</h3>
                <el-tag :type="getCompatibilityType(recommendation.compatibilityScore)">
                  匹配度: {{ Math.round(recommendation.compatibilityScore * 100) }}%
                </el-tag>
              </div>

              <div class="recommendation-info">
                <el-descriptions :column="1" size="small">
                  <el-descriptions-item label="学号">{{ recommendation.targetStudentId }}</el-descriptions-item>
                  <el-descriptions-item label="专业">{{ recommendation.targetStudentMajor }}</el-descriptions-item>
                  <el-descriptions-item label="学院">{{ recommendation.targetStudentCollege }}</el-descriptions-item>
                  <el-descriptions-item label="推荐理由">{{ recommendation.recommendationReason }}</el-descriptions-item>
                </el-descriptions>
              </div>

              <div class="recommendation-actions">
                <el-button 
                  type="primary" 
                  size="small" 
                  @click="viewCompatibility(recommendation.targetStudentId)"
                >
                  查看匹配详情
                </el-button>
                <el-button 
                  type="success" 
                  size="small" 
                  @click="acceptRecommendation(recommendation.targetStudentId)"
                >
                  接受推荐
                </el-button>
                <el-button 
                  type="danger" 
                  size="small" 
                  @click="rejectRecommendation(recommendation.targetStudentId)"
                >
                  拒绝推荐
                </el-button>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 匹配详情对话框 -->
      <el-dialog v-model="compatibilityDialogVisible" title="匹配详情" width="600px">
        <div v-if="compatibilityDetails">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="整体匹配度">
              <el-progress 
                :percentage="Math.round(compatibilityDetails.overallScore * 100)"
                :color="getProgressColor(compatibilityDetails.overallScore)"
              />
            </el-descriptions-item>
            <el-descriptions-item label="专业匹配度">
              <el-progress 
                :percentage="Math.round(compatibilityDetails.details.majorCompatibility * 100)"
                :color="getProgressColor(compatibilityDetails.details.majorCompatibility)"
              />
            </el-descriptions-item>
            <el-descriptions-item label="问卷匹配度">
              <el-progress 
                :percentage="Math.round(compatibilityDetails.details.questionnaireCompatibility * 100)"
                :color="getProgressColor(compatibilityDetails.details.questionnaireCompatibility)"
              />
            </el-descriptions-item>
            <el-descriptions-item label="标签匹配度">
              <el-progress 
                :percentage="Math.round(compatibilityDetails.details.tagCompatibility * 100)"
                :color="getProgressColor(compatibilityDetails.details.tagCompatibility)"
              />
            </el-descriptions-item>
          </el-descriptions>

          <div class="student-comparison">
            <el-row :gutter="20">
              <el-col :span="12">
                <h4>您的信息</h4>
                <el-descriptions :column="1" size="small">
                  <el-descriptions-item label="学号">{{ compatibilityDetails.student1.id }}</el-descriptions-item>
                  <el-descriptions-item label="姓名">{{ compatibilityDetails.student1.name }}</el-descriptions-item>
                  <el-descriptions-item label="专业">{{ compatibilityDetails.student1.major }}</el-descriptions-item>
                  <el-descriptions-item label="学院">{{ compatibilityDetails.student1.college }}</el-descriptions-item>
                </el-descriptions>
              </el-col>
              <el-col :span="12">
                <h4>对方信息</h4>
                <el-descriptions :column="1" size="small">
                  <el-descriptions-item label="学号">{{ compatibilityDetails.student2.id }}</el-descriptions-item>
                  <el-descriptions-item label="姓名">{{ compatibilityDetails.student2.name }}</el-descriptions-item>
                  <el-descriptions-item label="专业">{{ compatibilityDetails.student2.major }}</el-descriptions-item>
                  <el-descriptions-item label="学院">{{ compatibilityDetails.student2.college }}</el-descriptions-item>
                </el-descriptions>
              </el-col>
            </el-row>
          </div>
        </div>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/api'

const recommendations = ref([])
const compatibilityDialogVisible = ref(false)
const compatibilityDetails = ref(null)

onMounted(() => {
  loadRecommendations()
})

const loadRecommendations = async () => {
  try {
    const response = await http.get('/exchange/recommendations/for-student')
    recommendations.value = response.data.data
  } catch (error) {
    ElMessage.error('获取调换推荐失败')
  }
}

const getCompatibilityType = (score) => {
  if (score >= 0.8) return 'success'
  if (score >= 0.6) return 'warning'
  return 'danger'
}

const getProgressColor = (score) => {
  if (score >= 0.8) return '#67c23a'
  if (score >= 0.6) return '#e6a23c'
  return '#f56c6c'
}

const viewCompatibility = async (targetStudentId) => {
  try {
    const response = await http.get(`/exchange/recommendations/compatibility/${targetStudentId}`)
    compatibilityDetails.value = response.data.data
    compatibilityDialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取匹配详情失败')
  }
}

const acceptRecommendation = async (targetStudentId) => {
  try {
    await ElMessageBox.confirm('确定要接受这个调换推荐吗？', '确认接受', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await http.post(`/exchange/recommendations/accept/${targetStudentId}`)
    ElMessage.success('推荐已接受')
    loadRecommendations()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('接受推荐失败')
    }
  }
}

const rejectRecommendation = async (targetStudentId) => {
  try {
    await ElMessageBox.confirm('确定要拒绝这个调换推荐吗？', '确认拒绝', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await http.post(`/exchange/recommendations/reject/${targetStudentId}`)
    ElMessage.success('推荐已拒绝')
    loadRecommendations()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('拒绝推荐失败')
    }
  }
}

const refreshRecommendations = () => {
  loadRecommendations()
}
</script>

<style scoped>
.exchange-recommendations {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.no-recommendations {
  text-align: center;
  padding: 40px 0;
}

.recommendations-list {
  margin-top: 20px;
}

.recommendation-card {
  margin-bottom: 20px;
}

.recommendation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.recommendation-header h3 {
  margin: 0;
  color: #303133;
}

.recommendation-info {
  margin-bottom: 15px;
}

.recommendation-actions {
  text-align: center;
}

.recommendation-actions .el-button {
  margin: 0 5px;
}

.student-comparison {
  margin-top: 20px;
}

.student-comparison h4 {
  margin-bottom: 15px;
  color: #303133;
}
</style>
