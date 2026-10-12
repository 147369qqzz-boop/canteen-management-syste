const { get } = require('../../utils/request')
const { preload } = require('../../utils/image')

Page({
  data: {
    carouselList: [],
    hotGoods: [],
    newGoods: [],
    recommendGoods: [],
    flashGoods: [],
    categories: [
      { id: null, name: '全部', icon: '🍽️' },
      { id: null, name: '主食', icon: '🍚' },
      { id: null, name: '小吃', icon: '🍟' },
      { id: null, name: '饮品', icon: '🥤' },
      { id: null, name: '甜品', icon: '🍰' },
      { id: null, name: '套餐', icon: '🍱' },
      { id: null, name: '汤煲', icon: '🍲' },
      { id: null, name: '粉面', icon: '🍜' },
    ],
    noticeText: '',
    countdown: { h: '00', m: '00', s: '00' },
    countdownTimer: null
  },

  onLoad() {
    this.loadData()
    this.startCountdown()
  },

  onShow() {
    wx.stopPullDownRefresh()
  },

  onPullDownRefresh() {
    this.loadData()
  },

  loadData() {
    // 轮播图
    get('/carousel/selectAll').then(res => {
      const list = preload(res.data || [], 'img')
      this.setData({ carouselList: list })
    }).catch(() => {})

    // 热销 TOP 6
    get('/goods/selectAll', { status: '上架' }).then(res => {
      const all = (res.data || []).sort((a, b) => b.saleCount - a.saleCount)
      const hot = preload(all.slice(0, 6), 'img')
      const flash = preload(all.slice(0, 5), 'img')
      this.setData({ hotGoods: hot, flashGoods: flash })
    }).catch(() => {})

    // 最新上架 取8个
    get('/goods/selectAll', { status: '上架' }).then(res => {
      const list = preload((res.data || []).slice(0, 8), 'img')
      this.setData({ newGoods: list })
    }).catch(() => {})

    // 推荐菜品 取6个
    get('/goods/selectAll', { status: '上架' }).then(res => {
      const list = preload(
        (res.data || []).filter(v => v.recommend === '是').slice(0, 6),
        'img'
      )
      this.setData({ recommendGoods: list })
    }).catch(() => {})

    // 公告
    const now = new Date()
    const hours = now.getHours()
    let notice = ''
    if (hours < 9) notice = '早餐供应中 🥐 7:00 - 9:00 | 热粥豆浆新鲜出炉'
    else if (hours < 14) notice = '午餐供应中 🍱 11:00 - 13:30 | 今日特色：宫保鸡丁、红烧肉限量供应'
    else if (hours < 20) notice = '晚餐供应中 🍲 17:00 - 19:30 | 晚间特供暖心小火锅'
    else notice = '食堂已打烊 🌙 明天 7:00 相约食堂，不见不散'
    this.setData({ noticeText: notice })

    // 动态加载分类
    get('/category/selectAll').then(res => {
      const cats = (res.data || []).slice(0, 8)
      if (cats.length > 0) {
        const icons = ['🍽️', '🍚', '🍟', '🥤', '🍰', '🍱', '🍲', '🍜']
        this.setData({
          categories: cats.map((c, i) => ({ ...c, icon: icons[i] || '🍽️' }))
        })
      }
    }).catch(() => {})
  },

  startCountdown() {
    const update = () => {
      const now = new Date()
      const end = new Date(now)
      end.setHours(23, 59, 59, 0)
      const diff = Math.max(0, (end - now) / 1000)
      const h = Math.floor(diff / 3600)
      const m = Math.floor((diff % 3600) / 60)
      const s = Math.floor(diff % 60)
      this.setData({
        countdown: {
          h: String(h).padStart(2, '0'),
          m: String(m).padStart(2, '0'),
          s: String(s).padStart(2, '0')
        }
      })
    }
    update()
    const timer = setInterval(update, 1000)
    this.setData({ countdownTimer: timer })
  },

  onUnload() {
    if (this.data.countdownTimer) clearInterval(this.data.countdownTimer)
  },

  goDetail(e) {
    const id = e.currentTarget.dataset.id
    if (id) wx.navigateTo({ url: '/pages/goods-detail/goods-detail?id=' + id })
  },

  goCategory(e) {
    const app = getApp()
    app.globalData.categoryFilter = e.currentTarget.dataset.id || null
    wx.switchTab({ url: '/pages/goods/goods' })
  },

  goGoods() {
    wx.switchTab({ url: '/pages/goods/goods' })
  }
})
