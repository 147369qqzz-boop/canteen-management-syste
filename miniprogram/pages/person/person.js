const { get, put, upload } = require('../../utils/request')

Page({
  data: {
    user: {}
  },

  onShow() {
    this.loadUser()
  },

  loadUser() {
    const user = wx.getStorageSync('system-user')
    if (!user || !user.id) {
      wx.navigateTo({ url: '/pages/login/login' })
      return
    }
    get('/user/selectById/' + user.id).then(res => {
      const data = res.data || {}
      this.setData({ user: data })
      wx.setStorageSync('system-user', data)
    })
  },

  onInput(e) {
    const field = e.currentTarget.dataset.field
    this.setData({ ['user.' + field]: e.detail.value })
  },

  uploadAvatar() {
    wx.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const filePath = res.tempFilePaths[0]
        upload(filePath).then(url => {
          this.setData({ 'user.avatar': url })
          wx.showToast({ title: '上传成功', icon: 'success' })
        })
      }
    })
  },

  save() {
    const { user } = this.data
    if (!user.name || !user.name.trim()) {
      wx.showToast({ title: '请输入姓名', icon: 'none' })
      return
    }
    put('/user/update', user).then(res => {
      if (res.code === '200') {
        wx.showToast({ title: '更新成功', icon: 'success' })
        this.loadUser()
      } else {
        wx.showToast({ title: res.msg || '更新失败', icon: 'none' })
      }
    })
  }
})
