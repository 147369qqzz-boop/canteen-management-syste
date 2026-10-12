const app = getApp()

Page({
  data: {
    user: {}
  },

  onShow() {
    const user = wx.getStorageSync('system-user') || {}
    this.setData({ user })
    wx.stopPullDownRefresh()
  },

  onPullDownRefresh() {
    this.onShow()
  },

  goPage(e) {
    const user = wx.getStorageSync('system-user')
    if (!user || !user.id) { wx.navigateTo({ url: '/pages/login/login' }); return }
    wx.navigateTo({ url: e.currentTarget.dataset.url })
  },

  goOrders(e) {
    const user = wx.getStorageSync('system-user')
    if (!user || !user.id) { wx.navigateTo({ url: '/pages/login/login' }); return }
    wx.navigateTo({ url: '/pages/orders/orders' })
  },

  goLogin() { wx.navigateTo({ url: '/pages/login/login' }) },

  logout() {
    wx.showModal({
      title: '提示',
      content: '确定退出登录吗？',
      success: (res) => {
        if (res.confirm) {
          wx.removeStorageSync('system-user')
          wx.showToast({ title: '已退出', icon: 'none' })
          this.setData({ user: {} })
        }
      }
    })
  }
})
