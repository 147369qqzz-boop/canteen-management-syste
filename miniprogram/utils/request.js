const BASE_URL = 'http://10.144.174.167:8080'

// 替换所有图片 URL 中的 localhost → 实际 IP
function fixImgUrls(obj) {
  if (!obj || typeof obj !== 'object') return obj
  if (Array.isArray(obj)) {
    obj.forEach(item => fixImgUrls(item))
    return obj
  }
  for (const key of Object.keys(obj)) {
    const val = obj[key]
    if (typeof val === 'string' && val.startsWith('http://localhost:8080')) {
      obj[key] = val.replace('http://localhost:8080', BASE_URL)
    } else if (typeof val === 'object') {
      fixImgUrls(val)
    }
  }
  return obj
}

const request = (url, options = {}) => {
  const { method = 'GET', data = {}, header = {} } = options

  return new Promise((resolve, reject) => {
    wx.request({
      url: BASE_URL + url,
      method,
      data,
      header: { 'Content-Type': 'application/json;charset=utf-8', ...header },
      timeout: 10000,
      success(res) {
        if (res.statusCode === 200) {
          const result = fixImgUrls(res.data)
          if (result.code === '401') {
            wx.showToast({ title: result.msg || '请重新登录', icon: 'none' })
            wx.removeStorageSync('system-user')
            wx.navigateTo({ url: '/pages/login/login' })
            reject(result)
            return
          }
          resolve(result)
        } else {
          wx.showToast({ title: '服务器错误(' + res.statusCode + ')', icon: 'none' })
          reject(res)
        }
      },
      fail(err) {
        console.error('Request failed:', url, err.errMsg)
        if (err.errMsg && err.errMsg.includes('timeout')) {
          wx.showToast({ title: '网络超时，请确认后端已启动', icon: 'none', duration: 2500 })
        }
        reject(err)
      }
    })
  })
}

const get = (url, params = {}) => {
  const parts = Object.keys(params)
    .filter(k => params[k] !== null && params[k] !== undefined && params[k] !== '')
    .map(k => k + '=' + encodeURIComponent(params[k]))
  const fullUrl = parts.length ? url + '?' + parts.join('&') : url
  return request(fullUrl, { method: 'GET' })
}

const post = (url, data = {}) => request(url, { method: 'POST', data })
const put = (url, data = {}) => request(url, { method: 'PUT', data })
const del = (url) => request(url, { method: 'DELETE' })

const upload = (filePath) => {
  return new Promise((resolve, reject) => {
    wx.uploadFile({
      url: BASE_URL + '/files/upload',
      filePath,
      name: 'file',
      timeout: 15000,
      success(res) {
        try {
          const data = JSON.parse(res.data)
          if (data.code === '200') resolve(data.data)
          else { wx.showToast({ title: data.msg || '上传失败', icon: 'none' }); reject(data) }
        } catch (e) {
          wx.showToast({ title: '上传失败', icon: 'none' }); reject(res)
        }
      },
      fail(err) {
        wx.showToast({ title: '上传失败', icon: 'none' }); reject(err)
      }
    })
  })
}

module.exports = { get, post, put, del, upload, BASE_URL }
