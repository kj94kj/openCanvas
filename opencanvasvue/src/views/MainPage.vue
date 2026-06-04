<template>
  <div>
    <h1>커버 목록</h1>

    <div>
      <button @click="changeSort('new')">최신순</button>
      <button @click="changeSort('likes')">좋아요순</button>
      <button @click="changeSort('views')">조회수순</button>
    </div>

    <hr />

    <div
      v-for="cover in covers"
      :key="cover.coverId"
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
      <p>생성일: {{ formatDate(cover.coverTime) }}</p>
    </div>

    <hr />

    <div>
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
import axios from 'axios'
import { useRouter } from 'vue-router'

const router = useRouter()

const covers = ref([])
const page = ref(0)
const size = ref(10)
const totalPages = ref(0)

const sortType = ref('new')

async function getCovers() {
  const response = await axios.get(`/api/covers/${sortType.value}`, {
    params: {
      page: page.value,
      size: size.value
    }
  })

  // 실제용
  // covers.value = response.data.content
  // totalPages.value = response.data.totalPages

  // 테스트용
  covers.value = [
    {
      coverId: 1,
      contentId: 101,
      title: '첫 번째 테스트 작품',
      coverImageUrl: '',
      roomType: 'PUBLIC',
      view: 123,
      likeCount: 15,
      coverTime: '2026-06-04T10:00:00'
    },
    {
      coverId: 2,
      contentId: 102,
      title: '두 번째 테스트 작품',
      coverImageUrl: '',
      roomType: 'PRIVATE',
      view: 77,
      likeCount: 5,
      coverTime: '2026-06-03T14:30:00'
    }
  ]

  totalPages.value = 3
}

function changeSort(type) {
  sortType.value = type
  page.value = 0
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
  router.push(`/content/${cover.contentId}`)
}

function formatDate(dateText) {
  if (!dateText) return ''
  return new Date(dateText).toLocaleString()
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