<template>
  <main class="content-page">
    <header class="page-header">
      <button class="back-button" @click="goMainPage">
        ← 메인페이지
      </button>

      <div class="like-area">
        <button
          class="like-button"
          :class="{ liked: isLiked }"
          @click="toggleLike"
        >
          {{ isLiked ? '♥' : '♡' }}
        </button>

        <span class="like-count">
          {{ contentInfo?.likeCount ?? 0 }}
        </span>
      </div>
    </header>

    <div class="content-layout">
      <section class="left-panel">
        <div class="writing-box">
          <div class="title-row">
            <h1>{{ contentInfo?.title }}</h1>
            <span v-if="selectedWriting" class="current-version">
              현재 버전 {{ selectedWriting.depth }}-{{ selectedWriting.siblingIndex }}
            </span>
          </div>

          <hr />

          <div v-if="!selectedWriting" class="empty-box">
            <p>오른쪽에서 읽을 버전을 선택해주세요.</p>
          </div>

          <div v-else class="story-area">
            <article
              v-for="writing in selectedPath"
              :key="writing.writingId"
              class="story-block"
            >
              <p class="story-body">
                {{ writing.body }}
              </p>
            </article>
          </div>
        </div>
      </section>

      <aside class="right-panel">
        <h2>버전 목록</h2>

        <div class="version-grid">
          <div
            v-for="row in groupedWritings"
            :key="row.depth"
            class="version-row"
            :class="{ single: row.columns.filter(Boolean).length === 1 }"
          >
            <article
              v-for="(writing, index) in row.columns"
              :key="writing?.writingId ?? `empty-${row.depth}-${index}`"
              class="version-card"
              :class="{
                selected: isSelectedWriting(writing),
                path: isSelectedPathWriting(writing),
                empty: !writing
              }"
              @click="writing && selectWriting(writing)"
            >
              <template v-if="writing">
                <div class="version-header">
                  <strong>{{ writing.depth }}-{{ writing.siblingIndex }}</strong>
                  <span>{{ displayUsername(writing.username) }}</span>
                </div>

                <div class="version-time">
                  {{ formatRelativeTime(writing.time) }}
                </div>

                <p class="version-preview">
                  {{ makePreview(writing.body) }}
                </p>
              </template>
            </article>
          </div>
        </div>

        <button
          class="enter-button"
          :class="{ disabled: !canEnterRoom }"
          :disabled="!canEnterRoom"
          @click="enterWritingRoom"
        >
          {{ writingButtonText }}
        </button>
      </aside>
    </div>
  </main>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import api from '@/api'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const contentInfo = ref(null)
const writings = ref([])
const selectedWriting = ref(null)
const selectedPath = ref([])
const isLiked = ref(false)

const coverId = route.params.coverId

onMounted(async () => {
  await fetchContent()
  await fetchLikeCheck()
})

const groupedWritings = computed(() => {
  const sorted = [...writings.value].sort((a, b) => {
    if (a.depth !== b.depth) {
      return a.depth - b.depth
    }

    return a.siblingIndex - b.siblingIndex
  })

  const map = new Map()

  sorted.forEach(writing => {
    if (!map.has(writing.depth)) {
      map.set(writing.depth, {
        depth: writing.depth,
        columns: [null, null]
      })
    }

    const row = map.get(writing.depth)

    if (writing.siblingIndex === 1) {
      row.columns[0] = writing
    }

    if (writing.siblingIndex === 2) {
      row.columns[1] = writing
    }
  })

  return [...map.values()]
})

const writingButtonText = computed(() => {
  if (!contentInfo.value) {
    return '로딩중'
  }

  if (contentInfo.value.roomType === 'AVAILABLE') {
    if (!selectedWriting.value && writings.value.length > 0) {
      return '읽을 버전을 선택해 주세요'
    }

    return '여기 부터 이어쓰기'
  }

  if (contentInfo.value.roomType === 'EDITING') {
    return '작성중인 글 구경하기'
  }

  if (contentInfo.value.roomType === 'COMPLETE') {
    return '이야기가 끝났습니다.'
  }

  return '로딩중'
})

const canWrite = computed(() => {
  if (!contentInfo.value) return false

  if (contentInfo.value.roomType === 'COMPLETE') return false
  if (contentInfo.value.roomType === 'EDITING') return false

  if (writings.value.length === 0) return true
  if (!selectedWriting.value) return false

  const childCount = writings.value.filter(w =>
    w.depth === selectedWriting.value.depth + 1
  ).length

  return childCount < 2
})

const canEnterRoom = computed(() => {
  if (!contentInfo.value) return false
  if (contentInfo.value.roomType === 'COMPLETE') return false
  if (contentInfo.value.roomType === 'EDITING') return true

  if (contentInfo.value.roomType === 'AVAILABLE') {
    return canWrite.value
  }

  return false
})

function goMainPage() {
  router.push('/mainpage')
}

function isSelectedWriting(writing) {
  if (!writing || !selectedWriting.value) return false

  return (
    writing.depth === selectedWriting.value.depth &&
    writing.siblingIndex === selectedWriting.value.siblingIndex
  )
}

function isSelectedPathWriting(writing) {
  if (!writing) return false

  return selectedPath.value.some(pathWriting =>
    pathWriting.depth === writing.depth &&
    pathWriting.siblingIndex === writing.siblingIndex
  )
}

async function fetchContent() {
  try {
    const response = await api.get(`/api/content/${coverId}`)

    const data = response.data
    contentInfo.value = data.firstContentDto
    writings.value = data.simpleWritingDtos ?? []
  } catch (error) {
    console.error('컨텐츠 조회 실패', error)
  }
}

async function selectWriting(writing) {
  selectedWriting.value = writing

  await fetchParents(writing)
  await checkWriteStatus()
}

async function fetchParents(writing) {
  try {
    const requestDto = {
      depth: writing.depth,
      siblingIndex: writing.siblingIndex,
      title: contentInfo.value.title
    }

    const response = await api.post('/api/writings/parents', requestDto)

    selectedPath.value = response.data ?? []
  } catch (error) {
    console.error('부모 글 조회 실패', error)
    selectedPath.value = []
  }
}

async function checkWriteStatus() {
  try {
    const response = await api.get(`/api/covers/${coverId}/room-type`)

    if (contentInfo.value) {
      contentInfo.value.roomType = response.data
    }
  } catch (error) {
    console.error('방 상태 조회 실패', error)
  }
}

function makePreview(body) {
  if (!body) return ''
  if (body.length <= 60) return body

  return body.substring(0, 60) + '...'
}

function displayUsername(username) {
  if (!username) return ''

  return username.split('@')[0]
}

function formatRelativeTime(time) {
  if (!time) return ''

  const date = new Date(time)
  const diffMs = Date.now() - date.getTime()
  const diffMinutes = Math.floor(diffMs / (1000 * 60))
  const diffHours = Math.floor(diffMinutes / 60)
  const diffDays = Math.floor(diffHours / 24)

  if (diffMinutes < 1) return '방금 전'
  if (diffMinutes < 60) return `${diffMinutes}분 전`
  if (diffHours < 24) return `${diffHours}시간 전`
  if (diffDays < 7) return `${diffDays}일 전`

  return date.toLocaleDateString()
}

async function enterWritingRoom() {
  if (!contentInfo.value) {
    return
  }

  const roomId = contentInfo.value.roomId
  const roomType = contentInfo.value.roomType

  if (roomType === 'AVAILABLE') {
    if (!selectedWriting.value && writings.value.length > 0) {
      alert('글을 선택해주세요.')
      return
    }

    await enterAsEditor(roomId)
    return
  }

  if (roomType === 'EDITING') {
    await enterAsViewer(roomId)
    return
  }

  if (roomType === 'COMPLETE') {
    alert('이미 완료된 글입니다.')
  }
}

async function enterAsEditor(roomId) {
  try {
    let writingDto

    if (writings.value.length === 0) {
      writingDto = {
        depth: 0,
        siblingIndex: 0,
        title: contentInfo.value.title
      }
    } else {
      writingDto = {
        depth: selectedWriting.value.depth,
        siblingIndex: selectedWriting.value.siblingIndex,
        title: contentInfo.value.title
      }
    }

    const response = await api.post(`/api/rooms/${roomId}/create`, writingDto)

    router.push({
      path: `/writing-room/${roomId}`,
      query: {
        mode: 'editor',
        writingId: selectedWriting.value?.writingId,
        coverId: coverId
      },
      state: {
        chatRoom: response.data
      }
    })
  } catch (error) {
    console.error(error)
    alert(error.response?.data || '작성자로 입장하지 못했습니다.')
  }
}

async function enterAsViewer(roomId) {
  try {
    const response = await api.get(`/api/rooms/${roomId}/enter`)

    router.push({
      path: `/writing-room/${roomId}`,
      query: {
        mode: 'viewer',
        coverId: coverId
      },
      state: {
        chatRoom: response.data
      }
    })
  } catch (error) {
    console.error(error)
    alert(error.response?.data || '관전자로 입장하지 못했습니다.')
  }
}

async function fetchLikeCheck() {
  try {
    const response = await api.get('/api/content/like-check', {
      params: {
        coverId
      }
    })

    isLiked.value = response.data
  } catch (error) {
    console.error('좋아요 상태 조회 실패', error)
    isLiked.value = false
  }
}

async function toggleLike() {
  if (!contentInfo.value) return

  const beforeLiked = isLiked.value

  try {
    const response = await api.post('/api/content/like-toggle', null, {
      params: {
        coverId,
        likeType: 'LIKE'
      }
    })

    const afterLiked = response.data

    isLiked.value = afterLiked

    if (beforeLiked === false && afterLiked === true) {
      contentInfo.value.likeCount = (contentInfo.value.likeCount ?? 0) + 1
    }

    if (beforeLiked === true && afterLiked === false) {
      contentInfo.value.likeCount = Math.max((contentInfo.value.likeCount ?? 0) - 1, 0)
    }
  } catch (error) {
    console.error('좋아요 토글 실패', error)
    alert(error.response?.data || '좋아요 처리에 실패했습니다.')
  }
}
</script>

<style scoped>
.content-page {
  min-height: 100vh;
  padding: 24px 28px;
  background: #fbfbfb;
  color: #222;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1440px;
  margin: 0 auto 16px;
}

.back-button {
  padding: 10px 14px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
  color: #444;
  cursor: pointer;
  font-size: 14px;
}

.back-button:hover {
  border-color: #ff5a57;
  color: #ff5a57;
}

.like-area {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 14px;
  border: 1px solid #ffd6d4;
  border-radius: 8px;
  background: #fff;
}

.like-button {
  border: none;
  background: none;
  font-size: 22px;
  cursor: pointer;
  color: #ff5a57;
  line-height: 1;
}

.like-button.liked {
  color: #ff3f3b;
}

.like-count {
  font-size: 15px;
  font-weight: 700;
}

.content-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 520px;
  gap: 24px;
  max-width: 1440px;
  margin: 0 auto;
}

.left-panel,
.right-panel {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.04);
}

.left-panel {
  min-height: calc(100vh - 112px);
  padding: 44px 32px;
  overflow-y: auto;
}

.writing-box {
  max-width: 860px;
  margin: 0 auto;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 18px;
}

.title-row h1 {
  margin: 0;
  font-size: 40px;
  line-height: 1.2;
}

.current-version {
  flex-shrink: 0;
  padding: 7px 10px;
  border-radius: 8px;
  background: #fff0ef;
  color: #ff4f4b;
  font-size: 14px;
  font-weight: 700;
}

hr {
  margin: 28px 0;
  border: none;
  border-top: 1px solid #eee;
}

.empty-box {
  min-height: 420px;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #888;
}

.story-area {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.story-block {
  padding: 0;
}

.story-body {
  margin: 0;
  white-space: pre-wrap;
  line-height: 1.9;
  font-size: 17px;
}

.right-panel {
  position: sticky;
  top: 24px;
  height: calc(100vh - 48px);
  padding: 28px 20px 18px;
  display: flex;
  flex-direction: column;
}

.right-panel h2 {
  margin: 0 0 24px;
  font-size: 22px;
}

.version-grid {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding-right: 4px;
  overflow-y: auto;
}

.version-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.version-row.single {
  grid-template-columns: 1fr 1fr;
}

.version-card {
  min-height: 128px;
  padding: 16px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
  cursor: pointer;
  transition: border-color 0.15s ease, background-color 0.15s ease, box-shadow 0.15s ease;
}

.version-card:hover {
  border-color: #ffaaa7;
  box-shadow: 0 6px 18px rgba(255, 90, 87, 0.08);
}

.version-card.path {
  border-color: #ffd1cf;
  background: #fff7f6;
}

.version-card.selected {
  border: 2px solid #ff514d;
  background: #fff;
}

.version-card.empty {
  visibility: hidden;
  pointer-events: none;
}

.version-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.version-header strong {
  color: #ff4f4b;
  font-size: 22px;
}

.version-header span {
  color: #555;
  font-size: 14px;
  font-weight: 700;
}

.version-time {
  margin-bottom: 10px;
  color: #888;
  font-size: 13px;
}

.version-preview {
  margin: 0;
  color: #555;
  font-size: 14px;
  line-height: 1.55;
}

.enter-button {
  width: 100%;
  margin-top: 18px;
  padding: 18px 20px;
  border: none;
  border-radius: 8px;
  background: #ff514d;
  color: #fff;
  cursor: pointer;
  font-size: 24px;
  font-weight: 800;
  box-shadow: 0 10px 22px rgba(255, 81, 77, 0.24);
}

.enter-button.disabled {
  background: #c9c9c9;
  cursor: not-allowed;
  box-shadow: none;
}

@media (max-width: 1100px) {
  .content-layout {
    grid-template-columns: 1fr;
  }

  .right-panel {
    position: static;
    height: auto;
  }
}
</style>
