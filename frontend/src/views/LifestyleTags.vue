<template>
  <div class="lifestyle-tags">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>生活习惯标签</span>
          <el-button type="primary" @click="saveTags" :loading="saving">保存标签</el-button>
        </div>
      </template>

      <!-- 我的标签 -->
      <div class="my-tags-section">
        <h3>我的标签</h3>
        <div class="my-tags">
          <el-tag
            v-for="tag in myTags"
            :key="tag.id"
            closable
            @close="removeTag(tag.tagName)"
            class="tag-item"
          >
            {{ tag.tagName }}
          </el-tag>
          <el-tag v-if="myTags.length === 0" type="info">暂无标签</el-tag>
        </div>
      </div>

      <!-- 标签推荐 -->
      <div v-if="recommendations.length > 0" class="recommendations-section">
        <h3>推荐标签</h3>
        <div class="recommendations">
          <el-tag
            v-for="tag in recommendations"
            :key="tag"
            @click="addTag(tag)"
            class="tag-item recommendation-tag"
          >
            + {{ tag }}
          </el-tag>
        </div>
      </div>

      <!-- 所有可用标签 -->
      <div class="available-tags-section">
        <h3>所有可用标签</h3>
        <el-tabs v-model="activeCategory" @tab-click="handleCategoryChange">
          <el-tab-pane 
            v-for="category in categories" 
            :key="category.name" 
            :label="category.name" 
            :name="category.name"
          >
            <div class="category-tags">
              <el-tag
                v-for="tag in category.tags"
                :key="tag.name"
                :type="isTagSelected(tag.name) ? 'success' : 'info'"
                @click="toggleTag(tag.name)"
                class="tag-item available-tag"
              >
                {{ tag.name }}
              </el-tag>
            </div>
            <div class="tag-descriptions">
              <el-collapse>
                <el-collapse-item title="标签说明" name="descriptions">
                  <div class="description-list">
                    <div v-for="tag in category.tags" :key="tag.name" class="description-item">
                      <strong>{{ tag.name }}:</strong> {{ tag.description }}
                    </div>
                  </div>
                </el-collapse-item>
              </el-collapse>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/api'

const myTags = ref([])
const availableTags = ref([])
const recommendations = ref([])
const activeCategory = ref('作息习惯')
const saving = ref(false)

const categories = computed(() => {
  const categoryMap = new Map()
  
  availableTags.value.forEach(tag => {
    const category = tag.category || '其他'
    if (!categoryMap.has(category)) {
      categoryMap.set(category, [])
    }
    categoryMap.get(category).push(tag)
  })
  
  return Array.from(categoryMap.entries()).map(([name, tags]) => ({
    name,
    tags
  }))
})

onMounted(() => {
  loadMyTags()
  loadAvailableTags()
  loadRecommendations()
})

const loadMyTags = async () => {
  try {
    const response = await http.get('/lifestyle-tags/my-tags')
    myTags.value = response.data.data
  } catch (error) {
    ElMessage.error('获取我的标签失败')
  }
}

const loadAvailableTags = async () => {
  try {
    const response = await http.get('/lifestyle-tags/available')
    availableTags.value = response.data.data
  } catch (error) {
    ElMessage.error('获取可用标签失败')
  }
}

const loadRecommendations = async () => {
  try {
    const response = await http.get('/lifestyle-tags/recommendations')
    recommendations.value = response.data.data
  } catch (error) {
    // 推荐失败不影响主要功能
  }
}

const isTagSelected = (tagName) => {
  return myTags.value.some(tag => tag.tagName === tagName)
}

const addTag = async (tagName) => {
  if (isTagSelected(tagName)) {
    ElMessage.warning('标签已存在')
    return
  }

  try {
    await http.post('/lifestyle-tags/add', null, {
      params: { tagName }
    })
    
    // 添加到本地列表
    myTags.value.push({
      id: Date.now(),
      tagName: tagName,
      studentId: 'current',
      createdAt: new Date()
    })
    
    ElMessage.success('标签添加成功')
    loadRecommendations() // 重新加载推荐
  } catch (error) {
    ElMessage.error('标签添加失败')
  }
}

const removeTag = async (tagName) => {
  try {
    await http.delete('/lifestyle-tags/remove', {
      params: { tagName }
    })
    
    // 从本地列表移除
    myTags.value = myTags.value.filter(tag => tag.tagName !== tagName)
    
    ElMessage.success('标签删除成功')
    loadRecommendations() // 重新加载推荐
  } catch (error) {
    ElMessage.error('标签删除失败')
  }
}

const toggleTag = (tagName) => {
  if (isTagSelected(tagName)) {
    removeTag(tagName)
  } else {
    addTag(tagName)
  }
}

const saveTags = async () => {
  saving.value = true
  try {
    const tagNames = myTags.value.map(tag => tag.tagName)
    await http.post('/lifestyle-tags/set', tagNames)
    ElMessage.success('标签保存成功')
  } catch (error) {
    ElMessage.error('标签保存失败')
  } finally {
    saving.value = false
  }
}

const handleCategoryChange = (tab) => {
  activeCategory.value = tab.name
}
</script>

<style scoped>
.lifestyle-tags {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.my-tags-section,
.recommendations-section,
.available-tags-section {
  margin-bottom: 30px;
}

.my-tags-section h3,
.recommendations-section h3,
.available-tags-section h3 {
  margin-bottom: 15px;
  color: #303133;
}

.my-tags,
.recommendations,
.category-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.tag-item {
  cursor: pointer;
  transition: all 0.3s;
}

.tag-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.recommendation-tag {
  background-color: #f0f9ff;
  border-color: #409eff;
  color: #409eff;
}

.recommendation-tag:hover {
  background-color: #409eff;
  color: white;
}

.available-tag {
  margin-bottom: 10px;
}

.tag-descriptions {
  margin-top: 20px;
}

.description-list {
  max-height: 200px;
  overflow-y: auto;
}

.description-item {
  margin-bottom: 10px;
  padding: 8px;
  background-color: #f5f5f5;
  border-radius: 4px;
}

.description-item strong {
  color: #409eff;
}
</style>
