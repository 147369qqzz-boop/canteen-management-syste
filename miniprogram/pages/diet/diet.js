const { get, post, put, del } = require('../../utils/request')

Page({
  data: {
    goal: null,
    mealTypes: ['早餐', '午餐', '晚餐', '加餐'],
    mealIndex: 1,   // 默认午餐
    records: [],
    allTodayRecords: [],
    todaySummary: [],
    hasWarning: false,
    warningTips: '',

    // goal dialog
    showGoalModal: false,
    goalGoalType: '保持',
    goalCalories: 2000,
    goalProtein: 60,
    goalCarbs: 250,
    goalFat: 65,

    // record dialog
    showRecordModal: false,
    goodsOptions: [],
    recGoodsId: null,
    recGoodsName: '',
    recMealIndex: 1,
    recCalories: 0,
    recProtein: 0,
    recCarbs: 0,
    recFat: 0,

    // 近7天
    weekDays: [],
    weekCalories: [],
  },

  onLoad() {
    const user = wx.getStorageSync('system-user')
    if (!user || !user.id) { wx.navigateTo({ url: '/pages/login/login' }); return }
    this.userId = user.id
    this.loadGoal()
    this.loadRecords()
    this.loadGoodsOptions()
    this.loadWeekChart()
  },

  onShow() { wx.stopPullDownRefresh() },

  async loadGoal() {
    try {
      const res = await get('/dietGoal/selectAll', { userId: this.userId })
      if (res.data && res.data.length > 0) {
        const g = res.data[0]
        this.setData({
          goal: g,
          goalGoalType: g.goalType || '保持',
          goalCalories: g.dailyCalories || 2000,
          goalProtein: g.dailyProtein || 60,
          goalCarbs: g.dailyCarbs || 250,
          goalFat: g.dailyFat || 65
        })
      }
    } catch (e) { /* ignore */ }
  },

  async loadRecords() {
    const today = new Date().toISOString().split('T')[0]
    try {
      // 当前餐次
      const mt = this.data.mealTypes[this.data.mealIndex]
      const res = await get('/dietRecord/selectAll', { userId: this.userId, mealType: mt })
      const filtered = (res.data || []).filter(r => r.recordTime && r.recordTime.includes(today))
      this.setData({ records: filtered })
      // 全天
      const all = await get('/dietRecord/selectAll', { userId: this.userId })
      const allToday = (all.data || []).filter(r => r.recordTime && r.recordTime.includes(today))
      this.setData({ allTodayRecords: allToday })
      this.computeSummary()
    } catch (e) { /* ignore */ }
  },

  computeSummary() {
    const goal = this.data.goal || { dailyCalories: 2000, dailyProtein: 60, dailyCarbs: 250, dailyFat: 65 }
    let cal = 0, pro = 0, carb = 0, fat = 0
    for (const r of this.data.allTodayRecords) {
      cal += Number(r.calories || 0)
      pro += Number(r.protein || 0)
      carb += Number(r.carbs || 0)
      fat += Number(r.fat || 0)
    }
    const items = [
      { label: '热量', current: cal, target: Number(goal.dailyCalories), unit: '千卡', over: cal > Number(goal.dailyCalories) },
      { label: '蛋白质', current: pro, target: Number(goal.dailyProtein), unit: 'g', over: pro > Number(goal.dailyProtein) },
      { label: '碳水', current: carb, target: Number(goal.dailyCarbs), unit: 'g', over: carb > Number(goal.dailyCarbs) },
      { label: '脂肪', current: fat, target: Number(goal.dailyFat), unit: 'g', over: fat > Number(goal.dailyFat) },
    ]
    const warns = items.filter(i => i.over)
    let tips = '各项指标均在目标范围内，继续保持！'
    if (cal > Number(goal.dailyCalories)) tips += '热量已超标，建议减少高热量食物。'
    if (pro > Number(goal.dailyProtein)) tips += '蛋白质偏高，可减少肉类。'
    if (carb > Number(goal.dailyCarbs)) tips += '碳水偏高，用粗粮替代精米白面。'
    if (fat > Number(goal.dailyFat)) tips += '脂肪偏高，减少油炸食品。'

    this.setData({
      todaySummary: items,
      hasWarning: warns.length > 0,
      warningTips: tips
    })
  },

  async loadGoodsOptions() {
    try {
      const res = await get('/goods/selectAll')
      this.setData({ goodsOptions: res.data || [] })
    } catch (e) { /* ignore */ }
  },

  async loadWeekChart() {
    const days = []; const calData = []
    try {
      const res = await get('/dietRecord/selectAll', { userId: this.userId })
      const all = res.data || []
      for (let i = 6; i >= 0; i--) {
        const d = new Date(); d.setDate(d.getDate() - i)
        const ds = d.toISOString().split('T')[0]
        days.push(ds.substring(5))
        let sum = 0
        for (const r of all) {
          if (r.recordTime && r.recordTime.includes(ds)) sum += Number(r.calories || 0)
        }
        calData.push(sum)
      }
      this.setData({ weekDays: days, weekCalories: calData })
    } catch (e) { /* ignore */ }
  },

  onMealChange(e) {
    this.setData({ mealIndex: parseInt(e.detail.value) })
    this.loadRecords()
  },

  // --- Goal Modal ---
  showGoal() {
    const g = this.data.goal || {}
    this.setData({
      showGoalModal: true,
      goalGoalType: g.goalType || '保持',
      goalCalories: g.dailyCalories || 2000,
      goalProtein: g.dailyProtein || 60,
      goalCarbs: g.dailyCarbs || 250,
      goalFat: g.dailyFat || 65
    })
  },
  closeGoal() { this.setData({ showGoalModal: false }) },
  onGoalType(e) { this.setData({ goalGoalType: ['减脂', '增肌', '保持'][parseInt(e.detail.value)] }) },
  onGoalInput(e) { const f = e.currentTarget.dataset.field; this.setData({ [f]: e.detail.value }) },

  async saveGoal() {
    const payload = {
      userId: this.userId,
      goalType: this.data.goalGoalType,
      dailyCalories: this.data.goalCalories,
      dailyProtein: this.data.goalProtein,
      dailyCarbs: this.data.goalCarbs,
      dailyFat: this.data.goalFat
    }
    try {
      if (this.data.goal && this.data.goal.id) {
        payload.id = this.data.goal.id
        await put('/dietGoal/update', payload)
      } else {
        await post('/dietGoal/add', payload)
      }
      wx.showToast({ title: '已保存', icon: 'success' })
      this.setData({ showGoalModal: false })
      this.loadGoal()
    } catch (e) { /* ignore */ }
  },

  // --- Record Modal ---
  showRecord() {
    this.setData({
      showRecordModal: true,
      recGoodsId: null,
      recGoodsName: '',
      recCalories: 0, recProtein: 0, recCarbs: 0, recFat: 0
    })
  },
  closeRecord() { this.setData({ showRecordModal: false, recGoodsId: null }) },
  onRecMeal(e) { this.setData({ recMealIndex: parseInt(e.detail.value) }) },
  onRecGoods(e) {
    const idx = parseInt(e.detail.value)
    if (idx < 0) { this.setData({ recGoodsId: null }); return }
    const g = this.data.goodsOptions[idx]
    this.setData({
      recGoodsId: g.id,
      recGoodsName: g.name,
      recCalories: g.calories || 0,
      recProtein: g.protein || 0,
      recCarbs: g.carbs || 0,
      recFat: g.fat || 0,
    })
  },
  async addRecord() {
    if (!this.data.recGoodsId) { wx.showToast({ title: '请选择菜品', icon: 'none' }); return }
    try {
      await post('/dietRecord/add', {
        userId: this.userId,
        goodsId: this.data.recGoodsId,
        goodsName: this.data.recGoodsName,
        mealType: this.data.mealTypes[this.data.recMealIndex],
        calories: this.data.recCalories,
        protein: this.data.recProtein,
        carbs: this.data.recCarbs,
        fat: this.data.recFat
      })
      wx.showToast({ title: '已添加', icon: 'success' })
      this.setData({ showRecordModal: false })
      this.loadRecords()
      this.loadWeekChart()
    } catch (e) { /* ignore */ }
  },

  async deleteRecord(e) {
    const id = e.currentTarget.dataset.id
    try {
      const res = await new Promise((resolve) => {
        wx.showModal({ title: '提示', content: '确定删除？', success: resolve })
      })
      if (!res.confirm) return
      await del('/dietRecord/delete/' + id)
      wx.showToast({ title: '已删除', icon: 'success' })
      this.loadRecords()
      this.loadWeekChart()
    } catch (e) { /* ignore */ }
  },

  formatTime(t) { return t ? t.replace('T', ' ') : '' },
  noop() {}
})
