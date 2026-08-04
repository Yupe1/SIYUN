export function buildShareLink(path) {
  const normalizedPath = path.startsWith('/') ? path : `/${path}`

  // #ifdef H5
  const base = `${window.location.origin}${window.location.pathname}${window.location.search}`
  return `${base}#${normalizedPath}`
  // #endif

  // #ifndef H5
  return normalizedPath
  // #endif
}

export function showShareDialog(path, title = '分享链接') {
  const link = buildShareLink(path)
  uni.showModal({
    title,
    content: link,
    confirmText: '复制链接',
    cancelText: '关闭',
    success: (result) => {
      if (!result.confirm) return
      uni.setClipboardData({
        data: link,
        success: () => uni.showToast({ title: '链接已复制', icon: 'success' }),
      })
    },
  })
  return link
}
