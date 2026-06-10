import { createRouter, createWebHistory } from 'vue-router'

import HomeView from './views/HomeView.vue'
import OAuthCallbackView from './views/OAuthCallbackView.vue'
import MyPageTest from './views/MyPageTest.vue'
import MyInfoPage from './views/MyInfoPage.vue'
import MyWritingPage from './views/MyWritingPage.vue'
import MyLikedPage from './views/MyLikedPage.vue'
import MainPage from './views/MainPage.vue'

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
    path: '/mypagetest',
    component: MyPageTest
  },
    {
    path: '/mypage/info',
    component: MyInfoPage
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
  name: 'ContentPage',
  component: ContentPage
  },
  {
    path: '/writing-room/:roomId',
    component: WritingRoomPage
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router