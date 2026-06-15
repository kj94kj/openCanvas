<template>
  <div>
    <h1>커버 목록</h1>

    <button @click="goCreateCover">
    글쓰기
    </button>

    <div>
      <input
        v-model="keyword"
        placeholder="제목 검색"
        @keyup.enter="searchCovers"
      />
      <button @click="searchCovers">검색</button>
      <button @click="clearSearch" v-if="isSearchMode">전체 보기</button>
    </div>

    <hr />

    <div>
      <button @click="changeSort('new')">최신순</button>
      <button @click="changeSort('likes')">좋아요순</button>
      <button @click="changeSort('views')">조회수순</button>
    </div>

    <hr />

    <p v-if="covers.length === 0">표시할 커버가 없습니다.</p>

    <div
      v-for="cover in covers"
      :key="cover.id"
      class="card"
      @click="goContent(cover)"
    >
      <img
        v-if="cover.coverImageUrl"
        :src="cover.coverImageUrl"
        class="cover-image"
      />

      <h3>{{ cover.title }}</h3>
      <p>방 타입: {{ cover.roomType }}</p>
      <p>조회수: {{ cover.view }}</p>
      <p>좋아요: {{ cover.likeCount }}</p>
      <p>생성일: {{ formatDate(cover.time) }}</p>
    </div>

    <hr />

    <div v-if="!isSearchMode">
      <button @click="prevPage" :disabled="page === 0">
        이전
      </button>

      <span>{{ page + 1 }} / {{ totalPages }}</span>

      <button @click="nextPage" :disabled="page >= totalPages - 1">
        다음
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '@/api'
import { useRouter } from 'vue-router'

const router = useRouter()

const covers = ref([])
const page = ref(0)
const size = ref(10)
const totalPages = ref(0)

const sortType = ref('new')

const keyword = ref('')
const isSearchMode = ref(false)

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
.card {
  border: 1px solid #ddd;
  padding: 12px;
  margin-bottom: 12px;
  cursor: pointer;
}

.cover-image {
  width: 160px;
  height: 120px;
  object-fit: cover;
}
</style>