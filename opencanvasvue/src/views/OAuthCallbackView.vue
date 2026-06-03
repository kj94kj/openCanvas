<template>
  <div>
    <h1>로그인 처리 중...</h1>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

onMounted(() => {
  const params = new URLSearchParams(window.location.search)
  const accessToken = params.get('access_token')

  if (accessToken) {
    localStorage.setItem('accessToken', accessToken)

    router.push('/mypage')
  } else {
    alert('토큰이 없습니다.')
    router.push('/')
  }
})
</script>