const { get } = require('../../utils/request')

Page({
  data: {
    canteenList: [],
    canteenIndex: -1,
    floorIndex: -1,
    floorList: [],
    filterStatus: 'all',
    stalls: [],
    loading: true
  },

  onLoad() {
    this.loadCanteens()
    this.loadStalls()
  },

  onShow() {
    wx.stopPullDownRefresh()
  },

  onPullDownRefresh() {
    this.setData({ canteenIndex: -1, floorIndex: -1, filterStatus: 'all' })
    this.loadStalls()
  },

  async loadCanteens() {
    try {
      const res = await get('/canteen/selectAll')
      this.setData({ canteenList: res.data || [] })
    } catch (e) { /* ignore */ }
  },

  onCanteenChange(e) {
    const idx = parseInt(e.detail.value)
    const c = this.data.canteenList[idx]
    let floors = []
    if (c && c.floors) {
      floors = c.floors.split(',').map(f => f.trim()).filter(f => f)
    }
    this.setData({ canteenIndex: idx, floorIndex: -1, floorList: floors })
    this.loadStalls()
  },

  onFloorChange(e) {
    this.setData({ floorIndex: parseInt(e.detail.value) })
    this.loadStalls()
  },

  toggleStatus() {
    const next = this.data.filterStatus === 'all' ? '营业中' : 'all'
    this.setData({ filterStatus: next })
    this.loadStalls()
  },

  async loadStalls() {
    this.setData({ loading: true })
    try {
      const params = { filterStatus: this.data.filterStatus }
      if (this.data.canteenIndex >= 0) {
        params.canteenId = this.data.canteenList[this.data.canteenIndex].id
      }
      if (this.data.floorIndex >= 0 && this.data.floorList.length > 0) {
        params.floor = this.data.floorList[this.data.floorIndex]
      }
      // 使用聚合接口，一次请求获取档口+菜品+评分
      const res = await get('/business/stallsDetail', params)
      this.setData({ stalls: res.data || [], loading: false })
    } catch (e) {
      this.setData({ loading: false })
    }
  },

  estimateWait(stall) {
    const count = stall.queueCount || 0
    const perMeal = stall.estimatedCookingTime || 10
    return Math.ceil(count * perMeal / 5)
  },

  goGoodsDetail(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: '/pages/goods-detail/goods-detail?id=' + id })
  },

  goStallGoods(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: '/pages/goods/goods?businessId=' + id })
  }
})
