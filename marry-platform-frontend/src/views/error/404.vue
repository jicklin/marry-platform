<template>
  <div class="not-found">
    <div class="big-text">404</div>
    <div class="sub">您访问的页面不存在</div>
    <NButton type="primary" @click="$router.push('/')">返回首页</NButton>
  </div>
</template>

<script setup lang="ts"></script>

<style scoped>
.not-found {
  height: 70vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.big-text {
  font-size: 96px;
  font-weight: 700;
  color: var(--n-primary-color, #2d8cf0);
}
.sub {
  color: var(--fg-muted, #909399);
  margin-bottom: 16px;
}
</style>