import { createRouter, createWebHistory } from 'vue-router'

import HomeView from './views/HomeView.vue'
import OAuthCallbackView from './views/OAuthCallbackView.vue'
import MyWritingPage from './views/MyWritingPage.vue'
import MyLikedPage from './views/MyLikedPage.vue'
import MainPage from './views/MainPage.vue'
import ContentPage from './views/ContentPage.vue'
import WritingRoomPage from './views/WritingRoomPage.vue'
import CoverCreatePage from './views/CoverCreatePage.vue'

const routes = [
  {
    path: '/',
    component: HomeView
  },
  {
    path: '/oauth2/callback',
    component: OAuthCallbackView
  },
  {
    path: '/mypage/writings',
    component: MyWritingPage
  },
  {
    path: '/mypage/likes',
    component: MyLikedPage
  },
  {
    path: '/mainpage',
    component: MainPage
  },
  {
    path: '/content/:coverId',
    component: ContentPage
  },
  {
    path: '/writing-room/:roomId',
    component: WritingRoomPage
  },
  {
    path: '/cover-create',
    component: CoverCreatePage
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router