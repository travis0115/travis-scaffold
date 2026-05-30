<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue';

const props = withDefaults(
  defineProps<{
    forceLookX?: number;
    forceLookY?: number;
    maxDistance?: number;
    pupilColor?: string;
    size?: number;
  }>(),
  {
    forceLookX: undefined,
    forceLookY: undefined,
    maxDistance: 5,
    pupilColor: 'black',
    size: 12,
  },
);

const mx = ref(0);
const my = ref(0);
const pupilRef = ref<HTMLDivElement>();

const onMove = (e: MouseEvent) => {
  mx.value = e.clientX;
  my.value = e.clientY;
};

onMounted(() => window.addEventListener('mousemove', onMove));
onUnmounted(() => window.removeEventListener('mousemove', onMove));

const pos = computed(() => {
  if (!pupilRef.value) return { x: 0, y: 0 };
  if (props.forceLookX !== undefined && props.forceLookY !== undefined)
    return { x: props.forceLookX, y: props.forceLookY };
  const r = pupilRef.value.getBoundingClientRect();
  const dx = mx.value - (r.left + r.width / 2);
  const dy = my.value - (r.top + r.height / 2);
  const d = Math.min(Math.hypot(dx, dy), props.maxDistance);
  const a = Math.atan2(dy, dx);
  return { x: Math.cos(a) * d, y: Math.sin(a) * d };
});
</script>

<template>
  <div
    ref="pupilRef"
    :style="{
      width: `${size}px`,
      height: `${size}px`,
      backgroundColor: pupilColor,
      transform: `translate(${pos.x}px, ${pos.y}px)`,
    }"
    class="pupil"
  ></div>
</template>

<style scoped>
.pupil {
  border-radius: 50%;
  transition: transform 0.1s ease-out;
}
</style>
