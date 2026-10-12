const { put } = require('../../utils/request')

Page({
  data: {
    form: {
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    }
  },

  onInput(e) {
    const field = e.currentTarget.dataset.field
    this.setData({ ['form.' + field]: e.detail.value })
  },

  save() {
    const { oldPassword, newPassword, confirmPassword } = this.data.form
    if (!oldPassword.trim()) {
      wx.showToast({ title: '请输入原密码', icon: 'none' })
      return
    }
    if (!newPassword.trim()) {
      wx.showToast({ title: '请输入新密码', icon: 'none' })
      return
    }
    if (newPassword !== confirmPassword) {
      wx.showToast({ title: '两次密码不一致', icon: 'none' })
      return
    }

    const user = wx.getStorageSync('system-user')
    if (!user || !user.id) {
      wx.navigateTo({ url: '/pages/login/login' })
      return
    }

    put('/user/update', {
      id: user.id,
      password: newPassword,
      oldPassword: oldPassword
    }).then(res => {
      if (res.code === '200') {
        wx.showToast({ title: '密码修改成功，请重新登录', icon: 'success' })
        wx.removeStorageSync('system-user')
        setTimeout(() => {
          wx.redirectTo({ url: '/pages/login/login' })
        }, 1500)
      } else {
        wx.showToast({ title: res.msg || '修改失败', icon: 'none' })
      }
    })
  }
})
