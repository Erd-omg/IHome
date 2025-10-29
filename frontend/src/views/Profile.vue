<template>
  <div style="padding:24px; display:flex; justify-content:center;">
    <div style="width:100%; max-width:920px;">
      <div style="margin-bottom: 16px;">
        <el-button type="text" @click="$router.back()" style="padding: 0;">
          <span style="font-size: 16px;">← 返回</span>
        </el-button>
      </div>
      <h2 style="margin: 0; font-size: 20px; font-weight: 600;">个人中心</h2>
      
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
            <el-descriptions-item label="睡眠偏好">
              <el-tag v-for="tag in getSleepTags()" :key="tag" type="success" style="margin-right:8px;">
                {{ tag }}
              </el-tag>
              <span v-if="getSleepTags().length === 0">{{ questionnaireData.answers?.sleepTimePreference || '-' }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="整洁程度">
              <el-tag v-for="tag in getCleanlinessTags()" :key="tag" type="success" style="margin-right:8px;">
                {{ tag }}
              </el-tag>
              <span v-if="getCleanlinessTags().length === 0">{{ questionnaireData.answers?.cleanlinessLevel || '-' }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="噪音容忍度">
              <el-tag v-for="tag in getNoiseTags()" :key="tag" type="success" style="margin-right:8px;">
                {{ tag }}
              </el-tag>
              <span v-if="getNoiseTags().length === 0">{{ questionnaireData.answers?.noiseTolerance || '-' }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="房间用餐">
              <el-tag v-for="tag in getEatingTags()" :key="tag" type="success" style="margin-right:8px;">
                {{ tag }}
              </el-tag>
              <span v-if="getEatingTags().length === 0">{{ questionnaireData.answers?.eatingInRoom || '-' }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="集体消费">
              <el-tag v-for="tag in getConsumptionTags()" :key="tag" type="success" style="margin-right:8px;">
                {{ tag }}
              </el-tag>
              <span v-if="getConsumptionTags().length === 0">{{ questionnaireData.answers?.collectiveSpendingHabit || '-' }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="个人标签">
              <div v-if="getManualTags().length > 0">
                <el-tag v-for="tag in getManualTags()" :key="tag" style="margin-right:8px;">
                  {{ tag }}
                </el-tag>
              </div>
              <span v-else>-</span>
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
              <el-option label="早睡早起" value="早睡早起" />
              <el-option label="晚睡晚起" value="晚睡晚起" />
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
              <el-option 
                v-for="tag in availableManualTags" 
                :key="tag.value" 
                :label="tag.label" 
                :value="tag.value"
              />
            </el-select>
            <div style="margin-top:8px; font-size:12px; color:#666;">
              注意：可用的个人标签（已排除问卷将自动生成的标签）
            </div>
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
import { ref, reactive, onMounted, computed, watch } from 'vue'
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

// 初始化问卷表单的默认值
const initQuestionnaireForm = () => {
  // 如果用户已提交过问卷，使用之前的值
  if (questionnaireData.value?.answers) {
    let answers
    if (Array.isArray(questionnaireData.value.answers) && questionnaireData.value.answers.length > 0) {
      answers = questionnaireData.value.answers[0]
    } else if (typeof questionnaireData.value.answers === 'object') {
      answers = questionnaireData.value.answers
    }
    
    if (answers) {
      questionnaireForm.sleepTimePreference = answers.sleepTimePreference || ''
      questionnaireForm.cleanlinessLevel = answers.cleanlinessLevel || ''
      questionnaireForm.noiseTolerance = answers.noiseTolerance || ''
      questionnaireForm.eatingInRoom = answers.eatingInRoom || ''
      questionnaireForm.collectiveSpendingHabit = answers.collectiveSpendingHabit || ''
    }
  }
  
  // 只显示手动选择的标签，过滤掉自动生成的标签
  const autoTags = getAutoGeneratedTags()
  if (questionnaireData.value?.tags) {
    questionnaireForm.tags = questionnaireData.value.tags.filter(tag => !autoTags.includes(tag))
  } else {
    questionnaireForm.tags = []
  }
}

// 监听showQuestionnaire变化，当打开弹窗时重新初始化表单
watch(showQuestionnaire, (newVal) => {
  if (newVal) {
    // 确保问卷数据已加载
    if (questionnaireData.value) {
      initQuestionnaireForm()
    } else {
      // 如果数据还没加载，先加载
      loadQuestionnaire().then(() => {
        initQuestionnaireForm()
      })
    }
  }
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
    const data = response.data.data
    
    // 如果answers是数组，转换为对象
    if (Array.isArray(data.answers) && data.answers.length > 0) {
      data.answers = data.answers[0]
    }
    
    questionnaireData.value = data
    
    // 初始化问卷表单
    initQuestionnaireForm()
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
  
  // 前端验证必填字段
  if (!questionnaireForm.sleepTimePreference || 
      !questionnaireForm.cleanlinessLevel || 
      !questionnaireForm.noiseTolerance || 
      !questionnaireForm.eatingInRoom || 
      !questionnaireForm.collectiveSpendingHabit) {
    ElMessage.warning('请完整填写问卷')
    return
  }
  
  try {
    submittingQuestionnaire.value = true
    
    // 根据问卷答案生成自动标签列表
    const autoTags = getPendingAutoTags()
    
    // 过滤掉自动生成的标签，只保留手动选择的标签
    const manualTags = (questionnaireForm.tags || []).filter(tag => !autoTags.includes(tag))
    
    const payload = {
      studentId: user.value.id,
      answers: {
        sleepTimePreference: questionnaireForm.sleepTimePreference,
        cleanlinessLevel: questionnaireForm.cleanlinessLevel,
        noiseTolerance: questionnaireForm.noiseTolerance,
        eatingInRoom: questionnaireForm.eatingInRoom,
        collectiveSpendingHabit: questionnaireForm.collectiveSpendingHabit
      },
      tags: manualTags
    }
    
    const response = await api.submitQuestionnaire(payload)
    
    // 检查是否有标签冲突
    if (response.data.data && !response.data.data.success) {
      ElMessage.error(response.data.data.message || '标签冲突')
      return
    }
    
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

// 根据当前问卷表单获取将要自动生成的标签
const getPendingAutoTags = () => {
  const tags: string[] = []
  
  // 睡眠偏好
  if (questionnaireForm.sleepTimePreference === '早睡早起') tags.push('早睡')
  if (questionnaireForm.sleepTimePreference === '晚睡晚起') tags.push('晚睡')
  if (questionnaireForm.sleepTimePreference === '不固定') tags.push('作息不规律')
  
  // 整洁程度
  if (questionnaireForm.cleanlinessLevel === '爱整洁') tags.push('整洁')
  if (questionnaireForm.cleanlinessLevel === '随意') tags.push('随意')
  
  // 噪音容忍度
  if (questionnaireForm.noiseTolerance === '安静') tags.push('安静')
  if (questionnaireForm.noiseTolerance === '能接受一点噪音') tags.push('适度噪音')
  
  // 房间用餐
  if (questionnaireForm.eatingInRoom === '经常') tags.push('宿舍用餐')
  if (questionnaireForm.eatingInRoom === '从不') tags.push('不在宿舍用餐')
  
  // 集体消费
  if (questionnaireForm.collectiveSpendingHabit === '愿意') tags.push('集体消费')
  if (questionnaireForm.collectiveSpendingHabit === '不愿意') tags.push('独立消费')
  
  return tags
}

// 所有可用的手动标签
const allManualTags = [
  { label: '爱运动', value: '爱运动' },
  { label: '爱学习', value: '爱学习' },
  { label: '社交达人', value: '社交达人' },
  { label: '负责任', value: '负责任' },
  { label: '友善', value: '友善' },
  { label: '守时', value: '守时' },
  { label: '环保', value: '环保' },
  { label: '不吸烟', value: '不吸烟' },
  { label: '音乐爱好者', value: '音乐爱好者' },
  { label: '游戏玩家', value: '游戏玩家' },
  { label: '阅读爱好者', value: '阅读爱好者' },
  { label: '电影爱好者', value: '电影爱好者' },
  { label: '旅行爱好者', value: '旅行爱好者' },
  { label: '夜猫子', value: '夜猫子' },
  { label: '早起者', value: '早起者' },
  { label: '素食主义者', value: '素食主义者' },
  { label: '宠物爱好者', value: '宠物爱好者' }
]

// 计算可用的人工标签（排除将被自动生成的标签）
const availableManualTags = computed(() => {
  const autoTags = getPendingAutoTags()
  return allManualTags.filter(tag => !autoTags.includes(tag.value))
})

// 获取睡眠相关的标签
const getSleepTags = () => {
  if (!questionnaireData.value?.answers) return []
  const answers = questionnaireData.value.answers
  const sleepTimePreference = answers.sleepTimePreference
  
  if (sleepTimePreference === '早睡早起') return ['早睡']
  if (sleepTimePreference === '晚睡晚起') return ['晚睡']
  if (sleepTimePreference === '不固定') return ['作息不规律']
  return []
}

// 获取整洁相关的标签
const getCleanlinessTags = () => {
  if (!questionnaireData.value?.answers) return []
  const answers = questionnaireData.value.answers
  if (answers.cleanlinessLevel === '爱整洁') return ['整洁']
  if (answers.cleanlinessLevel === '随意') return ['随意']
  return []
}

// 获取噪音相关的标签
const getNoiseTags = () => {
  if (!questionnaireData.value?.answers) return []
  const answers = questionnaireData.value.answers
  if (answers.noiseTolerance === '安静') return ['安静']
  if (answers.noiseTolerance === '能接受一点噪音') return ['适度噪音']
  return []
}

// 获取用餐相关的标签
const getEatingTags = () => {
  if (!questionnaireData.value?.answers) return []
  const answers = questionnaireData.value.answers
  if (answers.eatingInRoom === '经常') return ['宿舍用餐']
  if (answers.eatingInRoom === '从不') return ['不在宿舍用餐']
  return []
}

// 获取消费相关的标签
const getConsumptionTags = () => {
  if (!questionnaireData.value?.answers) return []
  const answers = questionnaireData.value.answers
  if (answers.collectiveSpendingHabit === '愿意') return ['集体消费']
  if (answers.collectiveSpendingHabit === '不愿意') return ['独立消费']
  return []
}

// 获取所有问卷自动生成的标签
const getAutoGeneratedTags = () => {
  return [
    ...getSleepTags(),
    ...getCleanlinessTags(),
    ...getNoiseTags(),
    ...getEatingTags(),
    ...getConsumptionTags()
  ]
}

// 获取手动选择的标签
const getManualTags = () => {
  if (!questionnaireData.value?.tags) return []
  
  const autoTags = getAutoGeneratedTags()
  return questionnaireData.value.tags.filter(tag => !autoTags.includes(tag))
}

onMounted(() => {
  loadUserInfo()
  loadAllocationInfo()
  loadQuestionnaire()
})
</script>



