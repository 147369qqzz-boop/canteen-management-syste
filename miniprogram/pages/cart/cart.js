const { get, post, put, del } = require('../../utils/request')
const { preload } = require('../../utils/image')

Page({
  data: {
    tableData: [],
    total: 0,
    allChecked: false,
    showOrderModal: false,
    orderForm: {
      deliverType: '自提',
      address: ''
    }
  },

  onShow() {
    this.load()
  },

  load() {
    const user = wx.getStorageSync('system-user')
    if (!user || !user.id) {
      wx.navigateTo({ url: '/pages/login/login' })
      return
    }
    get('/cart/selectAll', { userId: user.id }).then(res => {
      const list = preload(res.data || [], 'goodsImg')
      const items = list.map(item => ({ ...item, checked: false }))
      this.setData({ tableData: items })
      this.calTotal()
    })
  },

  toggleCheck(e) {
    const index = e.currentTarget.dataset.index
    const key = 'tableData[' + index + '].checked'
    this.setData({ [key]: !this.data.tableData[index].checked })
    this.calTotal()
  },

  toggleAll() {
    const allChecked = !this.data.allChecked
    const tableData = this.data.tableData.map(item => ({ ...item, checked: allChecked }))
    this.setData({ tableData, allChecked })
    this.calTotal()
  },

  changeNum(e) {
    const { index, delta } = e.currentTarget.dataset
    const item = this.data.tableData[index]
    const newNum = item.num + delta
    if (newNum < 1) return

    const key = 'tableData[' + index + '].num'
    this.setData({ [key]: newNum })

    put('/cart/update', { ...item, num: newNum }).then(() => {
      this.calTotal()
    })
  },

  deleteItem(e) {
    const id = e.currentTarget.dataset.id
    wx.showModal({
      title: '确认删除',
      content: '确定要删除该菜品吗？',
      success: (res) => {
        if (res.confirm) {
          del('/cart/delete/' + id).then(() => {
            wx.showToast({ title: '已删除', icon: 'none' })
            this.load()
          })
        }
      }
    })
  },

  calTotal() {
    const checked = this.data.tableData.filter(item => item.checked)
    let total = 0
    checked.forEach(item => {
      total += item.goodsPrice * item.num
    })
    this.setData({
      total: parseFloat(total.toFixed(2)),
      allChecked: checked.length === this.data.tableData.length && this.data.tableData.length > 0
    })
  },

  checkout() {
    if (this.data.total === 0) {
      wx.showToast({ title: '请选择菜品', icon: 'none' })
      return
    }
    this.setData({ showOrderModal: true, orderForm: { deliverType: '自提', address: '' } })
  },

  closeModal() {
    this.setData({ showOrderModal: false })
  },

  noop() {},

  onDeliverTypeChange(e) {
    this.setData({ 'orderForm.deliverType': e.detail.value })
  },

  onAddressInput(e) {
    this.setData({ 'orderForm.address': e.detail.value })
  },

  submitOrder() {
    const user = wx.getStorageSync('system-user')
    if (!user || !user.id) return

    if (this.data.orderForm.deliverType === '外送' && !this.data.orderForm.address.trim()) {
      wx.showToast({ title: '请输入收货地址', icon: 'none' })
      return
    }

    const selected = this.data.tableData.filter(item => item.checked)
    post('/orders/add', {
      userId: user.id,
      cartList: selected,
      deliverType: this.data.orderForm.deliverType,
      address: this.data.orderForm.address
    }).then(res => {
      if (res.code === '200') {
        wx.showToast({ title: '下单成功', icon: 'success' })
        this.setData({ showOrderModal: false })
        this.load()
      } else {
        wx.showToast({ title: res.msg || '下单失败', icon: 'none' })
      }
    })
  },

  goGoods() {
    wx.switchTab({ url: '/pages/goods/goods' })
  }
})
