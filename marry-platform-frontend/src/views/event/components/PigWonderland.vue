<template>
  <div ref="effectRef" class="pig-wonderland" aria-hidden="true">
    <canvas ref="canvasRef" class="particle-canvas" />

    <div class="pig-floater pig-one">
      <div class="pig-sticker" :style="pigStyles[0]" />
    </div>
    <div class="pig-floater pig-two">
      <div class="pig-sticker" :style="pigStyles[1]" />
    </div>
    <div class="pig-floater pig-three">
      <div class="pig-sticker" :style="pigStyles[2]" />
    </div>

    <div class="cursor-aura" />
  </div>
</template>

<script setup lang="ts">
import { onActivated, onBeforeUnmount, onDeactivated, onMounted, ref } from 'vue'
import sleepyPigUrl from '@/assets/pig/sleepy-pig.svg'
import balloonPigUrl from '@/assets/pig/balloon-pig.svg'
import flowerPigUrl from '@/assets/pig/flower-pig.svg'

interface Particle {
  x: number
  y: number
  vx: number
  vy: number
  size: number
  opacity: number
  rotation: number
  spin: number
  kind: 'snow' | 'star' | 'pig' | 'trail'
  life?: number
  imageIndex?: number
}

const effectRef = ref<HTMLElement | null>(null)
const canvasRef = ref<HTMLCanvasElement | null>(null)
const pigUrls = [sleepyPigUrl, balloonPigUrl, flowerPigUrl]
const pigStyles = pigUrls.map((url) => ({ backgroundImage: `url(${url})` }))

let context: CanvasRenderingContext2D | null = null
let animationId = 0
let width = 0
let height = 0
let dpr = 1
let lastTime = 0
let lastTrailTime = 0
let particles: Particle[] = []
let reducedMotion = false
let pigImages: HTMLImageElement[] = []
let effectsRunning = false

const mouse = { x: -1000, y: -1000, targetX: 0, targetY: 0, active: false }

function random(min: number, max: number) {
  return min + Math.random() * (max - min)
}

function makeParticle(kind: Particle['kind'], initial = false): Particle {
  const isPig = kind === 'pig'
  return {
    x: random(0, width),
    y: initial ? random(0, height) : random(-80, -10),
    vx: random(-0.22, 0.22),
    vy: isPig ? random(0.16, 0.3) : random(0.35, 1.05),
    size: isPig ? random(30, 48) : kind === 'star' ? random(3, 7) : random(2, 5.5),
    opacity: isPig ? random(0.34, 0.54) : random(0.3, 0.85),
    rotation: isPig ? random(-0.16, 0.16) : random(0, Math.PI * 2),
    spin: isPig ? random(-0.0014, 0.0014) : random(-0.008, 0.008),
    kind,
    imageIndex: isPig ? Math.floor(random(0, pigUrls.length)) : undefined
  }
}

function setupParticles() {
  const density = Math.min(110, Math.max(48, Math.round((width * height) / 14500)))
  particles = [
    ...Array.from({ length: density }, () => makeParticle('snow', true)),
    ...Array.from({ length: Math.round(density * 0.2) }, () => makeParticle('star', true)),
    ...Array.from({ length: width < 768 ? 2 : 4 }, () => makeParticle('pig', true))
  ]
}

function resize() {
  const canvas = canvasRef.value
  if (!canvas) return
  width = window.innerWidth
  height = window.innerHeight
  dpr = Math.min(window.devicePixelRatio || 1, 2)
  canvas.width = Math.round(width * dpr)
  canvas.height = Math.round(height * dpr)
  canvas.style.width = `${width}px`
  canvas.style.height = `${height}px`
  context = canvas.getContext('2d')
  context?.setTransform(dpr, 0, 0, dpr, 0, 0)
  setupParticles()
}

function drawSnow(p: Particle) {
  if (!context) return
  const gradient = context.createRadialGradient(p.x, p.y, 0, p.x, p.y, p.size)
  gradient.addColorStop(0, `rgba(255,255,255,${p.opacity})`)
  gradient.addColorStop(0.45, `rgba(255,220,235,${p.opacity * 0.7})`)
  gradient.addColorStop(1, 'rgba(255,190,220,0)')
  context.fillStyle = gradient
  context.beginPath()
  context.arc(p.x, p.y, p.size, 0, Math.PI * 2)
  context.fill()
}

function drawStar(p: Particle) {
  if (!context) return
  context.save()
  context.translate(p.x, p.y)
  context.rotate(p.rotation)
  context.strokeStyle = `rgba(255, ${Math.round(random(190, 235))}, 110, ${p.opacity})`
  context.lineWidth = 1.2
  context.beginPath()
  context.moveTo(-p.size, 0)
  context.lineTo(p.size, 0)
  context.moveTo(0, -p.size)
  context.lineTo(0, p.size)
  context.stroke()
  context.restore()
}

function drawPig(p: Particle) {
  const image = pigImages[p.imageIndex ?? 0]
  if (!context || !image?.complete) return
  context.save()
  context.globalAlpha = p.opacity
  context.translate(p.x, p.y)
  context.rotate(p.rotation)
  const ratio = image.naturalWidth / Math.max(image.naturalHeight, 1)
  const drawWidth = ratio >= 1 ? p.size * 2.35 : p.size * 2
  const drawHeight = ratio >= 1 ? drawWidth / ratio : (p.size * 2) / ratio
  context.drawImage(image, -drawWidth / 2, -drawHeight / 2, drawWidth, drawHeight)
  context.restore()
}

function updateParticle(p: Particle, delta: number) {
  const speed = Math.min(delta / 16.67, 2)
  p.rotation += p.spin * speed
  p.x += (p.vx + Math.sin(p.y * 0.012 + p.rotation) * 0.15) * speed
  p.y += p.vy * speed

  if (mouse.active) {
    const dx = p.x - mouse.x
    const dy = p.y - mouse.y
    const distance = Math.sqrt(dx * dx + dy * dy)
    const radius = p.kind === 'pig' ? 150 : 105
    if (distance > 0 && distance < radius) {
      const force = (1 - distance / radius) * (p.kind === 'pig' ? 1.2 : 2.5)
      p.x += (dx / distance) * force * speed
      p.y += (dy / distance) * force * speed
    }
  }

  if (p.kind === 'trail') {
    p.life = (p.life ?? 1) - 0.025 * speed
    p.opacity = Math.max(0, p.life)
    p.size *= 0.985
  } else if (p.y > height + p.size * 2 || p.x < -80 || p.x > width + 80) {
    Object.assign(p, makeParticle(p.kind))
  }
}

function animate(time: number) {
  if (!context) return
  const delta = lastTime ? time - lastTime : 16.67
  lastTime = time
  context.clearRect(0, 0, width, height)

  mouse.x += (mouse.targetX - mouse.x) * 0.16
  mouse.y += (mouse.targetY - mouse.y) * 0.16

  particles.forEach((particle) => {
    updateParticle(particle, delta)
    if (particle.kind === 'snow' || particle.kind === 'trail') drawSnow(particle)
    else if (particle.kind === 'star') drawStar(particle)
    else drawPig(particle)
  })
  particles = particles.filter((p) => p.kind !== 'trail' || (p.life ?? 0) > 0.03)
  animationId = requestAnimationFrame(animate)
}

function addTrail(x: number, y: number, time: number) {
  if (time - lastTrailTime < 28 || particles.length > 150) return
  lastTrailTime = time
  for (let i = 0; i < 2; i += 1) {
    particles.push({
      x: x + random(-7, 7), y: y + random(-7, 7),
      vx: random(-0.25, 0.25), vy: random(-0.3, 0.15),
      size: random(3, 7), opacity: 0.85,
      rotation: 0, spin: 0, kind: 'trail', life: 1
    })
  }
}

function onPointerMove(event: PointerEvent) {
  mouse.targetX = event.clientX
  mouse.targetY = event.clientY
  if (!mouse.active) {
    mouse.x = event.clientX
    mouse.y = event.clientY
  }
  mouse.active = true
  const nx = event.clientX / Math.max(width, 1) - 0.5
  const ny = event.clientY / Math.max(height, 1) - 0.5
  effectRef.value?.style.setProperty('--parallax-x', `${nx * 24}px`)
  effectRef.value?.style.setProperty('--parallax-y', `${ny * 18}px`)
  effectRef.value?.style.setProperty('--cursor-x', `${event.clientX}px`)
  effectRef.value?.style.setProperty('--cursor-y', `${event.clientY}px`)
  if (!reducedMotion) addTrail(event.clientX, event.clientY, performance.now())
}

function onPointerLeave() {
  mouse.active = false
  effectRef.value?.style.setProperty('--parallax-x', '0px')
  effectRef.value?.style.setProperty('--parallax-y', '0px')
}

function onPointerDown(event: PointerEvent) {
  if (reducedMotion) return
  for (let i = 0; i < 18; i += 1) {
    const angle = (Math.PI * 2 * i) / 18
    particles.push({
      x: event.clientX, y: event.clientY,
      vx: Math.cos(angle) * random(0.8, 2.2),
      vy: Math.sin(angle) * random(0.8, 2.2),
      size: random(3, 7), opacity: 1,
      rotation: angle, spin: 0.03, kind: 'trail', life: 1
    })
  }
}

function startEffects() {
  if (effectsRunning || !canvasRef.value) return
  effectsRunning = true
  lastTime = 0
  resize()
  window.addEventListener('resize', resize)
  window.addEventListener('pointermove', onPointerMove, { passive: true })
  document.documentElement.addEventListener('pointerleave', onPointerLeave)
  window.addEventListener('pointerdown', onPointerDown, { passive: true })
  if (!reducedMotion) animationId = requestAnimationFrame(animate)
  else if (context) {
    particles.slice(0, 28).forEach(drawSnow)
    particles.filter((p) => p.kind === 'pig').forEach(drawPig)
  }
}

function stopEffects() {
  if (!effectsRunning) return
  effectsRunning = false
  cancelAnimationFrame(animationId)
  animationId = 0
  mouse.active = false
  window.removeEventListener('resize', resize)
  window.removeEventListener('pointermove', onPointerMove)
  document.documentElement.removeEventListener('pointerleave', onPointerLeave)
  window.removeEventListener('pointerdown', onPointerDown)
}

onMounted(() => {
  reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
  pigImages = pigUrls.map((url) => {
    const image = new Image()
    image.src = url
    return image
  })
  startEffects()
})

// event 页面由 KeepAlive 缓存，离开页面时暂停 Canvas，返回时再恢复。
onActivated(startEffects)
onDeactivated(stopEffects)
onBeforeUnmount(stopEffects)
</script>

<style scoped>
.pig-wonderland {
  --parallax-x: 0px;
  --parallax-y: 0px;
  --cursor-x: -100px;
  --cursor-y: -100px;
  position: fixed;
  inset: 0;
  z-index: 8;
  overflow: hidden;
  pointer-events: none;
}

.particle-canvas {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
}

.cursor-aura {
  position: absolute;
  left: var(--cursor-x);
  top: var(--cursor-y);
  width: 180px;
  height: 180px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(251, 113, 133, 0.13), rgba(253, 224, 71, 0.05) 42%, transparent 70%);
  transform: translate(-50%, -50%);
  transition: left 80ms linear, top 80ms linear;
}

.pig-floater {
  position: absolute;
  opacity: 0.68;
  filter: drop-shadow(0 12px 14px rgba(167, 71, 101, 0.16));
}

.pig-sticker {
  width: 100%;
  height: 100%;
  background-repeat: no-repeat;
  background-position: center;
  background-size: contain;
  transform: translate(calc(var(--parallax-x) * var(--direction)), calc(var(--parallax-y) * var(--direction)));
  transition: transform 180ms ease-out;
}

.pig-one {
  --direction: 1;
  width: 156px;
  height: 118px;
  right: 4vw;
  top: 24vh;
  animation: pigFloatOne 8s ease-in-out infinite;
}
.pig-two {
  --direction: -0.7;
  width: 112px;
  height: 112px;
  left: 2vw;
  top: 62vh;
  opacity: 0.52;
  animation: pigFloatTwo 10s ease-in-out infinite;
}
.pig-three {
  --direction: 0.5;
  width: 98px;
  height: 98px;
  right: 18vw;
  bottom: 4vh;
  opacity: 0.44;
  animation: pigFloatThree 12s ease-in-out infinite;
}

@keyframes pigFloatOne {
  0%, 100% { translate: 0 0; rotate: -5deg; }
  50% { translate: -18px -28px; rotate: 5deg; }
}
@keyframes pigFloatTwo {
  0%, 100% { translate: 0 0; rotate: 6deg; }
  50% { translate: 22px -22px; rotate: -4deg; }
}
@keyframes pigFloatThree {
  0%, 100% { translate: 0 0; rotate: -3deg; }
  50% { translate: -14px -20px; rotate: 7deg; }
}

html.dark .pig-floater {
  opacity: 0.52;
  filter: drop-shadow(0 12px 18px rgba(251, 113, 133, 0.16));
}
html.dark .cursor-aura {
  background: radial-gradient(circle, rgba(244, 114, 182, 0.16), rgba(96, 165, 250, 0.05) 45%, transparent 70%);
}

@media (max-width: 768px) {
  .pig-one { width: 108px; height: 82px; right: -18px; }
  .pig-two { width: 78px; height: 78px; left: -18px; }
  .pig-three { display: none; }
  .cursor-aura { display: none; }
}

@media (prefers-reduced-motion: reduce) {
  .pig-floater { animation: none; }
  .pig-sticker { transition: none; }
  .cursor-aura { display: none; }
}
</style>
