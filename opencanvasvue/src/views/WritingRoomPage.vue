<template>
  <main class="writing-room-page">
    <section class="writing-shell">
      <header class="story-header">
        <h1>{{ chatRoom?.name || '이야기 이어쓰기' }}</h1>
      </header>

      <section v-if="previousWritings.length > 0" class="story-area">
        <article
          v-for="writing in previousWritings"
          :key="writing.writingId"
          class="story-block"
        >
          <p class="story-body">
            {{ writing.body }}
          </p>
        </article>
      </section>

      <section class="editor-paper">
        <div class="paragraph-editor">
          <textarea
            v-for="(paragraph, index) in paragraphs"
            :key="paragraph.paragraphId"
            :ref="el => setTextareaRef(el, index)"
            v-model="paragraph.body"
            :readonly="!isEditor"
            class="writing-textarea"
            :placeholder="getPlaceholder(index)"
            maxlength="2000"
            @input="onParagraphInput(paragraph, $event)"
            @keydown.enter="onEnterParagraph(index, $event)"
          />
        </div>

        <div class="editor-footer">
          <span class="char-count">{{ totalLength }} / 2,000</span>

          <button class="exit-button" @click="exitWritingRoom">
            {{ isEditor ? '작성종료' : '구경종료' }}
          </button>
        </div>
      </section>
    </section>
  </main>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client/dist/sockjs'
import api from '../api'

const route = useRoute()
const router = useRouter()

const roomId = route.params.roomId

const isEditor = ref(false)
const roleReady = ref(false)
const paragraphs = ref([])

const initialChatRoom = window.history.state?.chatRoom ?? null

const chatRoom = ref(initialChatRoom)
const previousWritings = ref(
  initialChatRoom?.writings ?? []
)

const connected = ref(false)

const textareaRefs = ref([])
const paragraphTimers = new Map()

const totalLength = computed(() => {
  return paragraphs.value.reduce((sum, paragraph) => {
    return sum + paragraph.body.length
  }, 0)
})

let stompClient = null

onMounted(() => {
  connectWebSocket()
})

onUnmounted(() => {
  disconnectWebSocket()
})

function createParagraphId() {
  return `p-${Date.now()}-${Math.random().toString(36).slice(2, 10)}`
}

function setTextareaRef(el, index) {
  if (el) {
    textareaRefs.value[index] = el
  }
}

function getPlaceholder(index) {
  if (index !== 0) {
    return ''
  }

  return isEditor.value
    ? '이어서 이야기를 써보세요'
    : ''
}

async function resolveRoomRole() {
  const response = await api.get(
    `/api/rooms/${roomId}/enter`
  )

  const roomData = response.data.chatRoom
  const role = response.data.role

  chatRoom.value = roomData
  previousWritings.value = roomData?.writings ?? []

  isEditor.value = role === 'EDITOR'
  roleReady.value = true

  if (isEditor.value && paragraphs.value.length === 0) {
    paragraphs.value.push({
      paragraphId: createParagraphId(),
      body: '',
      afterParagraphId: null
    })
  }

  await nextTick()
  resizeAllTextareas()
}

function connectWebSocket() {
  const token = localStorage.getItem('accessToken')

  if (!token) {
    alert('로그인이 필요합니다.')
    return
  }

  stompClient = new Client({
    // 로컬일경우와 배포일경우 uri 다름.
    webSocketFactory: () => new SockJS('/ws-stomp'),

    connectHeaders: {
      token: `Bearer ${token}`
    },

    onConnect: async () => {
     connected.value = true
     console.log('웹소켓 연결 성공')

      try {
        await resolveRoomRole()
     } catch (error) {
       console.error('문서방 입장 실패:', error)
        alert(
          error.response?.data ||
          '문서방에 입장하지 못했습니다.'
        )

       disconnectWebSocket()
       return
     }

      if (!stompClient || !connected.value) {
        return
      }

     stompClient.subscribe(
        `/sub/chat/room/${roomId}`,
        (msg) => {
         const body = JSON.parse(msg.body)

         console.log('받은 메시지:', body)

         if (body.type === 'ROOMOUT') {
           if (isEditor.value) {
             return
            }

            alert(
             body.message ||
              '작성자가 작성을 종료했습니다.'
            )

           disconnectWebSocket()

            router.push(
              `/content/${route.query.coverId || ''}`
            )
           return
          }

         if (body.type === 'EDIT' && !isEditor.value) {
           const result = applyParagraphMessage(body)

            resizeAllTextareas()

            if (result === 'created') {
              scrollToParagraph(body.paragraphId)
           }
         }
       }
     )
    },

    onStompError: (frame) => {
      console.error('STOMP 에러:', frame)
    },

    onWebSocketClose: () => {
      connected.value = false
      clearParagraphTimers()
      console.log('웹소켓 연결 종료')
    }
  })

  stompClient.activate()
}

function onParagraphInput(paragraph, event) {
  autoResize(event.target)

  if (!isEditor.value) {
    return
  }

  scheduleParagraphPublish(paragraph)
}

function onEnterParagraph(index, event) {
  if (!isEditor.value) {
    return
  }

  event.preventDefault()

  const currentParagraph = paragraphs.value[index]

  if (!currentParagraph) {
    return
  }

  // 핵심 1:
  // Enter를 누른 현재 문단의 예약 전송을 취소하고,
  // 현재 body 값을 즉시 전송한다.
  flushParagraph(currentParagraph)

  const newParagraph = {
    paragraphId: createParagraphId(),
    body: '',
    afterParagraphId: currentParagraph.paragraphId
  }

  paragraphs.value.splice(index + 1, 0, newParagraph)

  nextTick(() => {
    const nextTextarea = textareaRefs.value[index + 1]

    if (nextTextarea) {
      nextTextarea.focus()
      autoResize(nextTextarea)

      nextTextarea.scrollIntoView({
        behavior: 'smooth',
        block: 'center'
      })
    }
  })
}

function scheduleParagraphPublish(paragraph) {
  const oldTimer = paragraphTimers.get(paragraph.paragraphId)

  if (oldTimer) {
    clearTimeout(oldTimer)
  }

  const timer = setTimeout(() => {
    publishParagraph(paragraph, paragraph.afterParagraphId ?? null)
    paragraphTimers.delete(paragraph.paragraphId)
  }, 700)

  paragraphTimers.set(paragraph.paragraphId, timer)
}

function flushParagraph(paragraph) {
  if (!paragraph) {
    return
  }

  const oldTimer = paragraphTimers.get(paragraph.paragraphId)

  if (oldTimer) {
    clearTimeout(oldTimer)
    paragraphTimers.delete(paragraph.paragraphId)
  }

  publishParagraph(paragraph, paragraph.afterParagraphId ?? null)
}

function publishParagraph(paragraph, afterParagraphId = null) {
  if (!stompClient || !connected.value) {
    return
  }

  const payload = {
    type: 'EDIT',
    roomId,
    paragraphId: paragraph.paragraphId,
    message: paragraph.body
  }

  if (afterParagraphId) {
    payload.afterParagraphId = afterParagraphId
  }

  stompClient.publish({
    destination: '/pub/chat/message',
    body: JSON.stringify(payload)
  })
}

function publishAllParagraphs() {
  paragraphs.value.forEach((paragraph, index) => {
    const afterParagraphId =
      paragraph.afterParagraphId ??
      (index > 0 ? paragraphs.value[index - 1].paragraphId : null)

    const oldTimer = paragraphTimers.get(paragraph.paragraphId)

    if (oldTimer) {
      clearTimeout(oldTimer)
      paragraphTimers.delete(paragraph.paragraphId)
    }

    publishParagraph(paragraph, afterParagraphId)
  })
}

function applyParagraphMessage(body) {
  if (!body.paragraphId) {
    paragraphs.value = [
      {
        paragraphId: 'legacy-paragraph',
        body: body.message ?? '',
        afterParagraphId: null
      }
    ]
    return 'created'
  }

  const existingIndex = paragraphs.value.findIndex((paragraph) => {
    return paragraph.paragraphId === body.paragraphId
  })

  if (existingIndex !== -1) {
    paragraphs.value[existingIndex].body = body.message ?? ''

    if (body.afterParagraphId) {
      paragraphs.value[existingIndex].afterParagraphId = body.afterParagraphId
    }

    return 'updated'
  }

  const newParagraph = {
    paragraphId: body.paragraphId,
    body: body.message ?? '',
    afterParagraphId: body.afterParagraphId ?? null
  }

  const afterIndex = paragraphs.value.findIndex((paragraph) => {
    return paragraph.paragraphId === body.afterParagraphId
  })

  if (afterIndex === -1) {
    paragraphs.value.push(newParagraph)
    return 'created'
  }

  paragraphs.value.splice(afterIndex + 1, 0, newParagraph)
  return 'created'
}

function scrollToParagraph(paragraphId) {
  nextTick(() => {
    const targetIndex = paragraphs.value.findIndex((paragraph) => {
      return paragraph.paragraphId === paragraphId
    })

    const targetTextarea = textareaRefs.value[targetIndex]

    if (targetTextarea) {
      targetTextarea.scrollIntoView({
        behavior: 'smooth',
        block: 'center'
      })
    }
  })
}

function autoResize(textarea) {
  textarea.style.height = 'auto'
  textarea.style.height = `${textarea.scrollHeight}px`
}

function resizeAllTextareas() {
  nextTick(() => {
    textareaRefs.value.forEach((textarea) => {
      if (textarea) {
        autoResize(textarea)
      }
    })
  })
}

function clearParagraphTimers() {
  paragraphTimers.forEach((timer) => {
    clearTimeout(timer)
  })

  paragraphTimers.clear()
}

function sleep(ms) {
  return new Promise((resolve) => {
    setTimeout(resolve, ms)
  })
}

async function exitWritingRoom() {
  try {
    if (isEditor.value) {
      // 핵심 2:
      // 종료 시점에 남아 있는 예약 전송을 취소만 하지 않고,
      // 현재 paragraphs 전체를 즉시 전송한다.
      publishAllParagraphs()

      // WebSocket publish 직후 바로 exit API를 때리면
      // 서버에서 exit이 먼저 처리될 수 있으므로 아주 짧게 양보한다.
      // 완전한 정석은 exit API body로 paragraphs 전체를 보내는 방식이다.
      await sleep(300)
    }

    await api.post('/api/rooms/exit', null, {
      params: {
        roomId
      }
    })

    disconnectWebSocket()

    router.push(`/content/${route.query.coverId || ''}`)
  } catch (error) {
    console.error(error)
    alert(error.response?.data || '문서방 나가기에 실패했습니다.')
  }
}

function disconnectWebSocket() {
  clearParagraphTimers()

  if (stompClient) {
    stompClient.deactivate()
    stompClient = null
  }

  connected.value = false
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Gowun+Batang:wght@400;700&display=swap');

.writing-room-page {
  min-height: 100vh;
  padding: 52px 24px;
  background:
    radial-gradient(circle at top, rgba(255, 255, 255, 1) 0%, rgba(248, 248, 248, 1) 58%, rgba(244, 244, 244, 1) 100%);
}

.writing-shell {
  width: min(960px, 100%);
  margin: 0 auto;
  padding: 42px 54px 36px;
  background: rgba(255, 255, 255, 0.88);
  box-shadow: 0 18px 44px rgba(0, 0, 0, 0.04);
}

.story-header {
  margin-bottom: 28px;
  text-align: center;
}

.story-header h1 {
  margin: 0;
  color: #1f2933;
  font-family: 'Gowun Batang', serif;
  font-size: 36px;
  font-weight: 700;
  letter-spacing: 0;
}

.story-area {
  display: flex;
  flex-direction: column;
  gap: 14px;
  margin-bottom: 24px;
}

.story-block {
  padding: 20px 26px;
  border: 1px solid #e7e7e7;
  border-radius: 8px;
  background: #fff;
}

.story-body {
  margin: 0;
  color: #2f2f2f;
  font-family: 'Gowun Batang', serif;
  font-size: 17px;
  line-height: 1.8;
  white-space: pre-wrap;
}

.editor-paper {
  min-height: 300px;
  padding: 24px 26px 20px;
  border: 1px solid #e4e4e4;
  border-radius: 4px;
  background: #fff;
  box-shadow: 0 14px 30px rgba(0, 0, 0, 0.08);
}

.paragraph-editor {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.writing-textarea {
  width: 100%;
  min-height: 1.75em;
  padding: 0;
  margin: 0;
  overflow: hidden;
  border: none;
  outline: none;
  resize: none;
  background: transparent;
  color: #222;
  font-family: 'Gowun Batang', serif;
  font-size: 17px;
  line-height: 1.75;
  box-sizing: border-box;
}

.writing-textarea::placeholder {
  color: #9ca3af;
}

.writing-textarea:read-only {
  color: #555;
}

.editor-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-top: 18px;
}

.char-count {
  color: #8b8b8b;
  font-size: 14px;
}

.exit-button {
  min-width: 124px;
  height: 46px;
  border: none;
  border-radius: 6px;
  background: #ff5f52;
  color: #fff;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
}

.exit-button:hover {
  background: #f24f43;
}

@media (max-width: 768px) {
  .writing-room-page {
    padding: 24px 14px;
  }

  .writing-shell {
    padding: 28px 18px 24px;
  }

  .story-header h1 {
    font-size: 26px;
  }

  .story-block {
    padding: 18px;
  }

  .editor-footer {
    align-items: stretch;
    flex-direction: column;
  }

  .exit-button {
    width: 100%;
  }
}
</style>