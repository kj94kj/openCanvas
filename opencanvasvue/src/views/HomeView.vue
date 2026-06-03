<template>
  <div>
    <h1>메인 화면</h1>

    <button @click="loginWithGoogle">
      구글 로그인
    </button>
  </div>
</template>

// 지금 여기 주소를 잘 보고 로그인 하면 이런식으로 리다이렉트 주소로 설정해주면 됨.
<script setup>
function loginWithGoogle() {
  window.location.href =
    'http://localhost:8080/oauth2/authorization/google?redirect_uri=http://localhost:5173/oauth2/callback&mode=login'
}

</script>