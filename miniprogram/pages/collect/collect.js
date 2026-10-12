const { get, del } = require('../../utils/request')
const { preload } = require('../../utils/image')

Page({
  data: {
    tableData: [],
    pageNum: 1,
    pageSize: 10,
    total: 0,
    hasMore: true
  },

  onShow() {
    this.setData({ pageNum: 1, tableData: [], hasMore: true })
    this.load()
  },

  load() {
    const user = wx.getStorageSync('system-user')
    if (!user || !user.id) {
      wx.navigateTo({ url: '/pages/login/login' })
      return
    }

    get('/collect/selectPage', {
      pageNum: this.data.pageNum,
      pageSize: this.data.pageSize,
      userId: user.id
    }).then(res => {
      const data = res.data || {}
      const list = preload(data.list || [], 'goodsImg')
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

  cancelCollect(e) {
    const id = e.currentTarget.dataset.id
    wx.showModal({
      title: '确认取消',
      content: '确定取消收藏吗？',
      success: (res) => {
        if (res.confirm) {
          del('/collect/delete/' + id).then(() => {
            wx.showToast({ title: '已取消', icon: 'none' })
            this.setData({ pageNum: 1, tableData: [], hasMore: true })
            this.load()
          })
        }
      }
    })
  },

  goDetail(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: '/pages/goods-detail/goods-detail?id=' + id })
  }
})
