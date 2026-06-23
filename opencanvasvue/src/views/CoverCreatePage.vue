<template>
  <main class="cover-create-page">
    <section class="create-panel">
      <div class="panel-head">
        <p class="eyebrow">OpenCanvas</p>
        <h1>새 커버 만들기</h1>
        <p class="description">
          함께 이어 쓸 이야기의 첫 화면을 만들어보세요.
        </p>
      </div>

      <form class="cover-form" @submit.prevent="createCover">
        <label class="form-field">
          <span>제목</span>
          <input
            v-model.trim="title"
            placeholder="예: 사라진 도시의 마지막 기록"
          />
        </label>

        <label class="form-field">
          <span>이미지 URL</span>
          <input
            v-model.trim="coverImageUrl"
            placeholder="비워두면 기본 커버 이미지가 사용됩니다"
          />
        </label>

        <label class="form-field">
          <span>최대 작가 수</span>
          <input
            v-model.number="limit"
            type="number"
            min="1"
            max="20"
            placeholder="최대 작가 수"
          />
        </label>

        <button class="submit-button" type="submit" :disabled="isSubmitting">
          {{ isSubmitting ? '생성 중...' : '커버 생성' }}
        </button>
      </form>
    </section>
  </main>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/api'

const router = useRouter()

const title = ref('')
const coverImageUrl = ref('')
const limit = ref(1)
const isSubmitting = ref(false)

const canSubmit = computed(() => title.value.trim() && limit.value >= 1)

async function createCover() {
  if (!canSubmit.value || isSubmitting.value) return

  try {
    isSubmitting.value = true

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
  } finally {
    isSubmitting.value = false
  }
}
</script>

<style scoped>
.cover-create-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 24px;
  background: #fafafa;
}

.create-panel {
  width: 100%;
  max-width: 520px;
  padding: 42px;
  border: 1px solid #eeeeee;
  border-radius: 14px;
  background: #ffffff;
  box-shadow: 0 16px 40px rgba(15, 23, 42, 0.08);
}

.panel-head {
  margin-bottom: 32px;
  text-align: center;
}

.eyebrow {
  margin: 0 0 10px;
  color: #ff6b5f;
  font-size: 13px;
  font-weight: 700;
}

.panel-head h1 {
  margin: 0;
  color: #222222;
  font-size: 30px;
  font-weight: 800;
}

.description {
  margin: 12px 0 0;
  color: #777777;
  font-size: 15px;
  line-height: 1.5;
}

.cover-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 9px;
}

.form-field span {
  color: #333333;
  font-size: 14px;
  font-weight: 700;
}

.form-field input {
  width: 100%;
  box-sizing: border-box;
  padding: 15px 16px;
  border: 1px solid #dddddd;
  border-radius: 10px;
  outline: none;
  color: #222222;
  font-size: 15px;
  background: #ffffff;
  transition: border-color 0.15s ease, box-shadow 0.15s ease;
}

.form-field input:focus {
  border-color: #ff6b5f;
  box-shadow: 0 0 0 4px rgba(255, 107, 95, 0.14);
}

.submit-button {
  margin-top: 8px;
  padding: 16px;
  border: none;
  border-radius: 10px;
  background: #ff6b5f;
  color: #ffffff;
  font-size: 16px;
  font-weight: 800;
  cursor: pointer;
  transition: background 0.15s ease, transform 0.15s ease;
}

.submit-button:hover {
  background: #f25549;
  transform: translateY(-1px);
}

.submit-button:disabled {
  background: #ffc1bb;
  cursor: not-allowed;
  transform: none;
}

@media (max-width: 640px) {
  .cover-create-page {
    align-items: flex-start;
    padding: 32px 16px;
  }

  .create-panel {
    padding: 30px 22px;
  }

  .panel-head h1 {
    font-size: 26px;
  }
}
</style>