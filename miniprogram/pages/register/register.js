const { post } = require('../../utils/request')

Page({
  data: {
    form: {
      username: '',
      password: '',
      newPassword: '',
      role: '普通用户'
    }
  },

  onInput(e) {
    const field = e.currentTarget.dataset.field
    this.setData({ ['form.' + field]: e.detail.value })
  },

  register() {
    const { username, password, newPassword } = this.data.form
    if (!username.trim()) {
      wx.showToast({ title: '请输入账号', icon: 'none' })
      return
    }
    if (!password.trim()) {
      wx.showToast({ title: '请输入密码', icon: 'none' })
      return
    }
    if (password !== newPassword) {
      wx.showToast({ title: '两次密码不一致', icon: 'none' })
      return
    }

    post('/register', this.data.form).then(res => {
      if (res.code === '200') {
        wx.showToast({ title: '注册成功', icon: 'success' })
        setTimeout(() => {
          wx.navigateBack()
        }, 1000)
      } else {
        wx.showToast({ title: res.msg || '注册失败', icon: 'none' })
      }
    })
  },

  goLogin() {
    wx.navigateBack()
  }
})
