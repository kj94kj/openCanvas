<template>
  <main class="main-page">
    <header class="top-bar">
      <div class="search-box">
        <span class="search-icon">⌕</span>
        <input
          v-model="keyword"
          placeholder="검색어를 입력하세요"
          @keyup.enter="searchCovers"
        />
        <button v-if="isSearchMode" class="clear-button" @click="clearSearch">
          전체 보기
        </button>
      </div>

      <div class="user-menu">
        <button @click="router.push('/mypage/likes')">♡ 좋아요 누른 글</button>
        <button @click="router.push('/mypage/writings')">✎ 내가 쓴 글</button>
        <div class="avatar">나</div>
      </div>
    </header>

    <section class="content-head">
      <h1>이야기 둘러보기</h1>

      <select v-model="sortType" @change="changeSort(sortType)">
        <option value="new">최신순</option>
        <option value="likes">좋아요순</option>
        <option value="views">조회수순</option>
      </select>
    </section>

    <section class="cover-grid">
      <button class="create-card" @click="goCreateCover">
        <span class="plus">＋</span>
        <strong>새로 쓰기</strong>
      </button>

      <article
        v-for="cover in covers"
       :key="cover.id"
       class="cover-card"
        @click="goContent(cover)"
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

          <div class="stats">
            <span class="like">♡ {{ cover.likeCount ?? 0 }}</span>
            <span>◉ {{ cover.view ?? 0 }}</span>
          </div>
       </div>
      </article>
    </section>

    <p v-if="covers.length === 0" class="empty-text">
      표시할 커버가 없습니다.
    </p>

    <nav v-if="!isSearchMode && totalPages > 0" class="pagination">
      <button @click="prevPage" :disabled="page === 0">이전</button>
      <span>{{ page + 1 }} / {{ totalPages }}</span>
      <button @click="nextPage" :disabled="page >= totalPages - 1">다음</button>
    </nav>
  </main>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '@/api'
import { useRouter } from 'vue-router'

const router = useRouter()

const covers = ref([])
const page = ref(0)
const size = ref(11)
const totalPages = ref(0)

const sortType = ref('new')

const keyword = ref('')
const isSearchMode = ref(false)

// 기본 커버 이미지는 여기다가 저장.
const defaultCoverImage = '/images/default-cover.png'

function getCoverImage(cover) {
  return cover.coverImageUrl || defaultCoverImage
}

function getRoomTypeText(roomType) {
  if (roomType === 'AVAILABLE') return '쓰기 가능'
  if (roomType === 'EDITING') return '작성 중'
  if (roomType === 'COMPLETE') return '작성 마감'
  return '상태 없음'
}

async function getCovers() {
  isSearchMode.value = false

  const response = await api.get(`/api/covers/${sortType.value}`, {
    params: {
      page: page.value,
      size: size.value
    }
  })

  covers.value = response.data.content
  totalPages.value = response.data.totalPages
}

async function searchCovers() {
  if (!keyword.value.trim()) {
    getCovers()
    return
  }

  const response = await api.get('/api/covers/search', {
    params: {
      keyword: keyword.value
    }
  })

  covers.value = response.data
  isSearchMode.value = true
}

function clearSearch() {
  keyword.value = ''
  page.value = 0
  getCovers()
}

function changeSort(type) {
  sortType.value = type
  page.value = 0
  keyword.value = ''
  getCovers()
}

function prevPage() {
  if (page.value > 0) {
    page.value--
    getCovers()
  }
}

function nextPage() {
  if (page.value < totalPages.value - 1) {
    page.value++
    getCovers()
  }
}

function goContent(cover) {
  router.push(`/content/${cover.id}`)
}

function formatDate(dateText) {
  if (!dateText) return ''
  return new Date(dateText).toLocaleString()
}

function goCreateCover() {
  router.push('/cover-create')
}

onMounted(() => {
  getCovers()
})
</script>

<style scoped>
.main-page {
  width: 100%;
  max-width: 1480px;
  margin: 0 auto;
  padding: 22px 32px 40px;
  color: #111;
  background: #fff;
}

.top-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  padding-bottom: 34px;
  border-bottom: 1px solid #f0f0f0;
}

.search-box {
  display: flex;
  align-items: center;
  width: min(430px, 100%);
  height: 52px;
  padding: 0 16px;
  border: 1px solid #ddd;
  border-radius: 8px;
  background: #fff;
}

.search-icon {
  margin-right: 10px;
  font-size: 24px;
  color: #777;
}

.search-box input {
  flex: 1;
  border: 0;
  outline: 0;
  font-size: 16px;
}

.clear-button {
  border: 0;
  background: transparent;
  color: #ff6254;
  font-weight: 700;
  cursor: pointer;
}

.user-menu {
  display: flex;
  align-items: center;
  gap: 14px;
}

.user-menu button {
  height: 52px;
  padding: 0 18px;
  border: 1px solid #ddd;
  border-radius: 8px;
  background: #fff;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
}

.avatar {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  background: #f4f4f4;
  font-weight: 800;
  color: #ff6254;
}

.content-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 30px 0 18px;
}

.content-head h1 {
  margin: 0;
  font-size: 28px;
  font-weight: 800;
}

.content-head select {
  width: 130px;
  height: 48px;
  padding: 0 14px;
  border: 1px solid #ddd;
  border-radius: 8px;
  background: #fff;
  font-size: 15px;
  cursor: pointer;
}

.cover-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 28px 34px;
}

.create-card,
.cover-card {
  min-height: 340px;
  border: 1px solid #ddd;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.create-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 22px;
  border: 1.5px dashed #ff8a80;
  color: #ff6254;
  font-size: 22px;
  cursor: pointer;
}

.plus {
  width: 76px;
  height: 76px;
  border: 1.5px solid #ff6254;
  border-radius: 50%;
  display: grid;
  place-items: center;
  font-size: 44px;
  font-weight: 300;
}

.cover-card {
  overflow: hidden;
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

.empty-image {
  height: 100%;
  display: grid;
  place-items: center;
  color: #aaa;
}

.card-body {
  padding: 14px 16px;
}

.card-body h3 {
  margin: 0 0 8px;
  font-size: 17px;
  font-weight: 800;
}

.card-body p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.stats {
  display: flex;
  justify-content: space-between;
  margin-top: 22px;
  color: #666;
  font-size: 15px;
}

.like {
  color: #ff6254;
}

.empty-text {
  margin-top: 36px;
  text-align: center;
  color: #777;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-top: 34px;
}

.pagination button {
  padding: 10px 16px;
  border: 1px solid #ddd;
  border-radius: 8px;
  background: #fff;
  cursor: pointer;
}

.pagination button:disabled {
  color: #bbb;
  cursor: not-allowed;
}

@media (max-width: 1100px) {
  .cover-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 800px) {
  .top-bar,
  .content-head {
    align-items: stretch;
    flex-direction: column;
  }

  .search-box {
    width: 100%;
  }

  .user-menu {
    justify-content: flex-end;
  }

  .cover-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
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

</style>