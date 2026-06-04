<!--
  模块名称: 404页面
  功能描述: 当访问不存在的路由时显示的404错误页面。
-->
<template>
  <div class="not-found">
    <h1>404</h1>
    <p>页面未找到</p>
    <button @click="$router.push('/')">返回首页</button>
  </div>
</template>

<style scoped>
.not-found {
  height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
  color: #2c3e50;
}

h1 {
  font-size: 6rem;
  margin: 0;
  color: #3498db;
}

p {
  font-size: 1.5rem;
  margin-bottom: 20px;
}

button {
  padding: 10px 20px;
  background-color: #3498db;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1rem;
}
</style>
