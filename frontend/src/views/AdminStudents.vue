<template>
  <div style="padding:16px;">
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span>学生管理</span>
          <div>
            <el-button type="primary" size="small" @click="showCreateDialog = true">添加学生</el-button>
            <el-button size="small" @click="onExport">导出CSV</el-button>
          </div>
        </div>
      </template>

      <!-- 搜索筛选 -->
      <el-form :inline="true" :model="search" style="margin-bottom:12px;">
        <el-form-item label="学号">
          <el-input v-model="search.id" clearable placeholder="请输入学号" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="search.name" clearable placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="学院">
          <el-input v-model="search.college" clearable placeholder="请输入学院" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch">搜索</el-button>
          <el-button @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="rows" v-loading="loading" @sort-change="onSort">
        <el-table-column prop="id" label="学号" sortable="custom" />
        <el-table-column prop="name" label="姓名" sortable="custom" />
        <el-table-column prop="college" label="学院" />
        <el-table-column prop="major" label="专业" />
        <el-table-column prop="gender" label="性别" />
        <el-table-column prop="phoneNumber" label="手机号" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="editStudent(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteStudent(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="display:flex; justify-content:center; margin-top:12px;">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="load"
          @size-change="onSize"
        />
      </div>
    </el-card>

    <!-- 添加/编辑学生对话框 -->
    <el-dialog 
      v-model="showCreateDialog" 
      :title="editingStudent ? '编辑学生' : '添加学生'"
      width="600px"
    >
      <el-form :model="studentForm" :rules="studentRules" ref="studentFormRef" label-width="80px">
        <el-form-item label="学号" prop="id">
          <el-input v-model="studentForm.id" :disabled="!!editingStudent" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="studentForm.name" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="studentForm.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-select v-model="studentForm.gender" style="width: 100%">
            <el-option label="男" value="男" />
            <el-option label="女" value="女" />
          </el-select>
        </el-form-item>
        <el-form-item label="学院" prop="college">
          <el-input v-model="studentForm.college" />
        </el-form-item>
        <el-form-item label="专业" prop="major">
          <el-input v-model="studentForm.major" />
        </el-form-item>
        <el-form-item label="手机号" prop="phoneNumber">
          <el-input v-model="studentForm.phoneNumber" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="studentForm.email" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="saveStudent" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'
import { exportToCSV } from '../utils/csv'

const rows = ref<any[]>([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const sortBy = ref('')
const sortOrder = ref('')
const showCreateDialog = ref(false)
const saving = ref(false)
const editingStudent = ref<any>(null)
const studentFormRef = ref()

const search = reactive({ id: '', name: '', college: '' })

const studentForm = reactive({
  id: '',
  name: '',
  password: '',
  gender: '',
  college: '',
  major: '',
  phoneNumber: '',
  email: ''
})

const studentRules = {
  id: [{ required: true, message: '请输入学号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  college: [{ required: true, message: '请输入学院', trigger: 'blur' }],
  major: [{ required: true, message: '请输入专业', trigger: 'blur' }]
}

async function load() {
  loading.value = true
  try {
    const params: any = { page: page.value, size: size.value }
    if (sortBy.value) params.sort = `${sortBy.value},${sortOrder.value}`
    if (search.id) params.id = search.id
    if (search.name) params.name = search.name
    if (search.college) params.college = search.college
    
    const { data } = await api.adminStudents(params)
    const d = data.data
    rows.value = d.content || []
    total.value = d.totalElements || 0
  } finally {
    loading.value = false
  }
}

function onSort({ prop, order }: any) {
  sortBy.value = prop
  sortOrder.value = order === 'ascending' ? 'asc' : 'desc'
  load()
}

function onSize(s: number) { size.value = s; page.value = 1; load() }

function onSearch() { page.value = 1; load() }

function onReset() { 
  search.id = ''
  search.name = ''
  search.college = ''
  page.value = 1
  load() 
}

function onExport() {
  exportToCSV('students.csv', rows.value, { 
    id: '学号', 
    name: '姓名', 
    college: '学院', 
    major: '专业',
    gender: '性别',
    phoneNumber: '手机号',
    email: '邮箱'
  })
}

function editStudent(student: any) {
  editingStudent.value = student
  Object.assign(studentForm, student)
  studentForm.password = '' // 编辑时不显示密码
  showCreateDialog.value = true
}

async function deleteStudent(student: any) {
  try {
    await ElMessageBox.confirm(`确定要删除学生 ${student.name} 吗？`, '确认删除', {
      type: 'warning'
    })
    
    await api.deleteStudent(student.id)
    ElMessage.success('删除成功')
    load()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

async function saveStudent() {
  try {
    await studentFormRef.value.validate()
    saving.value = true
    
    if (editingStudent.value) {
      // 编辑学生
      const updateData = { ...studentForm }
      if (!updateData.password) {
        delete updateData.password // 如果密码为空，则不更新密码
      }
      await api.updateStudentAdmin(editingStudent.value.id, updateData)
      ElMessage.success('学生信息更新成功')
    } else {
      // 创建学生
      await api.createStudent(studentForm)
      ElMessage.success('学生创建成功')
    }
    
    showCreateDialog.value = false
    resetForm()
    load()
  } catch (error: any) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    saving.value = false
  }
}

function resetForm() {
  editingStudent.value = null
  Object.assign(studentForm, {
    id: '',
    name: '',
    password: '',
    gender: '',
    college: '',
    major: '',
    phoneNumber: '',
    email: ''
  })
}

onMounted(load)
</script>


