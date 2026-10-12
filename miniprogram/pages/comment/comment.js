const { get, put, del } = require('../../utils/request')
const { preload } = require('../../utils/image')

Page({
  data: {
    tableData: [],
    pageNum: 1,
    pageSize: 10,
    total: 0,
    hasMore: true,
    content: '',
    goodsName: '',
    showEditModal: false,
    editForm: {
      id: null,
      score: 5,
      content: ''
    }
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

    get('/comment/selectPage', {
      pageNum: this.data.pageNum,
      pageSize: this.data.pageSize,
      content: this.data.content,
      goodsName: this.data.goodsName,
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

  onContentInput(e) { this.setData({ content: e.detail.value }) },
  onGoodsNameInput(e) { this.setData({ goodsName: e.detail.value }) },

  search() {
    this.setData({ pageNum: 1, tableData: [], hasMore: true })
    this.load()
  },

  reset() {
    this.setData({ content: '', goodsName: '', pageNum: 1, tableData: [], hasMore: true })
    this.load()
  },

  editComment(e) {
    const item = e.currentTarget.dataset.item
    this.setData({
      showEditModal: true,
      editForm: { id: item.id, score: item.score, content: item.content }
    })
  },

  setScore(e) {
    this.setData({ 'editForm.score': e.currentTarget.dataset.score })
  },

  onEditContentInput(e) {
    this.setData({ 'editForm.content': e.detail.value })
  },

  closeEditModal() {
    this.setData({ showEditModal: false })
  },

  noop() {},

  submitEdit() {
    if (!this.data.editForm.content.trim()) {
      wx.showToast({ title: '请输入评价内容', icon: 'none' })
      return
    }
    put('/comment/update', this.data.editForm).then(res => {
      if (res.code === '200') {
        wx.showToast({ title: '修改成功', icon: 'success' })
        this.setData({ showEditModal: false, pageNum: 1, tableData: [], hasMore: true })
        this.load()
      } else {
        wx.showToast({ title: res.msg || '修改失败', icon: 'none' })
      }
    })
  },

  deleteComment(e) {
    const id = e.currentTarget.dataset.id
    wx.showModal({
      title: '确认删除',
      content: '删除后数据无法恢复，确定删除吗？',
      success: (res) => {
        if (res.confirm) {
          del('/comment/delete/' + id).then(() => {
            wx.showToast({ title: '已删除', icon: 'none' })
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
