const BALANCE_NOT_ENOUGH = 1014

export function redirectForRecharge(error, returnUrl = '') {
  if (Number(error?.code) !== BALANCE_NOT_ENOUGH
    && !String(error?.message || '').includes('余额不足')) {
    return false
  }
  uni.showToast({ title: '余额不足，请先充值', icon: 'none' })
  const query = returnUrl ? `?returnUrl=${encodeURIComponent(returnUrl)}` : ''
  setTimeout(() => {
    uni.navigateTo({ url: `/pages/mine/wallet${query}` })
  }, 450)
  return true
}
