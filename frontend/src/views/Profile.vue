<template>
  <div style="padding:24px; display:flex; justify-content:center;">
    <div style="width:100%; max-width:920px;">
      <el-page-header content="个人中心" @back="$router.back()" />
      
      <!-- 个人信息 -->
      <el-card style="margin-top:16px;">
        <template #header>
          <div style="display:flex; justify-content:space-between; align-items:center;">
            <span>个人信息</span>
            <el-button type="primary" @click="save" :loading="saving">保存修改</el-button>
          </div>
        </template>
        <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="学号" prop="id">
                <el-input v-model="form.id" disabled />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="姓名" prop="name">
                <el-input v-model="form.name" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="性别" prop="gender">
                <el-select v-model="form.gender" style="width:100%">
                  <el-option label="男" value="男" />
                  <el-option label="女" value="女" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="电话" prop="phoneNumber">
                <el-input v-model="form.phoneNumber" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="邮箱" prop="email">
                <el-input v-model="form.email" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="学院" prop="college">
                <el-input v-model="form.college" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="专业" prop="major">
                <el-input v-model="form.major" />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </el-card>

      <!-- 住宿信息 -->
      <el-card style="margin-top:16px;" v-if="allocationInfo">
        <template #header>住宿信息</template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="床位ID">{{ allocationInfo.bedId }}</el-descriptions-item>
          <el-descriptions-item label="入住时间">{{ formatDate(allocationInfo.checkInDate) }}</el-descriptions-item>
          <el-descriptions-item label="住宿状态">
            <el-tag :type="allocationInfo.status === '在住' ? 'success' : 'info'">
              {{ allocationInfo.status }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="退宿时间">
            {{ allocationInfo.checkOutDate ? formatDate(allocationInfo.checkOutDate) : '-' }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 问卷信息 -->
      <el-card style="margin-top:16px;">
        <template #header>
          <div style="display:flex; justify-content:space-between; align-items:center;">
            <span>室友匹配问卷</span>
            <el-button type="primary" @click="showQuestionnaire = true" v-if="!questionnaireData">
              填写问卷
            </el-button>
            <el-button type="warning" @click="showQuestionnaire = true" v-else>
              修改问卷
            </el-button>
          </div>
        </template>
        <div v-if="questionnaireData">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="睡眠偏好">{{ questionnaireData.answers?.sleepTimePreference || '-' }}</el-descriptions-item>
            <el-descriptions-item label="整洁程度">{{ questionnaireData.answers?.cleanlinessLevel || '-' }}</el-descriptions-item>
            <el-descriptions-item label="噪音容忍度">{{ questionnaireData.answers?.noiseTolerance || '-' }}</el-descriptions-item>
            <el-descriptions-item label="房间用餐">{{ questionnaireData.answers?.eatingInRoom || '-' }}</el-descriptions-item>
            <el-descriptions-item label="集体消费">{{ questionnaireData.answers?.collectiveSpendingHabit || '-' }}</el-descriptions-item>
            <el-descriptions-item label="个人标签">
              <el-tag v-for="tag in questionnaireData.tags" :key="tag" style="margin-right:8px;">
                {{ tag }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </div>
        <el-empty v-else description="尚未填写室友匹配问卷" />
      </el-card>

      <!-- 问卷弹窗 -->
      <el-dialog v-model="showQuestionnaire" title="室友匹配问卷" width="600px">
        <el-form :model="questionnaireForm" label-width="120px">
          <el-form-item label="睡眠时间偏好">
            <el-select v-model="questionnaireForm.sleepTimePreference" style="width:100%">
              <el-option label="早睡早起" value="早睡" />
              <el-option label="晚睡晚起" value="晚睡" />
              <el-option label="不固定" value="不固定" />
            </el-select>
          </el-form-item>
          <el-form-item label="整洁程度">
            <el-select v-model="questionnaireForm.cleanlinessLevel" style="width:100%">
              <el-option label="爱整洁" value="爱整洁" />
              <el-option label="一般" value="一般" />
              <el-option label="随意" value="随意" />
            </el-select>
          </el-form-item>
          <el-form-item label="噪音容忍度">
            <el-select v-model="questionnaireForm.noiseTolerance" style="width:100%">
              <el-option label="安静" value="安静" />
              <el-option label="能接受一点噪音" value="能接受一点噪音" />
              <el-option label="无所谓" value="无所谓" />
            </el-select>
          </el-form-item>
          <el-form-item label="房间用餐">
            <el-select v-model="questionnaireForm.eatingInRoom" style="width:100%">
              <el-option label="经常" value="经常" />
              <el-option label="偶尔" value="偶尔" />
              <el-option label="从不" value="从不" />
            </el-select>
          </el-form-item>
          <el-form-item label="集体消费意愿">
            <el-select v-model="questionnaireForm.collectiveSpendingHabit" style="width:100%">
              <el-option label="愿意" value="愿意" />
              <el-option label="一般" value="一般" />
              <el-option label="不愿意" value="不愿意" />
            </el-select>
          </el-form-item>
          <el-form-item label="个人标签">
            <el-select v-model="questionnaireForm.tags" multiple style="width:100%">
              <el-option label="安静" value="安静" />
              <el-option label="整洁" value="整洁" />
              <el-option label="不吸烟" value="不吸烟" />
              <el-option label="爱运动" value="爱运动" />
              <el-option label="爱学习" value="爱学习" />
              <el-option label="社交达人" value="社交达人" />
            </el-select>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="showQuestionnaire = false">取消</el-button>
          <el-button type="primary" @click="submitQuestionnaire" :loading="submittingQuestionnaire">
            提交
          </el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useStore } from 'vuex'
import { api } from '../api'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'

const store = useStore()
const user = computed(() => store.state.user)

const formRef = ref<FormInstance>()
const saving = ref(false)
const showQuestionnaire = ref(false)
const submittingQuestionnaire = ref(false)
const allocationInfo = ref<any>(null)
const questionnaireData = ref<any>(null)

const form = reactive({
  id: '',
  name: '',
  gender: '',
  phoneNumber: '',
  email: '',
  college: '',
  major: ''
})

const questionnaireForm = reactive({
  sleepTimePreference: '',
  cleanlinessLevel: '',
  noiseTolerance: '',
  eatingInRoom: '',
  collectiveSpendingHabit: '',
  tags: [] as string[]
})

// 表单验证规则
const rules: FormRules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  gender: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  phoneNumber: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  college: [
    { required: true, message: '请输入学院', trigger: 'blur' }
  ],
  major: [
    { required: true, message: '请输入专业', trigger: 'blur' }
  ]
}

// 格式化日期
const formatDate = (date: string) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString('zh-CN')
}

// 加载用户信息
const loadUserInfo = async () => {
  if (!user.value?.id) return
  
  try {
    const response = await api.getStudent(user.value.id)
    const data = response.data.data
    Object.assign(form, data)
  } catch (error) {
    console.error('加载用户信息失败:', error)
  }
}

// 加载住宿信息
const loadAllocationInfo = async () => {
  if (!user.value?.id) return
  
  try {
    const response = await api.getCurrentAllocation(user.value.id)
    allocationInfo.value = response.data.data
  } catch (error) {
    console.error('加载住宿信息失败:', error)
  }
}

// 加载问卷信息
const loadQuestionnaire = async () => {
  if (!user.value?.id) return
  
  try {
    const response = await api.getQuestionnaire(user.value.id)
    questionnaireData.value = response.data.data
    
    if (questionnaireData.value?.answers) {
      Object.assign(questionnaireForm, questionnaireData.value.answers)
    }
    if (questionnaireData.value?.tags) {
      questionnaireForm.tags = questionnaireData.value.tags
    }
  } catch (error) {
    console.error('加载问卷信息失败:', error)
  }
}

// 保存个人信息
const save = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    saving.value = true
    
    await api.updateStudent(form.id, form)
    ElMessage.success('个人信息更新成功')
    
    // 更新store中的用户信息
    store.commit('setUser', { ...user.value, ...form })
  } catch (error: any) {
    if (error !== false) { // 不是表单验证错误
      ElMessage.error(error.message || '更新失败')
    }
  } finally {
    saving.value = false
  }
}

// 提交问卷
const submitQuestionnaire = async () => {
  if (!user.value?.id) return
  
  try {
    submittingQuestionnaire.value = true
    
    const payload = {
      studentId: user.value.id,
      answers: {
        sleepTimePreference: questionnaireForm.sleepTimePreference,
        cleanlinessLevel: questionnaireForm.cleanlinessLevel,
        noiseTolerance: questionnaireForm.noiseTolerance,
        eatingInRoom: questionnaireForm.eatingInRoom,
        collectiveSpendingHabit: questionnaireForm.collectiveSpendingHabit
      },
      tags: questionnaireForm.tags
    }
    
    await api.submitQuestionnaire(payload)
    ElMessage.success('问卷提交成功')
    showQuestionnaire.value = false
    
    // 重新加载问卷信息
    await loadQuestionnaire()
  } catch (error: any) {
    ElMessage.error(error.message || '问卷提交失败')
  } finally {
    submittingQuestionnaire.value = false
  }
}

onMounted(() => {
  loadUserInfo()
  loadAllocationInfo()
  loadQuestionnaire()
})
</script>


