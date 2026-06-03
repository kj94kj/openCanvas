<template>
  <div>
    <h1>마이페이지</h1>

    <button @click="getMyInfo">
      내 정보 불러오기
    </button>

    <div v-if="user">
      <p>id: {{ user.id }}</p>
      <p>nickname: {{ user.nickname }}</p>
      <p>email: {{ user.email }}</p>
      <p>role: {{ user.role }}</p>
    </div>

    <hr />

    <h2>WebSocket 테스트</h2>

    <button @click="connectWebSocket" :disabled="connected">
      웹소켓 연결
    </button>

    <button @click="disconnectWebSocket" :disabled="!connected">
      연결 해제
    </button>

    <p>연결 상태: {{ connected ? '연결됨' : '연결 안 됨' }}</p>

    <input
      v-model="message"
      placeholder="메시지 입력"
      @keyup.enter="sendMessage"
    />

    <button @click="sendMessage">
      보내기
    </button>

    <h3>받은 메시지</h3>

    <ul>
      <li v-for="(msg, index) in receivedMessages" :key="index">
        {{ msg }}
      </li>
    </ul>
  </div>
</template>

<script setup>
import { ref, onUnmounted } from 'vue'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client/dist/sockjs'
import api from '../api'

const user = ref(null)

const connected = ref(false)
const message = ref('')
const receivedMessages = ref([])

let stompClient = null

async function getMyInfo() {
  try {
    const response = await api.get('/api/users/')
    user.value = response.data
  } catch (error) {
    console.error(error)
    alert('유저 정보를 불러오지 못했습니다.')
  }
}

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
    
      // pub sub을 지키고 주소가 4개 /로 이루어져야함.
      stompClient.subscribe('/sub/chat/room/1', (msg) => {
        console.log('받은 메시지:', msg.body)
        receivedMessages.value.push(msg.body)
      })
    },

    onStompError: (frame) => {
      console.error('STOMP 에러:', frame)
    },

    onWebSocketClose: () => {
      connected.value = false
      console.log('웹소켓 연결 종료')
    }
  })

  stompClient.activate()
}

function sendMessage() {
  if (!stompClient || !connected.value) {
    alert('웹소켓이 연결되지 않았습니다.')
    return
  }

  // pub sub을 지키고 주소가 4개 /로 이루어져야함.
  // body에 요소들도 다 맞춰줘야 제대로 메시지가 발행됨.
stompClient.publish({
  destination: '/pub/chat/message',
  body: JSON.stringify({
    type: 'EDIT',
    roomId: '1',
    message: message.value,
    num: '1'
  })
})

  message.value = ''
}

function disconnectWebSocket() {
  if (stompClient) {
    stompClient.deactivate()
    stompClient = null
    connected.value = false
  }
}

onUnmounted(() => {
  disconnectWebSocket()
})
</script>