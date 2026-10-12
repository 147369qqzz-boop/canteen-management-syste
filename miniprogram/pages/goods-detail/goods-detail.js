const { get, post, del } = require('../../utils/request')
const { preload } = require('../../utils/image')

Page({
  data: {
    id: null,
    goods: {},
    num: 1,
    totalPrice: '0.00',
    commentList: [],
    commentPageNum: 1,
    commentTotal: 0,
    avgScore: '0.0',
    sortBy: 'time',
    userCollect: {},
    showOrderModal: false,
    orderForm: { deliverType: '自提', address: '' }
  },

  onLoad(options) {
    if (!options.id) return
    this.setData({ id: options.id })
    this.loadGoods()
    this.loadCollect()
    this.loadComments()
  },

  calcTotal() {
    const p = (this.data.goods.price || 0) * this.data.num
    this.setData({ totalPrice: p.toFixed(2) })
  },

  loadGoods() {
    get('/goods/selectById/' + this.data.id).then(res => {
      const goods = res.data || {}
      if (goods.img) goods.img = preload([goods], 'img')[0].img
      this.setData({ goods })
      this.calcTotal()
    }).catch(() => {})
  },

  loadCollect() {
    const user = wx.getStorageSync('system-user')
    if (!user || !user.id) return
    get('/collect/selectAll', { goodsId: this.data.id, userId: user.id }).then(res => {
      const list = res.data || []
      this.setData({ userCollect: list.length > 0 ? list[0] : {} })
    }).catch(() => {})
  },

  toggleCollect() {
    const user = wx.getStorageSync('system-user')
    if (!user || !user.id) { wx.navigateTo({ url: '/pages/login/login' }); return }
    if (this.data.userCollect.id) {
      del('/collect/delete/' + this.data.userCollect.id).then(() => {
        wx.showToast({ title: '已取消收藏', icon: 'none' })
        this.loadCollect()
      }).catch(() => {})
    } else {
      post('/collect/add', { goodsId: this.data.id, userId: user.id }).then(() => {
        wx.showToast({ title: '收藏成功', icon: 'none' })
        this.loadCollect()
      }).catch(() => {})
    }
  },

  loadComments() {
    get('/comment/selectPage', {
      pageNum: this.data.commentPageNum,
      pageSize: 10,
      goodsId: this.data.id
    }).then(res => {
      const data = res.data || {}
      let list = preload(data.list || [], 'userAvatar')
      const total = data.total || 0

      // Sort
      if (this.data.sortBy === 'score-desc') list.sort((a, b) => b.score - a.score)
      else if (this.data.sortBy === 'score-asc') list.sort((a, b) => a.score - b.score)

      // Calculate average
      let avg = 0
      if (list.length > 0) {
        avg = (list.reduce((s, c) => s + c.score, 0) / list.length).toFixed(1)
      }
      // Better: calculate from all comments, but we only have the current page
      // Use the page data to estimate

      this.setData({
        commentList: this.data.commentPageNum === 1 ? list : [...this.data.commentList, ...list],
        commentTotal: total,
        avgScore: avg
      })
    }).catch(() => {})
  },

  loadMore() {
    if (this.data.commentList.length >= this.data.commentTotal) return
    this.setData({ commentPageNum: this.data.commentPageNum + 1 })
    this.loadComments()
  },

  setSort(e) {
    this.setData({ sortBy: e.currentTarget.dataset.sort, commentPageNum: 1, commentList: [] })
    this.loadComments()
  },

  minus() { if (this.data.num > 1) { this.setData({ num: this.data.num - 1 }); this.calcTotal() } },
  plus() { this.setData({ num: this.data.num + 1 }); this.calcTotal() },

  addCart() {
    const user = wx.getStorageSync('system-user')
    if (!user || !user.id) { wx.navigateTo({ url: '/pages/login/login' }); return }
    post('/cart/add', { goodsId: this.data.id, num: this.data.num, userId: user.id }).then(res => {
      if (res.code === '200') wx.showToast({ title: '已加入购物车', icon: 'success' })
      else wx.showToast({ title: res.msg || '失败', icon: 'none' })
    }).catch(() => {})
  },

  openBuy() {
    const user = wx.getStorageSync('system-user')
    if (!user || !user.id) { wx.navigateTo({ url: '/pages/login/login' }); return }
    this.setData({ showOrderModal: true, orderForm: { deliverType: '自提', address: '' } })
  },

  closeModal() { this.setData({ showOrderModal: false }) },
  noop() {},
  onAddrInput(e) { this.setData({ 'orderForm.address': e.detail.value }) },

  onDeliverTypeChange(e) {
    // Not used in simplified modal, keep for compat
  },

  submitOrder() {
    const user = wx.getStorageSync('system-user')
    if (!user || !user.id) return
    if (this.data.orderForm.deliverType === '外送' && !this.data.orderForm.address.trim()) {
      wx.showToast({ title: '请输入收货地址', icon: 'none' }); return
    }
    post('/orders/add', {
      userId: user.id,
      cartList: [{ goodsId: this.data.id, num: this.data.num }],
      deliverType: this.data.orderForm.deliverType,
      address: this.data.orderForm.address
    }).then(res => {
      if (res.code === '200') {
        wx.showToast({ title: '下单成功', icon: 'success' })
        this.setData({ showOrderModal: false })
        this.loadGoods()
      } else wx.showToast({ title: res.msg || '下单失败', icon: 'none' })
    }).catch(() => {})
  }
})
