<template>
  <el-container style="min-height:100vh;">
    <Navbar />
    <el-main style="background:#fafafa;">
      <router-view />
    </el-main>
    <el-footer height="48px" style="text-align:center;color:#999;">
      IHome 宿舍管理系统 ©2025
    </el-footer>
  </el-container>
  </template>

<script setup lang="ts">
import Navbar from '../components/Navbar.vue'
</script>


