<template>
  <main class="writings-page">
    <header class="page-head">
      <div>
        <h1>내가 쓴 글</h1>
        <p>내가 참여한 이야기들을 다시 확인할 수 있습니다.</p>
      </div>

      <button class="back-button" @click="goMainPage">
        메인으로
      </button>
    </header>

    <section v-if="writings.length > 0" class="cover-grid">
      <article
        v-for="cover in writings"
        :key="cover.coverId"
        class="cover-card"
        @click="openContent(cover.coverId)"
      >
        <div class="image-wrap">
          <img
            :src="getCoverImage(cover)"
            :alt="cover.title"
          />
        </div>

        <div class="card-body">
          <h3>{{ cover.title }}</h3>

          <p class="room-type" :class="cover.roomType">
            {{ getRoomTypeText(cover.roomType) }}
          </p>

          <div class="my-writing-count">
            내가 쓴 글 {{ cover.myWritingCount ?? 0 }}개
          </div>

          <div class="stats">
            <span class="like">♡ {{ cover.likeCount ?? 0 }}</span>
            <span>◉ {{ cover.view ?? 0 }}</span>
          </div>
        </div>
      </article>
    </section>

    <p v-if="writings.length === 0 && !errorMessage" class="empty-text">
      내가 쓴 글이 없습니다.
    </p>

    <p v-if="errorMessage" class="error-text">
      {{ errorMessage }}
    </p>
  </main>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const writings = ref([])
const errorMessage = ref('')

const defaultCoverImage = '/images/default-cover.png'

onMounted(() => {
  getMyWritings()
})

function getCoverImage(cover) {
  return cover.coverImageUrl || defaultCoverImage
}

function getRoomTypeText(roomType) {
  if (roomType === 'AVAILABLE') return '쓰기 가능'
  if (roomType === 'EDITING') return '작성 중'
  if (roomType === 'COMPLETE') return '작성 마감'
  return '상태 없음'
}

async function getMyWritings() {
  try {
    const token = localStorage.getItem('accessToken')

    const response = await fetch('http://localhost:8080/api/users/writings', {
      headers: {
        Authorization: `Bearer ${token}`
      }
    })

    if (!response.ok) {
      throw new Error('내가 쓴 글 조회 실패')
    }

    writings.value = await response.json()
  } catch (error) {
    errorMessage.value = error.message
  }
}

function goMainPage() {
  router.push('/mainpage')
}

function openContent(coverId) {
  router.push(`/content/${coverId}`)
}
</script>

<style scoped>
.writings-page {
  width: 100%;
  max-width: 1480px;
  margin: 0 auto;
  padding: 22px 32px 40px;
  color: #111;
  background: #fff;
}

.page-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 28px;
  margin-bottom: 30px;
  border-bottom: 1px solid #f0f0f0;
}

.page-head h1 {
  margin: 0;
  font-size: 28px;
  font-weight: 800;
}

.page-head p {
  margin: 8px 0 0;
  color: #777;
  font-size: 15px;
}

.cover-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 28px 34px;
}

.cover-card {
  min-height: 340px;
  overflow: hidden;
  border: 1px solid #ddd;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  cursor: pointer;
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}

.cover-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 18px rgba(0, 0, 0, 0.08);
}

.image-wrap {
  width: 100%;
  aspect-ratio: 4 / 2.85;
  background: #f5f5f5;
}

.image-wrap img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.card-body {
  padding: 14px 16px;
}

.card-body h3 {
  margin: 0 0 8px;
  font-size: 17px;
  font-weight: 800;
}

.room-type {
  display: inline-flex;
  align-items: center;
  width: fit-content;
  margin: 0;
  padding: 4px 9px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 700;
}

.room-type.AVAILABLE {
  background: #fff1ef;
  color: #ff6254;
}

.room-type.EDITING {
  background: #fff8e7;
  color: #c47a00;
}

.room-type.COMPLETE {
  background: #f1f1f1;
  color: #777;
}

.my-writing-count {
  width: fit-content;
  margin-top: 12px;
  padding: 5px 10px;
  border-radius: 999px;
  background: #f7f7f7;
  color: #555;
  font-size: 13px;
  font-weight: 700;
}

.stats {
  display: flex;
  justify-content: space-between;
  margin-top: 18px;
  color: #666;
  font-size: 15px;
}

.like {
  color: #ff6254;
}

.empty-text,
.error-text {
  margin-top: 36px;
  text-align: center;
  color: #777;
}

.error-text {
  color: #ff6254;
  font-weight: 700;
}

.back-button {
  height: 46px;
  padding: 0 18px;
  border: 1px solid #ddd;
  border-radius: 8px;
  background: #fff;
  color: #111;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
}

.back-button:hover {
  border-color: #ff8a80;
  color: #ff6254;
}

@media (max-width: 1100px) {
  .cover-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 800px) {
  .writings-page {
    padding: 18px 20px 34px;
  }

  .page-head {
    align-items: stretch;
    flex-direction: column;
    gap: 16px;
  }

  .back-button {
    width: fit-content;
  }

  .cover-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>