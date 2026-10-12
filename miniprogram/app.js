App({
  globalData: {
    user: null,
    baseUrl: 'http://10.144.174.167:8080'
  },

  onLaunch() {
    const user = wx.getStorageSync('system-user')
    if (user) {
      this.globalData.user = user
    }
  },

  checkLogin() {
    const user = wx.getStorageSync('system-user')
    if (!user || !user.id) {
      wx.navigateTo({ url: '/pages/login/login' })
      return null
    }
    this.globalData.user = user
    return user
  },

  setUser(user) {
    this.globalData.user = user
    wx.setStorageSync('system-user', user)
  },

  logout() {
    this.globalData.user = null
    wx.removeStorageSync('system-user')
  }
})
