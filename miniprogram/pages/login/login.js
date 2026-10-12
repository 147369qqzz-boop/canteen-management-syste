const { post } = require('../../utils/request')

Page({
  data: {
    form: {
      username: '',
      password: '',
      role: '普通用户'
    },
    roles: ['普通用户', '管理员', '商家'],
    roleIndex: 0,
    loading: false
  },

  onInput(e) {
    const field = e.currentTarget.dataset.field
    this.setData({ ['form.' + field]: e.detail.value })
  },

  onRoleChange(e) {
    const idx = e.detail.value
    this.setData({
      roleIndex: idx,
      'form.role': this.data.roles[idx]
    })
  },

  login() {
    const { username, password, role } = this.data.form
    if (!username.trim()) {
      wx.showToast({ title: '请输入账号', icon: 'none' })
      return
    }
    if (!password.trim()) {
      wx.showToast({ title: '请输入密码', icon: 'none' })
      return
    }

    this.setData({ loading: true })
    post('/login', { username, password, role }).then(res => {
      if (res.code === '200') {
        wx.setStorageSync('system-user', res.data)
        wx.showToast({ title: '登录成功', icon: 'success' })
        setTimeout(() => {
          if (res.data.role === '管理员') {
            wx.showToast({ title: '请使用管理端登录', icon: 'none' })
          } else if (res.data.role === '商家') {
            wx.showToast({ title: '请使用商家端登录', icon: 'none' })
          } else {
            wx.switchTab({ url: '/pages/index/index' })
          }
        }, 1000)
      } else {
        wx.showToast({ title: res.msg || '登录失败', icon: 'none' })
      }
    }).finally(() => {
      this.setData({ loading: false })
    })
  },

  goRegister() {
    wx.navigateTo({ url: '/pages/register/register' })
  }
})
