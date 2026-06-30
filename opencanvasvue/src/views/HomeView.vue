<template>
  <main class="login-page">
    <section class="login-content">
      <div class="logo">
        Open<span>Canvas</span>
      </div>

      <button class="login-button" @click="loginWithGoogle">
        로그인해서 시작하세요
      </button>
    </section>
  </main>
</template>

<script setup>
function loginWithGoogle() {
  window.location.href =
  'http://ec2-43-201-95-35.ap-northeast-2.compute.amazonaws.com/oauth2/authorization/google?redirect_uri=http://ec2-43-201-95-35.ap-northeast-2.compute.amazonaws.com/auth/callback&mode=login'
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #ffffff;
}

.login-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 36px;
}

.logo {
  color: #202124;
  font-size: 52px;
  font-weight: 800;
}

.logo span {
  color: #ff6f61;
}

.login-button {
  padding: 14px 28px;
  border: 1px solid #ff6f61;
  border-radius: 8px;
  background: #ff6f61;
  color: #ffffff;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 5px 16px rgba(255, 111, 97, 0.25);
  transition: 0.2s ease;
}

.login-button:hover {
  transform: translateY(-2px);
  background: #f66053;
  box-shadow: 0 8px 20px rgba(255, 111, 97, 0.32);
}
</style>