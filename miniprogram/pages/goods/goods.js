const { get, post } = require('../../utils/request')
const { preload } = require('../../utils/image')

Page({
  data: {
    keyword: '',
    categoryId: null,
    businessId: null,
    categoryList: [],
    sortType: 'default',
    sortOrder: 'desc',
    list: [],
    pageNum: 1,
    pageSize: 8,
    total: 0,
    loading: false,
    hasMore: true
  },

  onLoad(options) {
    if (options.name) this.setData({ keyword: options.name })
    if (options.categoryId) this.setData({ categoryId: Number(options.categoryId) || null })
    if (options.businessId) this.setData({ businessId: Number(options.businessId) || null })
    get('/category/selectAll').then(res => this.setData({ categoryList: res.data || [] })).catch(() => {})
    this.load()
  },

  onShow() {
    const app = getApp()
    if (app.globalData.categoryFilter !== undefined) {
      const id = app.globalData.categoryFilter
      app.globalData.categoryFilter = undefined
      this.setData({ categoryId: id, pageNum: 1, list: [], hasMore: true })
      this.load()
    }
    wx.stopPullDownRefresh()
  },
  onPullDownRefresh() { this.setData({ pageNum: 1, list: [], hasMore: true }); this.load() },

  onInput(e) { this.setData({ keyword: e.detail.value }) },
  doSearch() { this.setData({ pageNum: 1, list: [], hasMore: true }); this.load() },

  pickCate(e) {
    this.setData({
      categoryId: e.currentTarget.dataset.id || null,
      pageNum: 1, list: [], hasMore: true
    })
    this.load()
  },

  setSort(e) {
    const type = e.currentTarget.dataset.type
    let order = 'desc'
    if (type === 'price' && this.data.sortType === 'price') {
      order = this.data.sortOrder === 'desc' ? 'asc' : 'desc'
    }
    this.setData({ sortType: type, sortOrder: order, pageNum: 1, list: [], hasMore: true })
    this.load()
  },

  load() {
    if (this.data.loading) return
    this.setData({ loading: true })
    get('/goods/selectPage', {
      pageNum: this.data.pageNum,
      pageSize: this.data.pageSize,
      status: '上架',
      name: this.data.keyword,
      categoryId: this.data.categoryId,
      businessId: this.data.businessId
    }).then(res => {
      const data = res.data || {}
      let items = preload(data.list || [], 'img')
      // 前端排序
      if (this.data.sortType === 'sales') {
        items.sort((a, b) => this.data.sortOrder === 'desc' ? b.saleCount - a.saleCount : a.saleCount - b.saleCount)
      } else if (this.data.sortType === 'price') {
        items.sort((a, b) => this.data.sortOrder === 'desc' ? b.price - a.price : a.price - b.price)
      }
      this.setData({
        list: this.data.pageNum === 1 ? items : [...this.data.list, ...items],
        total: data.total || 0,
        hasMore: (this.data.list.length + items.length) < (data.total || 0),
        loading: false
      })
    }).catch(() => this.setData({ loading: false }))
  },

  loadMore() {
    if (!this.data.hasMore || this.data.loading) return
    this.setData({ pageNum: this.data.pageNum + 1 })
    this.load()
  },

  goDetail(e) {
    wx.navigateTo({ url: '/pages/goods-detail/goods-detail?id=' + e.currentTarget.dataset.id })
  },

  quickBuy(e) {
    const user = wx.getStorageSync('system-user')
    if (!user || !user.id) { wx.navigateTo({ url: '/pages/login/login' }); return }
    const id = e.currentTarget.dataset.id
    post('/cart/add', { goodsId: id, num: 1, userId: user.id }).then(res => {
      if (res.code === '200') wx.showToast({ title: '已加入购物车', icon: 'success' })
      else wx.showToast({ title: res.msg || '失败', icon: 'none' })
    })
  }
})
