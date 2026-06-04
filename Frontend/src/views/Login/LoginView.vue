<template>
  <!-- 登录页面根容器：包含背景特效和登录框 -->
  <div 
    class="login-wrapper"
    :class="{ 'warp-active': isLaunching }"
  >
    
    <!-- 窗口拖拽区域 -->
    <div class="window-drag-region"></div>

    <!-- 全局 Toast 提示组件 -->
    <div class="toast-notification" :class="{ 'show': toast.visible }">
      <div class="toast-content">
        <span class="toast-icon">✨</span>
        <span>{{ toast.message }}</span>
      </div>
    </div>
    <!-- 深空背景 Canvas：用于绘制星空穿梭效果 -->
    <canvas ref="canvasRef" class="deep-space-canvas"></canvas>

    <!-- 大气层与星球背景层：包含静态星星、雨滴和行星装饰 -->
    <div class="atmospheric-layer" :class="{ 'launch-mode': isLaunching }">
      <div class="deep-space-tint"></div>
      <div class="nebula-cloud"></div>
      
      <!-- 静态背景星星 -->
      <div class="star-field">
        <div 
          v-for="(style, index) in staticStars" 
          :key="`star-${index}`" 
          class="star" 
          :style="style"
        ></div>
      </div>

      <!-- 数字雨特效层 -->
      <div class="digital-rain-layer">
        <div 
          v-for="(style, index) in staticRainDrops" 
          :key="`rain-${index}`" 
          class="rain-drop" 
          :style="style"
        ></div>
      </div>

      <!-- 装饰性星球元素 -->
      <div class="cosmic-planet main-planet"><div class="planet-ring"></div></div>
      <div class="cosmic-planet planet-red-giant"></div>
      <div class="cosmic-planet planet-toxic-green"><div class="gas-swirl"></div></div>
      <div class="cosmic-planet planet-molten-gold"></div>
      <div class="cosmic-planet planet-amethyst"></div>
      <div class="cosmic-planet planet-moon-grey"><div class="craters"></div></div>
      <div class="cosmic-planet planet-rock-debris"></div>
      <div class="cosmic-planet planet-ice-world"></div>
      <div class="cosmic-planet planet-dwarf-red"></div>

      <div class="overlay-effects"></div>
    </div>

    <!-- 登录/注册主容器 -->
    <div 
      class="container" 
      id="container" 
      :class="{ 
        'right-panel-active': isRightPanelActive, // 控制面板切换状态
        'shaking': isLaunching // 控制发射时的震动效果
      }"
    >
      <!-- 注册表单区域 -->
      <div class="form-container sign-up-container">
        <form @submit.prevent="handleRegister">
          <h1>注册</h1>
          <span>✨✨✨</span>
          <input type="text" placeholder="员工姓名 / 用户名" v-model="registerForm.username" required />
          <input type="email" placeholder="工作邮箱" v-model="registerForm.email" />
          <input type="tel" placeholder="手机号码" v-model="registerForm.phone" />
          <input type="password" placeholder="设置登录密码" v-model="registerForm.password" required />
          <input type="password" placeholder="确认登录密码" v-model="registerForm.confirmPassword" required />
          <p v-if="registerError" class="error-message">{{ registerError }}</p>
          <button type="submit" :disabled="isLoading">
             {{ isLoading ? '正在提交...' : '立即注册' }}
          </button>
        </form>
      </div>

      <!-- 登录表单区域 -->
      <div class="form-container sign-in-container">
        <form @submit.prevent="handleLogin">
          <h1>登录</h1>
          <span>🎉🎉🎉</span>
          <input type="text" placeholder="账号 / 手机号" v-model="loginForm.username" required />
          <input type="password" placeholder="登录密码" v-model="loginForm.password" required />
          <p v-if="loginError" class="error-message">{{ loginError }}</p>
          <button type="submit" :disabled="isLoading">
            {{ isLoading ? '正在验证...' : '登录系统' }}
          </button>
        </form>
      </div>

      <!-- 切换遮罩层：包含"去登录"和"去注册"的切换按钮 -->
      <div class="overlay-container">
        <div class="overlay">
          <div class="overlay-panel overlay-left">
            <h1>欢迎回来</h1>
            <p>保持库存数据实时同步，请登录您的账号</p>
            <button class="ghost" @click="isRightPanelActive = false">去登录</button>
          </div>
          <div class="overlay-panel overlay-right">
            <h1>快速注册</h1>
            <p>注册账号以获取数据访问权限</p>
            <button class="ghost" @click="isRightPanelActive = true">去注册</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
// --- 导入依赖 ---
import axios from '@/api/request';
import { useUserStore } from '@/stores/user';
import { onMounted, onUnmounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';

// --- 核心状态管理 ---
const router = useRouter();
const userStore = useUserStore();
const isRightPanelActive = ref(false); // 控制登录/注册面板切换 (false: 登录, true: 注册)
const isLoading = ref(false); // 加载状态
const isLaunching = ref(false); // 是否正在执行穿梭动画

// --- Toast 消息通知逻辑 ---
const toast = reactive({ visible: false, message: '' });

const showToast = (message) => {
  toast.message = message;
  toast.visible = true;
  // 3秒后自动消失
  setTimeout(() => {
    toast.visible = false;
  }, 3000);
};
// -------------------------

// --- 表单数据与验证状态 ---
const loginForm = reactive({ username: '', password: '' });
const loginError = ref('');
const registerForm = reactive({ username: '', email: '', phone: '', password: '', confirmPassword: '' });
const registerError = ref('');

// --- 背景动画效果逻辑 (雨滴与星星) ---
const staticRainDrops = ref([]);
const staticStars = ref([]);
const random = (min, max) => Math.random() * (max - min) + min;

// 生成雨滴样式：随机位置、长度、颜色和下落速度
const generateRainStyle = (index) => {
  const colors = [
    'linear-gradient(to bottom, transparent, rgba(0, 255, 136, 0.8))', 
    'linear-gradient(to bottom, transparent, rgba(0, 200, 255, 0.8))', 
    'linear-gradient(to bottom, transparent, rgba(180, 100, 255, 0.8))',
    'linear-gradient(to bottom, transparent, rgba(255, 255, 255, 0.6))'
  ];
  const width = random(1, 2);
  const height = random(150, 400);
  const left = random(0, 100);
  const duration = random(1.5, 4);
  const delay = random(0, 5);
  return {
    left: `${left}%`,
    width: `${width}px`,
    height: `${height}px`,
    background: colors[index % colors.length],
    animationDuration: `${duration}s`,
    animationDelay: `${delay}s`,
    opacity: random(0.3, 0.8)
  };
};

// 生成背景星星样式：随机大小、位置和闪烁动画
const generateStarStyle = () => {
  const size = random(0.5, 3); 
  return {
    width: `${size}px`,
    height: `${size}px`,
    left: `${random(0, 100)}%`,
    top: `${random(0, 100)}%`,
    opacity: random(0.3, 1),
    animationDelay: `${random(0, 5)}s`,
    animationDuration: `${random(2, 6)}s`
  }
};

for (let i = 0; i < 80; i++) {
  staticRainDrops.value.push(generateRainStyle(i));
}
for (let i = 0; i < 200; i++) {
  staticStars.value.push(generateStarStyle());
}

// --- Canvas 粒子引擎 (星空穿梭效果) ---
const canvasRef = ref(null);
let engine = null;

// 星星粒子类：管理单个星星的状态和绘制
class Star {
  constructor(w, h) { this.reset(w, h, true); }
  // 重置星星位置：当星星飞出屏幕或初始化时调用
  reset(w, h, randomZ = false) {
    this.x = (Math.random() * w - w / 2) * (window.innerWidth / 100);
    this.y = (Math.random() * h - h / 2) * (window.innerHeight / 100); 
    this.z = randomZ ? Math.random() * 2000 : 2000; 
    this.pz = this.z;
    const colors = [[0,200,255], [255,100,100], [100,255,100], [255,255,255]];
    this.color = colors[Math.floor(Math.random() * colors.length)];
  }
  // 更新星星位置：根据速度减小z值，模拟向屏幕飞来的效果
  update(w, h, speed) {
    this.pz = this.z;
    this.z -= speed;
    if (this.z < 1) { this.reset(w, h); this.z = 2000; this.pz = 2000; }
  }
  // 绘制星星：计算透视投影坐标并绘制线条
  draw(ctx, w, h, speed) {
    const x2d = this.x / this.z * 100 + w / 2;
    const y2d = this.y / this.z * 100 + h / 2;
    let tailFactor = speed > 10 ? speed * 1.5 : 1;
    const px2d = this.x / (this.z + tailFactor) * 100 + w / 2;
    const py2d = this.y / (this.z + tailFactor) * 100 + h / 2;
    if (x2d < 0 || x2d > w || y2d < 0 || y2d > h) return;
    const opacity = (1 - this.z / 2000);
    const starSize = speed > 10 ? 1 : (1 - this.z / 2000) * 3; 
    ctx.beginPath();
    const gradient = ctx.createLinearGradient(px2d, py2d, x2d, y2d);
    gradient.addColorStop(0, `rgba(${this.color[0]}, ${this.color[1]}, ${this.color[2]}, 0)`);
    gradient.addColorStop(1, `rgba(${this.color[0]}, ${this.color[1]}, ${this.color[2]}, ${opacity})`);
    ctx.strokeStyle = gradient;
    ctx.lineWidth = starSize;
    ctx.lineCap = 'round';
    ctx.moveTo(px2d, py2d);
    ctx.lineTo(x2d, y2d);
    ctx.stroke();
  }
}

// 动画引擎类：管理Canvas渲染循环
class Engine {
  constructor(canvas) {
    this.canvas = canvas;
    this.ctx = canvas.getContext('2d');
    this.width = window.innerWidth;
    this.height = window.innerHeight;
    this.stars = [];
    this.baseSpeed = 0.5; 
    this.targetSpeed = 0.5; 
    this.currentSpeed = 0.5;
    this.init();
    this.animate();
  }
  // 初始化画布尺寸和星星数组
  init() {
    this.resize();
    window.addEventListener('resize', () => this.resize());
    for (let i = 0; i < 800; i++) this.stars.push(new Star(this.width, this.height));
  }
  // 响应窗口大小调整
  resize() {
    this.width = window.innerWidth;
    this.height = window.innerHeight;
    this.canvas.width = this.width;
    this.canvas.height = this.height;
  }
  // 动画循环：清除画布、更新并绘制所有星星
  animate() {
    this.currentSpeed += (this.targetSpeed - this.currentSpeed) * 0.05;
    this.ctx.fillStyle = `rgba(0, 0, 0, ${this.currentSpeed > 10 ? 0.1 : 0.8})`; 
    this.ctx.fillRect(0, 0, this.width, this.height);
    this.stars.forEach(star => {
      star.update(this.width, this.height, this.currentSpeed);
      star.draw(this.ctx, this.width, this.height, this.currentSpeed);
    });
    this.animationFrame = requestAnimationFrame(() => this.animate());
  }
  // 销毁实例：清理事件监听和动画帧
  destroy() {
    cancelAnimationFrame(this.animationFrame);
    window.removeEventListener('resize', this.resize);
  }
}

// --- 启动/停止 穿梭动画序列 ---
const startWarpSequence = async () => {
  isLaunching.value = true;
  if (engine) engine.targetSpeed = 120; 
  isLoading.value = true;
  await new Promise(r => setTimeout(r, 2000));
};

const abortWarpSequence = () => {
  isLaunching.value = false;
  if (engine) engine.targetSpeed = 0.5;
  isLoading.value = false;
};

// --- 登录处理逻辑 ---
const handleLogin = async () => {
  loginError.value = '';
  await startWarpSequence();
  try {
    const data = await axios.post('/auth/login', loginForm);
    userStore.setToken(data.token);
    userStore.setUser(data);
    if (data.role === 'ADMIN') {
      router.push('/admin/dashboard');
    } else {
      router.push('/dashboard');
    }
  } catch (err) {
    console.error(err);
    let msg = err.message || '登录失败，请检查账号密码';
    if (msg.includes('Invalid username or password') || msg.includes('Bad credentials')) {
      msg = '账号或密码错误';
    }
    loginError.value = msg;
    abortWarpSequence();
  }
};

const handleRegister = async () => {
  registerError.value = '';
  if (registerForm.password !== registerForm.confirmPassword) {
    registerError.value = '两次输入的密码不一致';
    return;
  }
  await startWarpSequence();
  try {
    await axios.post('/auth/register', { ...registerForm, role: 'USER' });
    
    // === 优化：替换 alert 为自定义 Toast ===
    showToast('账号注册成功，请登录'); 
    // ===================================

    abortWarpSequence();
    isRightPanelActive.value = false;
  } catch (err) {
    let msg = err.message || '注册失败';
    if (msg.includes('该数据已存在') || msg.includes('Duplicate entry')) {
      msg = '该账号已被注册，请直接登录';
    }
    registerError.value = msg;
    abortWarpSequence();
  }
};

onMounted(() => {
  if (canvasRef.value) engine = new Engine(canvasRef.value);
});
onUnmounted(() => {
  if (engine) engine.destroy();
});
</script>

<style scoped>
* { box-sizing: border-box; }
/* 窗口拖拽区域 */
.window-drag-region {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 40px;
  z-index: 9999;
  -webkit-app-region: drag;
}

.login-wrapper {
  display: flex; justify-content: center; align-items: center;
  height: 100vh; width: 100%; position: relative; overflow: hidden;
  background-color: #000;
  font-family: 'Segoe UI', 'Microsoft YaHei', sans-serif;
}

/* === 新增：Toast 提示样式 (不影响其他布局) === */
.toast-notification {
  position: fixed;
  top: 30px;
  left: 50%;
  transform: translateX(-50%) translateY(-100px); /* 默认隐藏在上方 */
  background: rgba(16, 20, 36, 0.85);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(79, 172, 254, 0.5);
  box-shadow: 0 0 20px rgba(79, 172, 254, 0.2), inset 0 0 10px rgba(79, 172, 254, 0.1);
  padding: 12px 35px;
  border-radius: 50px;
  color: #fff;
  z-index: 9999;
  transition: all 0.5s cubic-bezier(0.68, -0.55, 0.265, 1.55);
  opacity: 0;
  pointer-events: none;
}

.toast-notification.show {
  transform: translateX(-50%) translateY(0);
  opacity: 1;
}

.toast-content {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  letter-spacing: 1px;
}

.toast-icon {
  font-size: 16px;
}
/* =========================================== */

/* === 背景视觉效果 (Canvas, 大气层, 星球) === */
/* Canvas, Atmosphere, Planets Styles */
.deep-space-canvas { position: absolute; top: 0; left: 0; z-index: 0; opacity: 0.6; transition: opacity 1s; }
.launch-mode ~ .deep-space-canvas { opacity: 1; }
.atmospheric-layer { position: absolute; top: 0; left: 0; width: 100%; height: 100%; z-index: 1; pointer-events: none; transform-style: preserve-3d; perspective: 1200px; }
.atmospheric-layer > div { transition: all 1s ease-in-out; }
.deep-space-tint { position: absolute; width: 100%; height: 100%; background: radial-gradient(circle at center, #1b2735 0%, #090a0f 60%, #000 100%); opacity: 0.8; }
.nebula-cloud { position: absolute; top: -50%; left: -50%; width: 200%; height: 200%; background: radial-gradient(circle at 80% 20%, rgba(255, 0, 100, 0.1), transparent 30%), radial-gradient(circle at 20% 80%, rgba(0, 255, 100, 0.1), transparent 30%), radial-gradient(circle at 50% 50%, rgba(76, 29, 149, 0.2), transparent 50%); filter: blur(50px); animation: nebulaPulse 20s infinite alternate; }
/* 星云脉冲动画 */
@keyframes nebulaPulse { 0% { opacity: 0.5; } 100% { opacity: 0.8; } }
.star-field { position: absolute; width: 100%; height: 100%; }
.star { position: absolute; background: white; border-radius: 50%; box-shadow: 0 0 4px #fff; animation: twinkle 3s infinite alternate; }
/* 星星闪烁动画 */
@keyframes twinkle { 0% { opacity: 0.3; transform: scale(0.8); } 100% { opacity: 1; transform: scale(1.2); } }
.digital-rain-layer { position: absolute; top: 0; left: 0; width: 100%; height: 100%; z-index: 2; transition: opacity 0.3s ease-out; }
.rain-drop { position: absolute; top: -500px; box-shadow: 0 0 10px rgba(255,255,255,0.3); animation: rainFall linear infinite; will-change: transform; }
/* 数字雨下落动画 */
@keyframes rainFall { 0% { transform: translateY(0); opacity: 0; } 10% { opacity: 1; } 90% { opacity: 1; } 100% { transform: translateY(150vh); opacity: 0; } }
.launch-mode .rain-drop { opacity: 0 !important; }
.launch-mode .digital-rain-layer { opacity: 0; }
.cosmic-planet { position: absolute; border-radius: 50%; z-index: 2; box-shadow: 0 0 30px rgba(0,0,0,0.5); }
.main-planet { bottom: -120px; right: -120px; width: 450px; height: 450px; background: radial-gradient(circle at 30% 30%, #5d46e8, #000); box-shadow: inset -40px -40px 100px rgba(0,0,0,0.9), 0 0 60px rgba(81, 45, 168, 0.4); }
.planet-ring { position: absolute; top: 50%; left: 50%; width: 600px; height: 140px; border-radius: 50%; border: 4px solid rgba(100, 200, 255, 0.1); border-top: 6px solid rgba(100, 200, 255, 0.6); transform: translate(-50%, -50%) rotate(-35deg); }
.planet-red-giant { top: -80px; left: -80px; width: 300px; height: 300px; background: radial-gradient(circle at 70% 70%, #ff4d4d, #330000); box-shadow: inset 10px 10px 60px rgba(0,0,0,0.8), 0 0 50px rgba(255, 50, 50, 0.3); }
.planet-toxic-green { bottom: 15%; left: 5%; width: 120px; height: 120px; background: radial-gradient(circle at 40% 40%, #00ff88, #003311); box-shadow: 0 0 30px rgba(0, 255, 136, 0.4); overflow: hidden; }
.gas-swirl { position: absolute; width: 100%; height: 100%; background: repeating-linear-gradient(45deg, transparent, transparent 10px, rgba(0,0,0,0.2) 10px, rgba(0,0,0,0.2) 20px); opacity: 0.5; }
.planet-molten-gold { top: 20%; left: 45%; width: 40px; height: 40px; background: #ffd700; box-shadow: 0 0 25px #ffaa00; z-index: 1; }
.planet-amethyst { top: 40%; right: 10%; width: 90px; height: 90px; background: linear-gradient(135deg, #e0aaff, #3c096c); box-shadow: inset -5px -5px 10px rgba(0,0,0,0.5); }
.planet-moon-grey { bottom: 250px; right: 250px; width: 60px; height: 60px; background: #d3d3d3; box-shadow: inset -10px -10px 20px rgba(0,0,0,0.8); }
.craters { position: absolute; top: 20%; left: 30%; width: 10px; height: 10px; background: rgba(0,0,0,0.3); border-radius: 50%; box-shadow: 20px 10px 0 -2px rgba(0,0,0,0.3); }
.planet-rock-debris { top: 60%; left: -20px; width: 50px; height: 40px; background: #555; border-radius: 40% 60% 70% 30%; transform: rotate(15deg); filter: blur(2px); }
.planet-ice-world { top: 10%; right: 25%; width: 70px; height: 70px; background: radial-gradient(circle at 30% 30%, #e0f7fa, #006064); box-shadow: 0 0 20px rgba(0, 255, 255, 0.3); }
.planet-dwarf-red { bottom: -20px; left: 30%; width: 180px; height: 180px; background: radial-gradient(#b71c1c, transparent 70%); opacity: 0.6; filter: blur(20px); z-index: 5; }
.overlay-effects { position: absolute; width: 100%; height: 100%; background: radial-gradient(circle at center, transparent 30%, rgba(0,0,0,0.6) 100%); pointer-events: none; z-index: 5; }
.launch-mode .deep-space-tint { opacity: 0; transition: 0.5s; }
.launch-mode .nebula-cloud { opacity: 0; transform: scale(3); transition: 1.5s; }
.launch-mode .star-field { opacity: 0; transform: scale(5); transition: 1s; }
.launch-mode .main-planet { transform: translate(300px, 300px) scale(3); opacity: 0; transition: 1.2s cubic-bezier(0.5,0,0.5,1); }
.launch-mode .planet-red-giant { transform: translate(-300px, -300px) scale(3); opacity: 0; transition: 1.4s; }
.launch-mode .planet-toxic-green { transform: translate(-200px, 200px) scale(0.5); opacity: 0; transition: 0.8s; }
.launch-mode .planet-molten-gold { transform: scale(30); opacity: 0; transition: 0.6s ease-in; }
.launch-mode .planet-amethyst { transform: translate(200px, 0) scale(0.8); opacity: 0; transition: 1s; }
.launch-mode .planet-moon-grey { transform: translate(320px, 320px) scale(3); opacity: 0; transition: 1.2s; }
.launch-mode .planet-rock-debris { transform: translate(-200px, 50px) scale(2); opacity: 0; transition: 0.5s; }
.launch-mode .planet-ice-world { transform: translate(150px, -150px) scale(0.1); opacity: 0; transition: 1.5s; }
.launch-mode .planet-dwarf-red { transform: translateY(200px) scale(2); opacity: 0; transition: 1s; }

/* === UI 容器样式 (登录框主体) === */
.container {
  background: rgba(16, 20, 36, 0.65);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  box-shadow: 0 20px 50px rgba(0,0,0,0.6), 0 0 30px rgba(0, 150, 255, 0.15);
  position: relative; overflow: hidden;
  width: 800px; max-width: 95%; 
  min-height: 550px; 
  z-index: 10; transition: transform 0.2s;
}
.container.shaking {
  animation: warpShake 0.1s infinite;
  box-shadow: 0 0 80px rgba(0, 242, 254, 0.5), inset 0 0 20px rgba(0, 242, 254, 0.3);
  border-color: rgba(0, 242, 254, 0.8);
}
/* 穿梭时的震动动画 */
@keyframes warpShake {
  0% { transform: translate(1px, 1px) scale(1.00); }
  50% { transform: translate(-1px, -1px) scale(0.995); }
  100% { transform: translate(1px, -1px) scale(1.00); }
}

/* === 通用排版与表单控件样式 === */
h1 { font-weight: bold; margin: 0; background: linear-gradient(to right, #fff, #a1c4fd); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
p, span { color: #8a9bbd; }
input { background-color: rgba(255, 255, 255, 0.05); border: 1px solid rgba(255, 255, 255, 0.1); color: #fff; padding: 12px 15px; margin: 8px 0; width: 100%; border-radius: 4px; outline: none; transition: 0.3s; }
input:focus { background-color: rgba(255, 255, 255, 0.1); border-color: #4facfe; box-shadow: 0 0 10px rgba(79, 172, 254, 0.3); }
button { border-radius: 20px; border: none; background: linear-gradient(45deg, #30cfd0, #330867); color: #FFFFFF; font-size: 12px; font-weight: bold; padding: 12px 45px; letter-spacing: 1px; text-transform: uppercase; transition: 0.3s; cursor: pointer; margin-top: 15px; }
button:hover { background: linear-gradient(45deg, #4facfe, #00f2fe); box-shadow: 0 5px 15px rgba(0, 242, 254, 0.4); }
button:disabled { filter: grayscale(1); cursor: wait; }
button.ghost { background: transparent; border: 1px solid #fff; color: #fff; box-shadow: none; }
button.ghost:hover { background: rgba(255, 255, 255, 0.1); }

/* === 表单容器布局 === */
.form-container { position: absolute; top: 0; height: 100%; transition: all 0.6s ease-in-out; }
.sign-in-container { left: 0; width: 50%; z-index: 2; }

/* === 面板切换动画逻辑 === */
.container.right-panel-active .sign-in-container { 
  transform: translateX(100%); 
  opacity: 0; 
  pointer-events: none; 
  z-index: 0; 
}

.sign-up-container { left: 0; width: 50%; opacity: 0; z-index: 1; }
.container.right-panel-active .sign-up-container { transform: translateX(100%); opacity: 1; z-index: 5; animation: show 0.6s; }

/* 面板显示动画 */
@keyframes show { 0%, 49.99% { opacity: 0; z-index: 1; } 50%, 100% { opacity: 1; z-index: 5; } }
/* === 遮罩层与切换面板样式 === */
.overlay-container { position: absolute; top: 0; left: 50%; width: 50%; height: 100%; overflow: hidden; transition: transform 0.6s ease-in-out; z-index: 100; }
.container.right-panel-active .overlay-container { transform: translateX(-100%); }
.overlay { background: linear-gradient(to right, #24243e, #302b63, #0f0c29); color: #FFFFFF; position: relative; left: -100%; height: 100%; width: 200%; transform: translateX(0); transition: transform 0.6s ease-in-out; }
.container.right-panel-active .overlay { transform: translateX(50%); }
.overlay-panel { position: absolute; display: flex; align-items: center; justify-content: center; flex-direction: column; padding: 0 40px; text-align: center; top: 0; height: 100%; width: 50%; transform: translateX(0); transition: transform 0.6s ease-in-out; }
.overlay-left { transform: translateX(-20%); }
.container.right-panel-active .overlay-left { transform: translateX(0); }
.overlay-right { right: 0; transform: translateX(0); }
.container.right-panel-active .overlay-right { transform: translateX(20%); }
form { background-color: transparent; display: flex; align-items: center; justify-content: center; flex-direction: column; padding: 0 50px; height: 100%; text-align: center; }
.error-message { color: #ff6b6b; font-size: 12px; margin-top: 5px; }
</style>