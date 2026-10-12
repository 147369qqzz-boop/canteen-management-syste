Component({
  externalClasses: ['img-class'],
  properties: {
    src: { type: String, value: '', observer: 'load' },
    mode: { type: String, value: 'aspectFill' }
  },
  data: { localSrc: '' },
  methods: {
    load(url) {
      if (!url) { this.setData({ localSrc: '' }); return }
      if (url.startsWith('https://') || url.startsWith('wxfile://') || url.startsWith('http://tmp/')) {
        this.setData({ localSrc: url }); return
      }
      const that = this
      wx.downloadFile({
        url,
        success(res) {
          if (res.statusCode === 200) that.setData({ localSrc: res.tempFilePath })
        },
        fail() { that.setData({ localSrc: url }) }
      })
    }
  }
})
