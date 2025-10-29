<template>
  <div class="student-import">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>学生账号批量导入</span>
          <el-button type="primary" @click="downloadTemplate">下载模板</el-button>
        </div>
      </template>

      <el-upload
        ref="uploadRef"
        :auto-upload="false"
        :on-change="handleFileChange"
        :before-upload="beforeUpload"
        accept=".xlsx,.xls"
        drag
        class="upload-demo"
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
          将文件拖到此处，或<em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            只能上传 xlsx/xls 文件，且不超过 10MB
          </div>
        </template>
      </el-upload>

      <div v-if="file" class="file-info">
        <el-alert
          :title="`已选择文件: ${file.name}`"
          type="info"
          :closable="false"
        />
      </div>

      <div class="action-buttons">
        <el-button @click="validateData" :loading="validating">验证数据</el-button>
        <el-button type="primary" @click="importData" :loading="importing" :disabled="!file">
          开始导入
        </el-button>
      </div>

      <!-- 验证结果 -->
      <div v-if="validationResult" class="validation-result">
        <el-card>
          <template #header>
            <span>验证结果</span>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="总行数">{{ validationResult.totalRows }}</el-descriptions-item>
            <el-descriptions-item label="错误数">{{ validationResult.errorCount }}</el-descriptions-item>
            <el-descriptions-item label="警告数">{{ validationResult.warningCount }}</el-descriptions-item>
            <el-descriptions-item label="是否有效">
              <el-tag :type="validationResult.isValid ? 'success' : 'danger'">
                {{ validationResult.isValid ? '有效' : '无效' }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>

          <div v-if="validationResult.errors.length > 0" class="error-list">
            <h4>错误信息:</h4>
            <el-alert
              v-for="(error, index) in validationResult.errors"
              :key="index"
              :title="error"
              type="error"
              :closable="false"
              class="error-item"
            />
          </div>

          <div v-if="validationResult.warnings.length > 0" class="warning-list">
            <h4>警告信息:</h4>
            <el-alert
              v-for="(warning, index) in validationResult.warnings"
              :key="index"
              :title="warning"
              type="warning"
              :closable="false"
              class="warning-item"
            />
          </div>
        </el-card>
      </div>

      <!-- 导入结果 -->
      <div v-if="importResult" class="import-result">
        <el-card>
          <template #header>
            <span>导入结果</span>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="总行数">{{ importResult.totalRows }}</el-descriptions-item>
            <el-descriptions-item label="成功数">{{ importResult.successCount }}</el-descriptions-item>
            <el-descriptions-item label="错误数">{{ importResult.errorCount }}</el-descriptions-item>
            <el-descriptions-item label="警告数">{{ importResult.warningCount }}</el-descriptions-item>
          </el-descriptions>

          <div v-if="importResult.errors.length > 0" class="error-list">
            <h4>导入错误:</h4>
            <el-alert
              v-for="(error, index) in importResult.errors"
              :key="index"
              :title="error"
              type="error"
              :closable="false"
              class="error-item"
            />
          </div>

          <div v-if="importResult.warnings.length > 0" class="warning-list">
            <h4>导入警告:</h4>
            <el-alert
              v-for="(warning, index) in importResult.warnings"
              :key="index"
              :title="warning"
              type="warning"
              :closable="false"
              class="warning-item"
            />
          </div>
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import api from '@/api'

const uploadRef = ref()
const file = ref(null)
const validating = ref(false)
const importing = ref(false)
const validationResult = ref(null)
const importResult = ref(null)

const handleFileChange = (file, fileList) => {
  file.value = file.raw
  validationResult.value = null
  importResult.value = null
}

const beforeUpload = (file) => {
  const isExcel = file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' ||
                  file.type === 'application/vnd.ms-excel'
  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isExcel) {
    ElMessage.error('只能上传 Excel 文件!')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过 10MB!')
    return false
  }
  return false // 阻止自动上传
}

const downloadTemplate = async () => {
  try {
    const response = await http.get('/admin/students/import/template', {
      responseType: 'blob'
    })
    
    const blob = new Blob([response.data])
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '学生信息导入模板.xlsx'
    link.click()
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('模板下载成功')
  } catch (error) {
    ElMessage.error('模板下载失败')
  }
}

const validateData = async () => {
  if (!file.value) {
    ElMessage.warning('请先选择文件')
    return
  }

  validating.value = true
  try {
    const formData = new FormData()
    formData.append('file', file.value)
    
    const response = await http.post('/admin/students/import/validate', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    validationResult.value = response.data.data
    ElMessage.success('数据验证完成')
  } catch (error) {
    ElMessage.error('数据验证失败')
  } finally {
    validating.value = false
  }
}

const importData = async () => {
  if (!file.value) {
    ElMessage.warning('请先选择文件')
    return
  }

  if (validationResult.value && !validationResult.value.isValid) {
    ElMessage.warning('数据验证未通过，请修复错误后重试')
    return
  }

  try {
    await ElMessageBox.confirm('确定要导入这些学生数据吗？', '确认导入', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }

  importing.value = true
  try {
    const formData = new FormData()
    formData.append('file', file.value)
    
    const response = await http.post('/admin/students/import', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    importResult.value = response.data.data
    ElMessage.success('学生数据导入完成')
  } catch (error) {
    ElMessage.error('学生数据导入失败')
  } finally {
    importing.value = false
  }
}
</script>

<style scoped>
.student-import {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.upload-demo {
  margin: 20px 0;
}

.file-info {
  margin: 20px 0;
}

.action-buttons {
  margin: 20px 0;
  text-align: center;
}

.action-buttons .el-button {
  margin: 0 10px;
}

.validation-result,
.import-result {
  margin-top: 20px;
}

.error-list,
.warning-list {
  margin-top: 20px;
}

.error-item,
.warning-item {
  margin-bottom: 10px;
}
</style>
