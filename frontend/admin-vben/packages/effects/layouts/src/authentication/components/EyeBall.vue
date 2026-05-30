<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue';

const props = withDefaults(
  defineProps<{
    eyeColor?: string;
    forceLookX?: number;
    forceLookY?: number;
    isBlinking?: boolean;
    maxDistance?: number;
    pupilColor?: string;
    pupilSize?: number;
    size?: number;
  }>(),
  {
    eyeColor: 'white',
    forceLookX: undefined,
    forceLookY: undefined,
    isBlinking: false,
    maxDistance: 10,
    pupilColor: 'black',
    pupilSize: 16,
    size: 48,
  },
);

const mx = ref(0);
const my = ref(0);
const eyeRef = ref<HTMLDivElement>();

const onMove = (e: MouseEvent) => {
  mx.value = e.clientX;
  my.value = e.clientY;
};

onMounted(() => window.addEventListener('mousemove', onMove));
onUnmounted(() => window.removeEventListener('mousemove', onMove));

const pos = computed(() => {
  if (!eyeRef.value) return { x: 0, y: 0 };
  if (props.forceLookX !== undefined && props.forceLookY !== undefined)
    return { x: props.forceLookX, y: props.forceLookY };
  const r = eyeRef.value.getBoundingClientRect();
  const dx = mx.value - (r.left + r.width / 2);
  const dy = my.value - (r.top + r.height / 2);
  const d = Math.min(Math.hypot(dx, dy), props.maxDistance);
  const a = Math.atan2(dy, dx);
  return { x: Math.cos(a) * d, y: Math.sin(a) * d };
});
</script>

<template>
  <div
    ref="eyeRef"
    :style="{
      width: `${size}px`,
      height: isBlinking ? '2px' : `${size}px`,
      backgroundColor: eyeColor,
    }"
    class="eyeball"
  >
    <div
      v-if="!isBlinking"
      :style="{
        width: `${pupilSize}px`,
        height: `${pupilSize}px`,
        backgroundColor: pupilColor,
        transform: `translate(${pos.x}px, ${pos.y}px)`,
      }"
      class="pupil-inner"
    ></div>
  </div>
</template>

<style scoped>
.eyeball {
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.15s;
  overflow: hidden;
}
.pupil-inner {
  border-radius: 50%;
  transition: transform 0.1s ease-out;
}
</style>
