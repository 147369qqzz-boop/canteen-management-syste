var cache = {}

function download(url) {
  if (!url) return url
  if (url.startsWith('https://') || url.startsWith('wxfile://')) return url
  if (cache[url]) return cache[url]

  wx.downloadFile({
    url: url,
    success: function (res) {
      if (res.statusCode === 200) cache[url] = res.tempFilePath
    }
  })
  return url
}

function preload(list, fields) {
  if (!list || !list.length) return list
  if (typeof fields === 'string') fields = [fields]
  list.forEach(function (item) {
    fields.forEach(function (field) {
      if (item[field]) item[field] = download(item[field])
    })
  })
  return list
}

module.exports = { download: download, preload: preload }
