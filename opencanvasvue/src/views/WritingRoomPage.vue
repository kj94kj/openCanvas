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
        <textarea
          v-model="text"
          :readonly="!isEditor"
          class="writing-textarea"
          :placeholder="isEditor ? '이어서 이야기를 써보세요' : '작성자가 이야기를 이어 쓰는 중입니다.'"
          maxlength="2000"
        />

        <div class="editor-footer">
          <span class="char-count">{{ text.length }} / 2,000</span>

          <button class="exit-button" @click="exitWritingRoom">
            {{ isEditor ? '작성종료' : '구경종료' }}
          </button>
        </div>
      </section>
    </section>
  </main>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client/dist/sockjs'
import api from '../api'

const route = useRoute()
const router = useRouter()

const roomId = route.params.roomId
const mode = route.query.mode

const isEditor = computed(() => mode === 'editor')

const chatRoom = history.state.chatRoom
const previousWritings = ref(chatRoom?.writings ?? [])

const connected = ref(false)
const text = ref('')

let stompClient = null
let publishTimer = null

onMounted(() => {
  connectWebSocket()
})

onUnmounted(() => {
  disconnectWebSocket()
})

function connectWebSocket() {
  const token = localStorage.getItem('accessToken')

  if (!token) {
    alert('로그인이 필요합니다.')
    return
  }

  stompClient = new Client({
    webSocketFactory: () => new SockJS('/ws-stomp'),

    connectHeaders: {
      token: `Bearer ${token}`
    },

    onConnect: () => {
      connected.value = true
      console.log('웹소켓 연결 성공')

      stompClient.subscribe(`/sub/chat/room/${roomId}`, (msg) => {
        const body = JSON.parse(msg.body)

        console.log('받은 메시지:', body)

        if (body.type === 'ROOMOUT') {
          if (isEditor.value) {
            return
          }

          alert(body.message || '작성자가 작성을 종료했습니다.')

          disconnectWebSocket()

          router.push(`/content/${route.query.coverId || ''}`)
          return
        }

      if (body.type === 'EDIT' && !isEditor.value) {
        text.value = body.message
        }
      })

      if (isEditor.value) {
        startAutoPublish()
      }
    },

    onStompError: (frame) => {
      console.error('STOMP 에러:', frame)
    },

    onWebSocketClose: () => {
      connected.value = false
      stopAutoPublish()
      console.log('웹소켓 연결 종료')
    }
  })

  stompClient.activate()
}

function startAutoPublish() {
  stopAutoPublish()

  publishTimer = setInterval(() => {
    publishEditMessage()
  }, 1000)
}

function stopAutoPublish() {
  if (publishTimer) {
    clearInterval(publishTimer)
    publishTimer = null
  }
}

function publishEditMessage() {
  if (!stompClient || !connected.value) {
    return
  }

  stompClient.publish({
    destination: '/pub/chat/message',
    body: JSON.stringify({
      type: 'EDIT',
      roomId: roomId,
      message: text.value,
      num: '1'
    })
  })
}

async function exitWritingRoom() {
  try {
    if (isEditor.value) {
      publishEditMessage()
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
  stopAutoPublish()

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
  line-height: 2;
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

.writing-textarea {
  width: 100%;
  min-height: 230px;
  border: none;
  outline: none;
  resize: none;
  background: transparent;
  color: #222;
  font-family: 'Gowun Batang', serif;
  font-size: 17px;
  line-height: 2;
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