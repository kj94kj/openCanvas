<template>
  <div class="story-area" v-if="previousWritings.length > 0">
    <div
      v-for="writing in previousWritings"
      :key="writing.writingId"
      class="story-block"
    >
      <div class="story-meta">
        {{ writing.depth }}-{{ writing.siblingIndex }}
       / {{ writing.username }}
      </div>

     <p class="story-body">
        {{ writing.body }}
      </p>
    </div>
  </div>

  <div class="writing-room-page">
    <h1>문서방</h1>

    <p>roomId: {{ roomId }}</p>
    <p>모드: {{ isEditor ? '작성자' : '관전자' }}</p>
    <p>연결 상태: {{ connected ? '연결됨' : '연결 안 됨' }}</p>

    <textarea
      v-model="text"
      :readonly="!isEditor"
      class="writing-textarea"
      placeholder="내용을 입력하세요."
    />

    <div class="button-area">
      <button @click="connectWebSocket" :disabled="connected">
        연결
      </button>

      <button @click="disconnectWebSocket" :disabled="!connected">
        연결 해제
      </button>

      <button @click="exitWritingRoom">
        {{ isEditor ? '작성 완료' : '나가기' }}
      </button>
    </div>
  </div>
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
    webSocketFactory: () => new SockJS('http://localhost:8080/ws-stomp'),

    connectHeaders: {
      token: `Bearer ${token}`
    },

    onConnect: () => {
      connected.value = true
      console.log('웹소켓 연결 성공')

      stompClient.subscribe(`/sub/chat/room/${roomId}`, (msg) => {
        const body = JSON.parse(msg.body)

        console.log('받은 메시지:', body)

        /**
         * 관전자만 서버에서 받은 내용으로 textarea를 갱신한다.
         * 작성자는 자기 textarea를 서버 메시지로 덮어쓰면 안 됨.
         */
        if (!isEditor.value) {
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
.writing-room-page {
  padding: 24px;
}

.writing-textarea {
  width: 100%;
  height: 400px;
  margin-top: 16px;
  padding: 16px;
  font-size: 16px;
  resize: vertical;
}

.button-area {
  margin-top: 16px;
  display: flex;
  gap: 8px;
}
</style>