<template>
  <div>
    <nav>
      <RouterLink to="/">홈</RouterLink> |
      <RouterLink to="/mypage/info">내정보</RouterLink>
      <RouterLink to="/mypage/writings">내가 쓴글</RouterLink>
      <RouterLink to="/mypage/likes">좋아요 누른글</RouterLink>
      <RouterLink to="/mypagetest">웹소켓 테스트</RouterLink>
      <RouterLink to="/mainpage">메인페이지</RouterLink>
    </nav>

    <hr />

    <RouterView />
  </div>
</template>