<template>
  <div class="content-page">
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

        <!-- 글쓰기 버튼 -->
        <div class="write-area">
          <button
            v-if="writeStatus === 'WRITABLE'"
            class="write-button"
            @click="startWriting"
          >
            글쓰기
          </button>

          <button
            v-else-if="writeStatus === 'WRITING'"
            class="write-button disabled"
            disabled
          >
            다른 사람이 작성중
          </button>

          <button
            v-else
            class="write-button disabled"
            disabled
          >
            불가능
          </button>
        </div>
      </div>
    </section>

    <!-- 오른쪽: 버전 목록 -->
    <aside class="right-panel">
      <h3>버전 목록</h3>

        <div class="version-grid">
          <div
            v-for="writing in writings"
            :key="writing.writingId"
            class="version-card"
            :class="{ selected: selectedWriting?.writingId === writing.writingId }"
            @click="selectWriting(writing)"
          >
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
        </div>
      </div>
    </aside>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'
import { useRoute } from 'vue-router'

const route = useRoute()

const contentInfo = ref(null)
const writings = ref([])
const selectedWriting = ref(null)

// 나중에 백엔드에서 상태 받아오면 이 값만 바꾸면 됨.
// WRITABLE: 글쓰기 가능
// WRITING: 다른 사람이 작성중
// DISABLED: 불가능
const writeStatus = ref('WRITABLE')

// 예시: /content/:coverId 로 들어온다고 가정
const coverId = route.params.coverId

onMounted(() => {
  fetchContent()
})

async function fetchContent() {
  try {
    const response = await axios.get(`/api/content/${coverId}`)

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

  checkWriteStatus(writing)
}

async function fetchParents(writing) {
  try {
      //프론트에서는 curDepth, curSiblingIndex, title이 필요해서 최소한의 dto를 만듦.
    const requestDto = {
      depth: writing.depth,
      siblingIndex: writing.siblingIndex,
      title: contentInfo.value.title
    }

    const response = await axios.post('/api/writings/parents', requestDto)

    selectedPath.value = response.data ?? []
  } catch (error) {
    console.error('부모 글 조회 실패', error)
    selectedPath.value = []
  }
}

async function checkWriteStatus(writing) {
  const response = await axios.get(`/api/covers/${coverId}/room-type`)
  const roomType = response.data
  writeStatus.value = roomType
}

function makePreview(body) {
  if (!body) {
    return ''
  }

  if (body.length <= 50) {
    return body
  }

  return body.substring(0, 50) + '...'
}

function formatDate(time) {
  if (!time) {
    return ''
  }

  return new Date(time).toLocaleString()
}

function startWriting() {
  if (!selectedWriting.value) {
    alert('버전을 먼저 선택해주세요.')
    return
  }

  if (writeStatus.value !== 'WRITABLE') {
    return
  }

  /*
    여기서 웹소켓 연결 작업 시작하면 됨.

    필요한 값:
    - contentId
    - parentWritingId
    - depth
    - siblingIndex
    - roomId

    selectedWriting 기준으로 이어쓰기 시작.
  */

  const writingStartRequest = {
    contentId: selectedWriting.value.contentId,
    parentWritingId: selectedWriting.value.writingId,
    parentDepth: selectedWriting.value.depth,
    parentSiblingIndex: selectedWriting.value.siblingIndex,
    roomId: contentInfo.value.roomId
  }

  console.log('글쓰기 시작 요청', writingStartRequest)

  // 예시
  // connectWebSocket(writingStartRequest)
}

const canWrite = computed(() => {
  if (!contentInfo.value) return false

  if (contentInfo.value.roomType === 'FINISHED') return false

  if (contentInfo.value.roomType === 'EDITING') return false

  // 아직 글이 하나도 없으면 첫 글 작성 가능
  if (writings.value.length === 0) return true

  // 글이 있는데 선택한 글이 없으면 작성 불가
  if (!selectedWriting.value) return false

  // 선택한 글의 자식 개수 확인
  const childCount = writings.value.filter(w =>
    w.depth === selectedWriting.value.depth + 1
  ).length

  return childCount < 2
})
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
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
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

.version-card.selected {
  border: 2px solid #222;
  background-color: #eeeeee;
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