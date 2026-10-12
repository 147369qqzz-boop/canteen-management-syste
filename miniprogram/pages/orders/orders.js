const { get, post, put } = require('../../utils/request')
const { preload } = require('../../utils/image')

const STATUS_MAP = {
  '待接单': 'pending',
  '已配送': 'delivering',
  '已出货': 'shipped',
  '已完成': 'completed',
  '已取消': 'cancelled'
}

Page({
  data: {
    tableData: [],
    pageNum: 1,
    pageSize: 3,
    total: 0,
    loading: false,
    hasMore: true,
    orderNo: '',
    goodsName: '',
    showCommentModal: false,
    commentForm: {
      orderId: null,
      userId: null,
      score: 5,
      content: '',
      tags: []
    },
    quickTags: ['味道好', '分量足', '性价比高', '出餐快', '卖相好', '还会回购'],
    scoreHint: '非常满意！'
  },

  onShow() {
    this.load()
  },

  onOrderNoInput(e) { this.setData({ orderNo: e.detail.value }) },
  onGoodsNameInput(e) { this.setData({ goodsName: e.detail.value }) },

  search() {
    this.setData({ pageNum: 1, tableData: [], hasMore: true })
    this.load()
  },

  reset() {
    this.setData({ orderNo: '', goodsName: '', pageNum: 1, tableData: [], hasMore: true })
    this.load()
  },

  load() {
    const user = wx.getStorageSync('system-user')
    if (!user || !user.id) {
      wx.navigateTo({ url: '/pages/login/login' })
      return
    }

    if (this.data.loading) return
    this.setData({ loading: true })

    get('/orders/selectPage', {
      pageNum: this.data.pageNum,
      pageSize: this.data.pageSize,
      userId: user.id,
      orderNo: this.data.orderNo,
      goodsName: this.data.goodsName
    }).then(res => {
      const data = res.data || {}
      const rawList = data.list || []
      // Preload detail images
      for (const order of rawList) {
        if (order.orderDetailList) {
          order.orderDetailList = preload(order.orderDetailList, 'goodsImg')
        }
      }
      const list = rawList.map(item => ({
        ...item,
        statusClass: STATUS_MAP[item.status] || '',
        orderDetailList: (item.orderDetailList || []).map(d => ({
          ...d,
          subtotal: (d.goodsPrice * d.num).toFixed(2)
        }))
      }))
      this.setData({
        tableData: this.data.pageNum === 1 ? list : [...this.data.tableData, ...list],
        total: data.total || 0,
        hasMore: this.data.tableData.length + list.length < (data.total || 0),
        loading: false
      })
    }).catch(() => {
      this.setData({ loading: false })
    })
  },

  loadMore() {
    if (!this.data.hasMore || this.data.loading) return
    this.setData({ pageNum: this.data.pageNum + 1 })
    this.load()
  },

  cancelOrder(e) {
    const item = e.currentTarget.dataset.item
    wx.showModal({
      title: '确认取消',
      content: '确定要取消该订单吗？',
      success: (res) => {
        if (res.confirm) {
          put('/orders/update', { ...item, status: '已取消' }).then(() => {
            wx.showToast({ title: '已取消', icon: 'none' })
            this.setData({ pageNum: 1, tableData: [], hasMore: true })
            this.load()
          })
        }
      }
    })
  },

  doneOrder(e) {
    const item = e.currentTarget.dataset.item
    wx.showModal({
      title: '确认收货',
      content: '确定已经收到货了吗？',
      success: (res) => {
        if (res.confirm) {
          put('/orders/update', { ...item, status: '已完成' }).then(() => {
            wx.showToast({ title: '已确认收货', icon: 'none' })
            this.setData({ pageNum: 1, tableData: [], hasMore: true })
            this.load()
          })
        }
      }
    })
  },

  addComment(e) {
    const detail = e.currentTarget.dataset.item
    const orderId = e.currentTarget.dataset.orderId
    const user = wx.getStorageSync('system-user')

    get('/comment/selectAll', {
      orderId: detail.id,
      userId: user.id
    }).then(res => {
      const list = res.data || []
      const exist = list.length > 0 ? list[0] : null
      this.setData({
        showCommentModal: true,
        commentForm: {
          id: exist ? exist.id : null,
          orderId: detail.id,
          userId: user.id,
          score: exist ? exist.score : 5,
          content: exist ? exist.content : '',
          tags: exist && exist.tags ? exist.tags : []
        }
      })
      this.updateScoreHint(exist ? exist.score : 5)
    })
  },

  updateScoreHint(score) {
    const hints = { 1: '非常失望', 2: '比较一般', 3: '还可以', 4: '很不错', 5: '非常满意！' }
    this.setData({ scoreHint: hints[score] || '' })
  },

  setScore(e) {
    const score = e.currentTarget.dataset.score
    this.setData({ 'commentForm.score': score })
    this.updateScoreHint(score)
  },

  toggleTag(e) {
    const tag = e.currentTarget.dataset.tag
    let tags = [...this.data.commentForm.tags]
    const idx = tags.indexOf(tag)
    if (idx > -1) tags.splice(idx, 1)
    else if (tags.length < 3) tags.push(tag)
    else { wx.showToast({ title: '最多选3个标签', icon: 'none' }); return }
    this.setData({ 'commentForm.tags': tags })
  },

  onCommentInput(e) {
    this.setData({ 'commentForm.content': e.detail.value })
  },

  closeCommentModal() {
    this.setData({ showCommentModal: false })
  },

  noop() {},

  submitComment() {
    const form = this.data.commentForm
    if (!form.content.trim()) {
      wx.showToast({ title: '请输入评价内容', icon: 'none' })
      return
    }

    // Merge tags into content
    let content = form.content.trim()
    if (form.tags.length > 0) {
      content = '【' + form.tags.join('·') + '】' + content
    }

    const apiCall = form.id ? put : post
    const url = form.id ? '/comment/update' : '/comment/add'

    apiCall(url, { ...form, content }).then(res => {
      if (res.code === '200') {
        wx.showToast({ title: '评价成功', icon: 'success' })
        this.setData({ showCommentModal: false })
      } else {
        wx.showToast({ title: res.msg || '操作失败', icon: 'none' })
      }
    })
  }
})
