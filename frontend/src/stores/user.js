import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || '{}'),
    role: localStorage.getItem('role') || ''
  }),

  getters: {
    isLoggedIn: (state) => !!state.token,
    isShipOwner: (state) => state.role === 'SHIP_OWNER',
    isRenter: (state) => state.role === 'RENTER',
    isAdmin: (state) => state.role === 'ADMIN'
  },

  actions: {
    setToken(token) {
      this.token = token
      localStorage.setItem('token', token)
    },

    setUserInfo(userInfo) {
      this.userInfo = userInfo
      this.role = userInfo.role
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
      localStorage.setItem('role', userInfo.role)
    },

    logout() {
      this.token = ''
      this.userInfo = {}
      this.role = ''
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      localStorage.removeItem('role')
    },

    clearUser() {
      this.logout()
    }
  }
})
