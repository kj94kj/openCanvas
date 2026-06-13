<template>
  <div class="content-page">
        <!-- 글쓰기 버튼 -->
    <div class="write-area">
      <button
        @click="enterWritingRoom"
        :disabled="!canEnterRoom"
      >
        {{ writingButtonText }}
      </button>
    </div>

    <!-- 왼쪽: 글 영역 -->
    <section class="left-panel">
      <div v-if="!selectedWriting" class="empty-box">
        <h2>{{ contentInfo?.title }}</h2>
        <p>버전을 선택해주세요.</p>
      </div>

      <div v-else class="writing-box">
        <h2>{{ contentInfo?.title }}</h2>

        <div class="selected-info">
          <p>선택한 버전: {{ selectedWriting.depth }}-{{ selectedWriting.siblingIndex }}</p>
          <p>작성자: {{ selectedWriting.username }}</p>
          <p>작성 시간: {{ formatDate(selectedWriting.time) }}</p>
        </div>

        <hr />

        <!-- 선택한 버전까지의 글 -->
        <div class="story-area">
          <div
            v-for="writing in selectedPath"
            :key="writing.writingId"
            class="story-block"
          >
            <div class="story-meta">
              {{ writing.depth }}-{{ writing.siblingIndex }}
              / {{ writing.username }}
              / {{ formatDate(writing.time) }}
            </div>

            <p class="story-body">
              {{ writing.body }}
            </p>
          </div>
        </div>

        <hr />

      </div>
    </section>

    <!-- 오른쪽: 버전 목록 -->
    <aside class="right-panel">
      <h3>버전 목록</h3>

      <div
        v-for="row in groupedWritings"
        :key="row.depth"
        class="version-row"
      >
        <div
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
              <span>{{ writing.username }}</span>
           </div>

            <div class="version-time">
              {{ formatDate(writing.time) }}
            </div>

            <p class="version-preview">
             {{ makePreview(writing.body) }}
            </p>
         </template>
        </div>
      </div>
    </aside>
  </div>
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

const coverId = route.params.coverId

onMounted(async () => {
  await fetchContent()
  // await refreshRoomTypeWithRetry()
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

const writingButtonText = computed(() => {
  if (!contentInfo.value) {
    return '로딩중'
  }

  if (contentInfo.value.roomType === 'AVAILABLE') {
    if (!selectedWriting.value && writings.value.length > 0) {
      return '글을 선택해 주세요'
    }

    return '이어쓰기'
  }

  if (contentInfo.value.roomType === 'EDITING') {
    return '관전하기'
  }

  if (contentInfo.value.roomType === 'COMPLETE') {
    return '완료된 글'
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

  if (body.length <= 50) return body

  return body.substring(0, 50) + '...'
}

function formatDate(time) {
  if (!time) return ''

  return new Date(time).toLocaleString()
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
        contentId: contentInfo.value.id
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
        mode: 'viewer'
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


  function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms))
}

async function refreshRoomTypeWithRetry() {
  for (let i = 0; i < 5; i++) {
    await checkWriteStatus()

    if (contentInfo.value?.roomType !== 'EDITING') {
      return
    }

    await sleep(300)
  }
}
</script>

<style scoped>
.content-page {
  display: flex;
  height: 100vh;
  background-color: #f5f5f5;
}

.left-panel {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  background-color: white;
}

.right-panel {
  width: 420px;
  padding: 20px;
  border-left: 1px solid #ddd;
  overflow-y: auto;
  background-color: #fafafa;
}

.empty-box {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #777;
}

.writing-box {
  max-width: 800px;
  margin: 0 auto;
}

.selected-info {
  font-size: 14px;
  color: #666;
}

.story-area {
  margin-top: 20px;
}

.story-block {
  padding: 16px;
  margin-bottom: 16px;
  border-radius: 10px;
  background-color: #f8f8f8;
}

.story-meta {
  margin-bottom: 8px;
  font-size: 13px;
  color: #777;
}

.story-body {
  white-space: pre-wrap;
  line-height: 1.7;
  font-size: 16px;
}

.write-area {
  margin-top: 24px;
  text-align: right;
}

.write-button {
  padding: 12px 20px;
  border: none;
  border-radius: 8px;
  background-color: #222;
  color: white;
  cursor: pointer;
  font-size: 15px;
}

.write-button.disabled {
  background-color: #aaa;
  cursor: not-allowed;
}

.version-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.version-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.version-card.empty {
  visibility: hidden;
  cursor: default;
}

.version-card {
  padding: 10px;
  min-height: 90px;
  border-radius: 10px;
  background-color: white;
  border: 1px solid #ddd;
  cursor: pointer;
}

.version-card:hover {
  background-color: #f0f0f0;
}

.version-card.path {
  border: 3px solid orange;
  background-color: #fff3cd;
}

.version-card.selected {
  border: 4px solid red;
  background-color: #ffe0e0;
}

.version-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
}

.version-time {
  font-size: 12px;
  color: #777;
  margin-bottom: 8px;
}

.version-preview {
  font-size: 14px;
  color: #444;
  line-height: 1.4;
}
</style>