// WebSocket连接管理
import { ref } from 'vue'

class WebSocketService {
  constructor() {
    this.ws = null
    this.reconnectTimer = null
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 5
    this.listeners = new Map()
  }

  // 连接WebSocket
  connect(userId) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      console.log('WebSocket已连接')
      return
    }

    // 使用相对路径，自动适配协议和域名
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    const host = window.location.hostname
    const port = '3473' // 后端端口
    // 注意：后端有 context-path: /api，所以WebSocket路径也要加上 /api
    const wsUrl = `${protocol}//${host}:${port}/api/ws/notifications?userId=${userId}`

    console.log('正在连接WebSocket:', wsUrl)

    try {
      this.ws = new WebSocket(wsUrl)

      this.ws.onopen = () => {
        console.log('WebSocket连接成功')
        this.reconnectAttempts = 0
        if (this.reconnectTimer) {
          clearTimeout(this.reconnectTimer)
          this.reconnectTimer = null
        }
      }

      this.ws.onmessage = (event) => {
        try {
          const notification = JSON.parse(event.data)
          console.log('收到WebSocket消息:', notification)

          // 触发所有监听器
          this.listeners.forEach((callback) => {
            callback(notification)
          })
        } catch (error) {
          console.error('解析WebSocket消息失败:', error)
        }
      }

      this.ws.onerror = (error) => {
        console.error('WebSocket连接错误，可能是后端服务未启动或端口不正确')
        console.log('请确保后端服务运行在 http://localhost:3473')
      }

      this.ws.onclose = () => {
        console.log('WebSocket连接关闭')
        this.attemptReconnect(userId)
      }
    } catch (error) {
      console.error('创建WebSocket连接失败:', error)
    }
  }

  // 尝试重连
  attemptReconnect(userId) {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++
      const delay = Math.min(1000 * Math.pow(2, this.reconnectAttempts), 30000)
      console.log(`${delay}ms后尝试重连 (${this.reconnectAttempts}/${this.maxReconnectAttempts})`)

      this.reconnectTimer = setTimeout(() => {
        this.connect(userId)
      }, delay)
    } else {
      console.error('WebSocket重连失败，已达到最大重连次数')
    }
  }

  // 添加消息监听器
  addListener(id, callback) {
    this.listeners.set(id, callback)
  }

  // 移除消息监听器
  removeListener(id) {
    this.listeners.delete(id)
  }

  // 断开连接
  disconnect() {
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }

    if (this.ws) {
      this.ws.close()
      this.ws = null
    }

    this.listeners.clear()
    this.reconnectAttempts = 0
  }

  // 发送消息
  send(message) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(message))
    } else {
      console.error('WebSocket未连接')
    }
  }
}

// 创建单例
const wsService = new WebSocketService()

export default wsService
