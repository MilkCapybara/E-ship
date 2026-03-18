<template>
  <div class="particle-background">
    <canvas ref="canvasRef"></canvas>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const canvasRef = ref(null)
let animationId = null
let particles = []

class Particle {
  constructor(canvas) {
    this.canvas = canvas
    this.x = Math.random() * canvas.width
    this.y = Math.random() * canvas.height
    this.size = Math.random() * 3 + 1
    this.speedX = Math.random() * 0.5 - 0.25
    this.speedY = Math.random() * 0.5 - 0.25
    this.opacity = Math.random() * 0.5 + 0.3
  }

  update() {
    this.x += this.speedX
    this.y += this.speedY

    if (this.x > this.canvas.width) this.x = 0
    if (this.x < 0) this.x = this.canvas.width
    if (this.y > this.canvas.height) this.y = 0
    if (this.y < 0) this.y = this.canvas.height
  }

  draw(ctx) {
    ctx.fillStyle = `rgba(255, 255, 255, ${this.opacity})`
    ctx.beginPath()
    ctx.arc(this.x, this.y, this.size, 0, Math.PI * 2)
    ctx.fill()
  }
}

const initParticles = () => {
  const canvas = canvasRef.value
  if (!canvas) return

  canvas.width = window.innerWidth
  canvas.height = window.innerHeight

  particles = []
  const particleCount = Math.floor((canvas.width * canvas.height) / 15000)

  for (let i = 0; i < particleCount; i++) {
    particles.push(new Particle(canvas))
  }
}

const animate = () => {
  const canvas = canvasRef.value
  if (!canvas) return

  const ctx = canvas.getContext('2d')
  ctx.clearRect(0, 0, canvas.width, canvas.height)

  particles.forEach(particle => {
    particle.update()
    particle.draw(ctx)
  })

  // Draw connections
  particles.forEach((particleA, indexA) => {
    particles.slice(indexA + 1).forEach(particleB => {
      const dx = particleA.x - particleB.x
      const dy = particleA.y - particleB.y
      const distance = Math.sqrt(dx * dx + dy * dy)

      if (distance < 100) {
        ctx.strokeStyle = `rgba(255, 255, 255, ${0.1 * (1 - distance / 100)})`
        ctx.lineWidth = 0.5
        ctx.beginPath()
        ctx.moveTo(particleA.x, particleA.y)
        ctx.lineTo(particleB.x, particleB.y)
        ctx.stroke()
      }
    })
  })

  animationId = requestAnimationFrame(animate)
}

const handleResize = () => {
  initParticles()
}

onMounted(() => {
  initParticles()
  animate()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  if (animationId) {
    cancelAnimationFrame(animationId)
  }
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.particle-background {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
  pointer-events: none;
}

canvas {
  display: block;
  width: 100%;
  height: 100%;
}
</style>
