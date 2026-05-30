<script setup lang="ts">
import { computed, onMounted, onUnmounted, reactive, ref, watch } from 'vue';

import EyeBall from './EyeBall.vue';
import Pupil from './Pupil.vue';

// ── 动画状态（通过 DOM 事件自动检测） ──
const isTyping = ref(false);
const hasSecret = ref(false);
const secretVisible = ref(false);

let focusedInput: HTMLInputElement | null = null;
let passwordInput: HTMLInputElement | null = null;
let passwordObserver: MutationObserver | undefined;

/** 在表单中查找密码输入框 */
function findPasswordInput(): HTMLInputElement | null {
  // 优先通过 type="password" 查找
  const pwdInputs =
    document.querySelectorAll<HTMLInputElement>('input[type="password"]');
  if (pwdInputs.length > 0) return pwdInputs[0]!;
  // 备用：查找所有 input[type=text] 中 autocomplete 包含 password 的
  const textInputs =
    document.querySelectorAll<HTMLInputElement>('input[type="text"]');
  for (const input of textInputs) {
    if (input.autocomplete?.includes('password')) return input;
  }
  return null;
}

/** 更新密码相关状态 */
function updateSecretState() {
  if (passwordInput) {
    hasSecret.value = passwordInput.value.length > 0;
    secretVisible.value = passwordInput.type === 'text';
  } else {
    hasSecret.value = false;
    secretVisible.value = false;
  }
}

/** 监听密码输入框的 type 属性变化（密码可见性切换） */
function observePasswordInput() {
  // 如果已有有效引用，不需要重新查找
  if (passwordInput && document.contains(passwordInput)) {
    updateSecretState();
    return;
  }
  passwordObserver?.disconnect();
  const input = findPasswordInput();
  passwordInput = input;
  if (input) {
    updateSecretState();
    passwordObserver = new MutationObserver(() => {
      updateSecretState();
    });
    passwordObserver.observe(input, {
      attributeFilter: ['type'],
    });
  }
}

function onDocFocusIn(e: FocusEvent) {
  const target = e.target;
  if (target instanceof HTMLInputElement) {
    focusedInput = target;
    // 聚焦输入框时结束错误回避状态
    isErrorHiding.value = false;
    clearTimeout(errorTimeout);
    isTyping.value = true;
    // 每次聚焦都重新检查密码框
    observePasswordInput();
  }
}

function onDocFocusOut(e: FocusEvent) {
  const target = e.target;
  if (target === focusedInput) {
    focusedInput = null;
    isTyping.value = false;
  }
  // 短暂延迟后更新密码状态
  setTimeout(updateSecretState, 50);
}

function onDocInput(e: Event) {
  const target = e.target;
  if (target === passwordInput) {
    updateSecretState();
  }
}

// ── 登录失败回避动画 ──
let errorTimeout: number | undefined;

function onLoginError() {
  if (isErrorHiding.value) return;
  isErrorHiding.value = true;
  clearTimeout(errorTimeout);
  errorTimeout = window.setTimeout(() => {
    isErrorHiding.value = false;
  }, 2000);
}

// ── 角色位置和眼球跟踪 ──
const mouseX = ref(0);
const mouseY = ref(0);
const isPurpleBlinking = ref(false);
const isBlackBlinking = ref(false);
const isLookingAtEachOther = ref(false);
const isPurplePeeking = ref(false);
const isErrorHiding = ref(false);

const purpleRef = ref<HTMLDivElement | null>(null);
const blackRef = ref<HTMLDivElement | null>(null);
const yellowRef = ref<HTMLDivElement | null>(null);
const orangeRef = ref<HTMLDivElement | null>(null);

const purplePos = reactive({ bodySkew: 0, faceX: 0, faceY: 0 });
const blackPos = reactive({ bodySkew: 0, faceX: 0, faceY: 0 });
const yellowPos = reactive({ bodySkew: 0, faceX: 0, faceY: 0 });
const orangePos = reactive({ bodySkew: 0, faceX: 0, faceY: 0 });

// Derived state
const hiding = computed(() => secretVisible.value || isErrorHiding.value);
const leaning = computed(
  () => isTyping.value || (hasSecret.value && !secretVisible.value),
);

function calcPos(
  el: HTMLDivElement | null,
  target: { bodySkew: number; faceX: number; faceY: number },
) {
  if (!el) return;
  const r = el.getBoundingClientRect();
  const dx = mouseX.value - (r.left + r.width / 2);
  const dy = mouseY.value - (r.top + r.height / 3);
  target.faceX = Math.max(-15, Math.min(15, dx / 20));
  target.faceY = Math.max(-10, Math.min(10, dy / 30));
  target.bodySkew = Math.max(-6, Math.min(6, -dx / 120));
}

let rafId = 0;
function tick() {
  calcPos(purpleRef.value, purplePos);
  calcPos(blackRef.value, blackPos);
  calcPos(yellowRef.value, yellowPos);
  calcPos(orangeRef.value, orangePos);
  rafId = requestAnimationFrame(tick);
}

function onMouseMove(e: MouseEvent) {
  mouseX.value = e.clientX;
  mouseY.value = e.clientY;
}

function setupBlink(target: { value: boolean }) {
  let timer: number;
  const go = () => {
    timer = window.setTimeout(() => {
      target.value = true;
      window.setTimeout(() => {
        target.value = false;
        go();
      }, 150);
    }, Math.random() * 4000 + 3000);
  };
  go();
  return () => clearTimeout(timer);
}

let stopP: (() => void) | undefined;
let stopB: (() => void) | undefined;

onMounted(() => {
  window.addEventListener('mousemove', onMouseMove);
  document.addEventListener('focusin', onDocFocusIn);
  document.addEventListener('focusout', onDocFocusOut);
  document.addEventListener('input', onDocInput);
  stopP = setupBlink(isPurpleBlinking);
  stopB = setupBlink(isBlackBlinking);
  rafId = requestAnimationFrame(tick);
  // 初始查找密码框
  observePasswordInput();
  // 监听登录失败事件
  document.addEventListener('travis-login-error', onLoginError);
});
onUnmounted(() => {
  window.removeEventListener('mousemove', onMouseMove);
  document.removeEventListener('focusin', onDocFocusIn);
  document.removeEventListener('focusout', onDocFocusOut);
  document.removeEventListener('input', onDocInput);
  document.removeEventListener('travis-login-error', onLoginError);
  cancelAnimationFrame(rafId);
  passwordObserver?.disconnect();
  clearTimeout(peekT);
  clearTimeout(peekInterval);
  clearTimeout(errorTimeout);
  stopP?.();
  stopB?.();
});

// Look at each other when typing starts
watch(
  () => isTyping.value,
  (v) => {
    if (v) {
      isLookingAtEachOther.value = true;
      setTimeout(() => {
        isLookingAtEachOther.value = false;
      }, 800);
    } else {
      isLookingAtEachOther.value = false;
    }
  },
);

// Purple peeks when secret is visible
let peekT: number | undefined;
let peekInterval: number | undefined;

function startPeekLoop() {
  clearTimeout(peekT);
  clearTimeout(peekInterval);
  if (!secretVisible.value) return;

  // 立即偷瞄一次
  isPurplePeeking.value = true;
  peekT = window.setTimeout(() => {
    isPurplePeeking.value = false;
    // 偷瞄结束后，缩短间隔开始循环偷瞄
    scheduleNextPeek();
  }, 600);
}

function scheduleNextPeek() {
  if (!secretVisible.value) return;
  // 缩短间隔：500~1500ms
  peekInterval = window.setTimeout(() => {
    isPurplePeeking.value = true;
    peekT = window.setTimeout(() => {
      isPurplePeeking.value = false;
      scheduleNextPeek();
    }, 600);
  }, Math.random() * 1000 + 500);
}

watch(
  [secretVisible],
  () => {
    if (secretVisible.value) {
      startPeekLoop();
    } else {
      clearTimeout(peekT);
      clearTimeout(peekInterval);
      isPurplePeeking.value = false;
    }
  },
);

// ── 计算样式 ──
// 空闲状态（非输入、非隐藏）：强制 bodySkew 为 0 实现复位
const idle = computed(
  () =>
    !leaning.value &&
    !hiding.value &&
    !isLookingAtEachOther.value,
);

const purpleStyle = computed(() => {
  const height = leaning.value ? '440px' : '400px';
  const skew = idle.value ? 0 : purplePos.bodySkew;
  let transform = `skewX(${skew}deg)`;
  if (hiding.value) {
    transform = 'skewX(0deg)';
  } else if (leaning.value) {
    transform = `skewX(${skew - 12}deg) translateX(40px)`;
  }
  return { height, transform };
});

function getLeftRight(
  hideVal: string,
  lookVal: string,
  faceOffset: number,
  base: number,
) {
  if (hiding.value) return hideVal;
  if (isLookingAtEachOther.value) return lookVal;
  return `${base + faceOffset}px`;
}

const purpleEyesStyle = computed(() => ({
  gap: '32px',
  left: getLeftRight('20px', '55px', purplePos.faceX, 45),
  top: getLeftRight('35px', '65px', purplePos.faceY, 40),
}));

const purpleLookX = computed(() => {
  if (hiding.value) return isPurplePeeking.value ? 4 : -4;
  if (isLookingAtEachOther.value) return 3;
  return undefined;
});
const purpleLookY = computed(() => {
  if (hiding.value) return isPurplePeeking.value ? 0 : -4;
  if (isLookingAtEachOther.value) return 4;
  return undefined;
});

const blackStyle = computed(() => {
  const skew = idle.value ? 0 : blackPos.bodySkew;
  let transform = `skewX(${skew}deg)`;
  if (hiding.value) {
    transform = 'skewX(0deg)';
  } else if (isLookingAtEachOther.value) {
    transform = `skewX(${skew * 1.5 + 10}deg) translateX(20px)`;
  } else if (leaning.value) {
    transform = `skewX(${skew * 1.5}deg)`;
  }
  return { transform };
});

const blackEyesStyle = computed(() => ({
  gap: '24px',
  left: getLeftRight('10px', '32px', blackPos.faceX, 26),
  top: getLeftRight('28px', '12px', blackPos.faceY, 32),
}));

const blackLookX = computed(() => {
  if (hiding.value) return -4;
  if (isLookingAtEachOther.value) return 0;
  return undefined;
});
const blackLookY = computed(() => {
  if (hiding.value) return -4;
  if (isLookingAtEachOther.value) return -4;
  return undefined;
});

const orangeStyle = computed(() => ({
  transform: hiding.value
    ? 'skewX(0deg)'
    : `skewX(${idle.value ? 0 : orangePos.bodySkew}deg)`,
}));

const orangeEyesStyle = computed(() => ({
  gap: '32px',
  left: hiding.value ? '50px' : `${82 + orangePos.faceX}px`,
  top: hiding.value ? '85px' : `${90 + orangePos.faceY}px`,
}));

const yellowStyle = computed(() => ({
  transform: hiding.value
    ? 'skewX(0deg)'
    : `skewX(${idle.value ? 0 : yellowPos.bodySkew}deg)`,
}));

const yellowEyesStyle = computed(() => ({
  gap: '24px',
  left: hiding.value ? '20px' : `${52 + yellowPos.faceX}px`,
  top: hiding.value ? '35px' : `${40 + yellowPos.faceY}px`,
}));

const smallCharLookX = computed(() => {
  return hiding.value ? -5 : undefined;
});
const smallCharLookY = computed(() => {
  return hiding.value ? -4 : undefined;
});

const yellowMouthStyle = computed(() => ({
  background: isErrorHiding.value ? 'transparent' : '#2d2d2d',
  left: hiding.value ? '10px' : `${40 + yellowPos.faceX}px`,
  top: hiding.value ? '88px' : `${88 + yellowPos.faceY}px`,
}));
</script>

<template>
  <div class="characters-container">
    <!-- Purple -->
    <div ref="purpleRef" :style="purpleStyle" class="char purple">
      <div :style="purpleEyesStyle" class="eyes">
        <EyeBall
          v-for="i in 2"
          :key="`p${i}`"
          eye-color="white"
          :force-look-x="purpleLookX"
          :force-look-y="purpleLookY"
          :is-blinking="isPurpleBlinking"
          :max-distance="5"
          pupil-color="#2D2D2D"
          :pupil-size="7"
          :size="18"
        />
      </div>
    </div>
    <!-- Black -->
    <div ref="blackRef" :style="blackStyle" class="char black">
      <div :style="blackEyesStyle" class="eyes">
        <EyeBall
          v-for="i in 2"
          :key="`b${i}`"
          eye-color="white"
          :force-look-x="blackLookX"
          :force-look-y="blackLookY"
          :is-blinking="isBlackBlinking"
          :max-distance="4"
          pupil-color="#2D2D2D"
          :pupil-size="6"
          :size="16"
        />
      </div>
    </div>
    <!-- Orange -->
    <div ref="orangeRef" :style="orangeStyle" class="char orange">
      <div :style="orangeEyesStyle" class="eyes">
        <Pupil
          v-for="i in 2"
          :key="`o${i}`"
          :force-look-x="smallCharLookX"
          :force-look-y="smallCharLookY"
          :max-distance="5"
          pupil-color="#2D2D2D"
          :size="12"
        />
      </div>
    </div>
    <!-- Yellow -->
    <div ref="yellowRef" :style="yellowStyle" class="char yellow">
      <div :style="yellowEyesStyle" class="eyes">
        <Pupil
          v-for="i in 2"
          :key="`y${i}`"
          :force-look-x="smallCharLookX"
          :force-look-y="smallCharLookY"
          :max-distance="5"
          pupil-color="#2D2D2D"
          :size="12"
        />
      </div>
      <div :style="yellowMouthStyle" class="mouth">
        <svg
          v-if="isErrorHiding"
          class="wavy-mouth"
          height="16"
          viewBox="0 0 60 16"
          width="60"
        >
          <path
            d="M0 8 Q7.5 0 15 8 Q22.5 16 30 8 Q37.5 0 45 8 Q52.5 16 60 8"
            fill="none"
            stroke="#2d2d2d"
            stroke-width="3"
          />
        </svg>
      </div>
    </div>
  </div>
</template>

<style scoped>
.characters-container {
  position: relative;
  width: 450px;
  height: 400px;
}

.char {
  position: absolute;
  bottom: 0;
  transition: all 0.7s ease-in-out;
  transform-origin: bottom center;
}

.purple {
  left: 70px;
  width: 180px;
  background: #6c3ff5;
  border-radius: 10px 10px 0 0;
  z-index: 1;
}

.black {
  left: 240px;
  width: 120px;
  height: 310px;
  background: #2d2d2d;
  border-radius: 8px 8px 0 0;
  z-index: 2;
}

.orange {
  left: 0;
  width: 240px;
  height: 200px;
  background: #ff9b6b;
  border-radius: 120px 120px 0 0;
  z-index: 3;
}

.yellow {
  left: 310px;
  width: 140px;
  height: 230px;
  background: #e8d754;
  border-radius: 70px 70px 0 0;
  z-index: 4;
}

.eyes {
  position: absolute;
  display: flex;
  transition: all 0.7s ease-in-out;
}

.mouth {
  position: absolute;
  width: 80px;
  height: 4px;
  border-radius: 4px;
  transition: all 0.2s ease-out;
}

.wavy-mouth {
  display: block;
  margin-top: -6px;
}
</style>
