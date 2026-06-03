<template>
  <div>
    <h1>내 정보</h1>

    <div v-if="user">
      <p>id: {{ user.id }}</p>
      <p>닉네임: {{ user.nickname }}</p>
      <p>이메일: {{ user.email }}</p>
      <p>권한: {{ user.role }}</p>
    </div>

    <p v-if="errorMessage">{{ errorMessage }}</p>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const user = ref(null)
const errorMessage = ref('')

onMounted(() => {
  getUserInfo()
})

async function getUserInfo() {
  try {
    const token = localStorage.getItem('accessToken')

    const response = await fetch('http://localhost:8080/api/users/', {
      headers: {
        Authorization: `Bearer ${token}`
      }
    })

    if (!response.ok) {
      throw new Error('내 정보 조회 실패')
    }

    user.value = await response.json()
  } catch (error) {
    errorMessage.value = error.message
  }
}
</script>