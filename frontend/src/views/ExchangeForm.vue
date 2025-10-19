<template>
  <div style="padding:24px;">
    <el-page-header content="宿舍调换申请" @back="$router.back()" />
    
    <!-- 当前住宿信息 -->
    <el-card style="margin-top:16px;" v-if="currentAllocation">
      <template #header>当前住宿信息</template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="学号">{{ user?.id }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ user?.name }}</el-descriptions-item>
        <el-descriptions-item label="当前床位">{{ currentAllocation.bedId }}</el-descriptions-item>
        <el-descriptions-item label="入住时间">{{ formatDate(currentAllocation.checkInDate) }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 调换申请表单 -->
    <el-card style="margin-top:16px;">
      <template #header>调换申请</template>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="申请人" prop="applicant">
              <el-input v-model="form.applicant" placeholder="请输入申请人姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系方式" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号码" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="当前宿舍" prop="currentDorm">
              <el-input v-model="form.currentDorm" placeholder="请输入当前宿舍号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="目标宿舍" prop="targetDorm">
              <el-input v-model="form.targetDorm" placeholder="请输入目标宿舍号" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="调换原因" prop="reason">
          <el-input 
            v-model="form.reason" 
            type="textarea" 
            :rows="4"
            placeholder="请详细说明调换原因，如：室友关系、学习环境、个人偏好等"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="紧急程度">
          <el-radio-group v-model="form.urgency">
            <el-radio label="一般">一般</el-radio>
            <el-radio label="紧急">紧急</el-radio>
            <el-radio label="非常紧急">非常紧急</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item>
          <el-button @click="$router.back()">取消</el-button>
          <el-button type="primary" @click="submit" :loading="submitting">确认提交</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 申请须知 -->
    <el-card style="margin-top:16px;">
      <template #header>申请须知</template>
      <el-alert
        title="重要提醒"
        type="info"
        :closable="false"
        show-icon
      >
        <ul style="margin: 0; padding-left: 20px;">
          <li>宿舍调换申请需要经过管理员审核，请耐心等待</li>
          <li>调换成功后，原床位将自动释放</li>
          <li>请确保目标宿舍有空余床位</li>
          <li>调换过程中请保持联系方式畅通</li>
          <li>如有疑问，请联系宿管员</li>
        </ul>
      </el-alert>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { api } from '../api'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'

const router = useRouter()
const store = useStore()
const formRef = ref<FormInstance>()

const user = computed(() => store.state.user)
const currentAllocation = ref<any>(null)
const submitting = ref(false)

const form = reactive({
  studentId: '',
  applicant: '',
  currentDorm: '',
  phone: '',
  targetDorm: '',
  reason: '',
  urgency: '一般'
})

// 表单验证规则
const rules: FormRules = {
  applicant: [
    { required: true, message: '请输入申请人姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入联系方式', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  currentDorm: [
    { required: true, message: '请输入当前宿舍号', trigger: 'blur' }
  ],
  targetDorm: [
    { required: true, message: '请输入目标宿舍号', trigger: 'blur' }
  ],
  reason: [
    { required: true, message: '请说明调换原因', trigger: 'blur' },
    { min: 10, message: '调换原因至少10个字符', trigger: 'blur' }
  ]
}

// 格式化日期
const formatDate = (date: string) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString('zh-CN')
}

// 加载当前住宿信息
const loadCurrentAllocation = async () => {
  if (!user.value?.id) return
  
  try {
    const response = await api.getCurrentAllocation(user.value.id)
    currentAllocation.value = response.data.data
    
    if (currentAllocation.value) {
      // 自动填充表单信息
      form.studentId = user.value.id
      form.applicant = user.value.name || ''
      form.currentDorm = currentAllocation.value.bedId || ''
    }
  } catch (error) {
    console.error('加载当前住宿信息失败:', error)
  }
}

// 提交申请
const submit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    submitting.value = true
    
    const payload = {
      ...form,
      studentId: user.value?.id || form.studentId
    }
    
    await api.createSwitch(payload)
    ElMessage.success('调换申请已提交，请等待审核')
    
    // 跳转到申请列表页面
    router.push('/exchange')
  } catch (error: any) {
    if (error !== false) { // 不是表单验证错误
      ElMessage.error(error.message || '提交失败')
    }
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadCurrentAllocation()
})
</script>


