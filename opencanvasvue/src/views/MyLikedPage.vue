<template>
  <div>
    <h1>좋아요한 작품</h1>

    <div
      v-for="cover in likedCovers"
      :key="cover.coverId"
      class="card"
    >
      <img
        v-if="cover.coverImageUrl"
        :src="cover.coverImageUrl"
        class="cover-image"
        @click="openContent(cover.coverId)"
      />

      <h3>{{ cover.title }}</h3>
      <p>방 타입: {{ cover.roomType }}</p>
      <p>조회수: {{ cover.view }}</p>
      <p>좋아요: {{ cover.likeCount }}</p>
      <p>생성일: {{ formatDate(cover.coverTime) }}</p>
    </div>

    <p v-if="likedCovers.length === 0">좋아요한 작품이 없습니다.</p>
    <p v-if="errorMessage">{{ errorMessage }}</p>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const likedCovers = ref([])
const errorMessage = ref('')

onMounted(() => {
  getLikedCovers()
})

async function getLikedCovers() {
  try {
    const token = localStorage.getItem('accessToken')

    const response = await fetch('http://localhost:8080/api/users/likes', {
      headers: {
        Authorization: `Bearer ${token}`
      }
    })

    if (!response.ok) {
      throw new Error('좋아요한 작품 조회 실패')
    }

    likedCovers.value = await response.json()
  } catch (error) {
    errorMessage.value = error.message
  }
}

function formatDate(dateText) {
  if (!dateText) return ''
  return new Date(dateText).toLocaleString()
}

function openContent(coverId) {
  console.log('나중에 작품 보기:', coverId)
}
</script>

<style scoped>
.card {
  border: 1px solid #ddd;
  padding: 16px;
  margin-bottom: 12px;
  border-radius: 8px;
}

.cover-image {
  width: 160px;
  height: 100px;
  object-fit: cover;
}
</style>