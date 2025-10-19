<template>
  <div style="padding:24px;">
    <el-page-header content="缴费异常处理" />
    <el-card style="margin-top:12px;">
      <el-form :inline="true" :model="q">
        <el-form-item label="宿舍号"><el-input v-model="q.dormNo" /></el-form-item>
        <el-form-item label="成员人"><el-input v-model="q.member" /></el-form-item>
        <el-form-item label="创建时间"><el-date-picker v-model="q.createRange" type="daterange" /></el-form-item>
        <el-form-item label="异常类型"><el-select v-model="q.type"><el-option label="金额未到账" value="not_arrived" /></el-select></el-form-item>
        <el-form-item>
          <el-button type="primary">搜索</el-button>
          <el-button>重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    <el-table :data="rows" style="width:100%; margin-top:12px;">
      <el-table-column prop="status" label="状态" />
      <el-table-column prop="applicant" label="申请人" />
      <el-table-column prop="major" label="年级专业" />
      <el-table-column prop="dorm" label="宿舍" />
      <el-table-column prop="theory" label="理论金额" />
      <el-table-column prop="actual" label="实际金额" />
      <el-table-column prop="abnormal" label="异常类型" />
      <el-table-column label="操作" width="160">
        <template #default>
          <el-button size="small">编辑</el-button>
          <el-button size="small" type="danger">驳回</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div style="display:flex; justify-content:center; margin-top:12px;">
      <el-pagination layout="prev, pager, next" :total="100" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
const q = reactive({ dormNo:'', member:'', createRange:[] as any, type:'' })
const rows: any[] = []
</script>


