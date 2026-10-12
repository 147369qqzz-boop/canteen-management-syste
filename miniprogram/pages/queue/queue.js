const { get } = require('../../utils/request')

Page({
  data: {
    now: '',
    canteenList: [],
    canteenIndex: -1,
    stalls: [],
    loading: true,
    autoRefresh: true
  },
  timer: null,

  onLoad() {
    this.updateClock()
    this.clockTimer = setInterval(() => this.updateClock(), 1000)
    this.loadCanteens()
    this.loadData()
    this.startAutoRefresh()
  },

  onUnload() {
    if (this.clockTimer) clearInterval(this.clockTimer)
    if (this.timer) clearInterval(this.timer)
  },

  onShow() {
    wx.stopPullDownRefresh()
  },

  onPullDownRefresh() {
    this.loadData()
  },

  updateClock() {
    const d = new Date()
    this.setData({ now: d.toLocaleTimeString('zh-CN', { hour12: false }) })
  },

  async loadCanteens() {
    try {
      const res = await get('/canteen/selectAll')
      this.setData({ canteenList: res.data || [] })
    } catch (e) { /* ignore */ }
  },

  onCanteenChange(e) {
    this.setData({ canteenIndex: parseInt(e.detail.value) })
    this.loadData()
  },

  async loadData() {
    this.setData({ loading: true })
    try {
      const params = {}
      if (this.data.canteenIndex >= 0) {
        params.canteenId = this.data.canteenList[this.data.canteenIndex].id
      }
      const res = await get('/business/queueOverview', params)
      let stalls = res.data || []
      stalls.sort((a, b) => {
        if (a.operateStatus === '休息中' && b.operateStatus !== '休息中') return 1
        if (a.operateStatus !== '休息中' && b.operateStatus === '休息中') return -1
        return (b.queueCount || 0) - (a.queueCount || 0)
      })
      this.setData({ stalls, loading: false })
    } catch (e) {
      this.setData({ loading: false })
    }
  },

  startAutoRefresh() {
    this.timer = setInterval(() => this.loadData(), 15000)
  },

  toggleAuto() {
    const next = !this.data.autoRefresh
    this.setData({ autoRefresh: next })
    if (next) {
      this.startAutoRefresh()
    } else if (this.timer) {
      clearInterval(this.timer)
      this.timer = null
    }
  },

  estimateWait(stall) {
    const count = stall.queueCount || 0
    const perMeal = stall.estimatedCookingTime || 10
    return Math.ceil(count * perMeal / 5)
  },

  barWidth(stall) {
    const wait = this.estimateWait(stall)
    return Math.min(100, wait * 5)
  },

  barClass(stall) {
    const wait = this.estimateWait(stall)
    if (wait > 20) return 'bar-d'
    if (wait > 10) return 'bar-w'
    return 'bar-o'
  },

  queueHint(stall) {
    if (stall.operateStatus === '休息中') return '当前休息中'
    const count = stall.queueCount || 0
    if (count === 0) return '无需排队'
    if (count < 5) return '排队较少'
    if (count < 15) return '排队适中'
    return '排队较多'
  }
})
