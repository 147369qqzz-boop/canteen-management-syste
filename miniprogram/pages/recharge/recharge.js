const { get, post } = require('../../utils/request')

Page({
  data: {
    tableData: [],
    pageNum: 1,
    pageSize: 10,
    total: 0,
    hasMore: true,
    time: '',
    account: 0,
    showRechargeModal: false,
    rechargeForm: {
      money: '1',
      type: '微信支付'
    }
  },

  onShow() {
    this.loadAccount()
    this.setData({ pageNum: 1, tableData: [], hasMore: true })
    this.load()
  },

  loadAccount() {
    const user = wx.getStorageSync('system-user')
    if (!user || !user.id) {
      wx.navigateTo({ url: '/pages/login/login' })
      return
    }
    get('/user/selectById/' + user.id).then(res => {
      this.setData({ account: (res.data || {}).account || 0 })
    })
  },

  load() {
    const user = wx.getStorageSync('system-user')
    if (!user || !user.id) return

    get('/recharge/selectPage', {
      pageNum: this.data.pageNum,
      pageSize: this.data.pageSize,
      userId: user.id,
      time: this.data.time
    }).then(res => {
      const data = res.data || {}
      const list = data.list || []
      this.setData({
        tableData: this.data.pageNum === 1 ? list : [...this.data.tableData, ...list],
        total: data.total || 0,
        hasMore: this.data.tableData.length + list.length < (data.total || 0)
      })
    })
  },

  loadMore() {
    if (!this.data.hasMore) return
    this.setData({ pageNum: this.data.pageNum + 1 })
    this.load()
  },

  onDateChange(e) {
    this.setData({ time: e.detail.value })
  },

  search() {
    this.setData({ pageNum: 1, tableData: [], hasMore: true })
    this.load()
  },

  reset() {
    this.setData({ time: '', pageNum: 1, tableData: [], hasMore: true })
    this.load()
  },

  showRecharge() {
    this.setData({ showRechargeModal: true, rechargeForm: { money: '1', type: '微信支付' } })
  },

  closeModal() {
    this.setData({ showRechargeModal: false })
  },

  noop() {},

  onMoneyInput(e) {
    this.setData({ 'rechargeForm.money': e.detail.value })
  },

  onPayTypeChange(e) {
    this.setData({ 'rechargeForm.type': e.detail.value })
  },

  submitRecharge() {
    const user = wx.getStorageSync('system-user')
    if (!user || !user.id) return

    const money = parseFloat(this.data.rechargeForm.money)
    if (!money || money <= 0) {
      wx.showToast({ title: '请输入有效金额', icon: 'none' })
      return
    }

    post('/recharge/add', {
      userId: user.id,
      money: money,
      type: this.data.rechargeForm.type
    }).then(res => {
      if (res.code === '200') {
        wx.showToast({ title: '充值成功', icon: 'success' })
        this.setData({ showRechargeModal: false, pageNum: 1, tableData: [], hasMore: true })
        this.load()
        this.loadAccount()
      } else {
        wx.showToast({ title: res.msg || '充值失败', icon: 'none' })
      }
    })
  }
})
