<template>
  <div style="padding:24px;">
    <div style="margin-bottom: 16px;">
      <el-button type="text" @click="$router.back()" style="padding: 0;">
        <span style="font-size: 16px;">← 返回</span>
      </el-button>
    </div>
    <h2 style="margin: 0; font-size: 20px; font-weight: 600;">宿舍调换申请</h2>
    
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
            <el-form-item label="当前宿舍" prop="currentBedId">
              <el-input v-model="form.currentBedId" placeholder="请输入当前宿舍号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="目标宿舍" prop="targetBedId">
              <el-autocomplete
                v-model="form.targetBedId"
                :fetch-suggestions="searchBeds"
                placeholder="请输入或搜索目标宿舍号"
                style="width: 100%"
                clearable
                @select="handleBedSelect"
              >
                <template #default="{ item }">
                  <div>{{ item.dormitoryId }} - {{ item.bedNumber }} ({{ item.bedType }})</div>
                </template>
              </el-autocomplete>
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
  applicantId: '',
  applicant: '',
  currentBedId: '',
  phone: '',
  targetBedId: '',
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
  currentBedId: [
    { required: true, message: '请输入当前宿舍号', trigger: 'blur' }
  ],
  targetBedId: [
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

// 搜索床位
const searchBeds = async (queryString: string, cb: (suggestions: any[]) => void) => {
  try {
    if (!queryString || queryString.trim() === '') {
      cb([])
      return
    }
    
    // 调用后端搜索接口，传递关键字进行模糊查询
    const response = await api.searchBeds(queryString.trim())
    const beds = response.data.data || []
    
    // 过滤结果，只显示"可用"状态的床位
    const availableBeds = beds.filter((bed: any) => bed.status === '可用')
    
    const results = availableBeds.map((bed: any) => ({
      value: bed.dormitoryId + '-' + bed.bedNumber,
      id: bed.id,
      dormitoryId: bed.dormitoryId,
      bedNumber: bed.bedNumber,
      bedType: bed.bedType,
      status: bed.status
    }))
    
    cb(results)
  } catch (error) {
    console.error('搜索床位失败:', error)
    cb([])
  }
}

// 处理床位选择
const handleBedSelect = (item: any) => {
  form.targetBedId = item.id
}

// 加载当前住宿信息
const loadCurrentAllocation = async () => {
  if (!user.value?.id) return
  
  try {
    // 先加载学生信息获取姓名
    const studentResponse = await api.getStudent(user.value.id)
    const studentData = studentResponse.data.data
    
    const response = await api.getCurrentAllocation(user.value.id)
    currentAllocation.value = response.data.data
    
    // 自动填充表单信息
    form.applicantId = user.value.id
    form.applicant = studentData?.name || user.value.name || user.value.id
    form.phone = studentData?.phoneNumber || ''
    
    if (currentAllocation.value) {
      // 优先使用dormitoryId字段
      if (currentAllocation.value.dormitoryId) {
        form.currentBedId = currentAllocation.value.dormitoryId
      } else if (currentAllocation.value.bedId) {
        // 如果dormitoryId为空，从bedId提取
        const parts = currentAllocation.value.bedId.split('-')
        if (parts.length >= 2) {
          form.currentBedId = parts[0] + '-' + parts[1]
        }
      }
    }
  } catch (error) {
    console.error('加载当前住宿信息失败:', error)
    // 如果没有加载到信息，至少填充学号
    form.applicantId = user.value.id
    form.applicant = user.value.id
  }
}

// 提交申请
const submit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    submitting.value = true
    
    const payload = {
      applicantId: user.value?.id || form.applicantId,
      currentBedId: form.currentBedId,
      targetBedId: form.targetBedId,
      reason: form.reason
    }
    
    await api.createSwitch(payload)
    ElMessage.success('调换申请已提交，请等待审核')
    
    // 延迟跳转，让列表页面有时间加载
    setTimeout(() => {
      router.push('/exchange')
    }, 500)
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


