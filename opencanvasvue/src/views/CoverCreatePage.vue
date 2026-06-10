<template>
  <div>
    <h1>커버 생성</h1>

    <div>
      <label>제목</label>
      <input v-model="title" placeholder="제목 입력" />
    </div>

    <div>
      <label>이미지 URL</label>
      <input v-model="coverImageUrl" placeholder="이미지 URL 입력" />
    </div>

    <div>
      <label>최대 작가 수</label>
      <input v-model.number="limit" type="number" placeholder="최대 작가 수" />
    </div>

    <button @click="createCover">커버 생성</button>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/api/axios'

const router = useRouter()

const title = ref('')
const coverImageUrl = ref('')
const limit = ref(1)

async function createCover() {
  try {
    const requestBody = {
      title: title.value,
      coverImageUrl: coverImageUrl.value,
      limit: limit.value
    }

    const response = await api.post('/api/covers', requestBody)

    const createdCover = response.data

    router.push(`/content/${createdCover.id}`)
  } catch (error) {
    console.error(error)
    alert(error.response?.data || '커버 생성에 실패했습니다.')
  }
}
</script>