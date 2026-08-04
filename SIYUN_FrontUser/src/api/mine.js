import { request, uploadFile } from '@/utils/request'

export function getMineOverview() {
  return request({ url: '/siyun/mine/overview', method: 'GET' })
}

export function getWallet() {
  return request({ url: '/siyun/wallet', method: 'GET' })
}

export function rechargeWallet(amount) {
  return request({
    url: '/siyun/wallet/recharge',
    method: 'POST',
    data: { amount },
    loading: true,
  })
}

export function getStudyHistory() {
  return request({ url: '/siyun/study-history', method: 'GET' })
}

export function getMyCoupons() {
  return request({ url: '/siyun/coupons', method: 'GET' })
}

export function getFeedback() {
  return request({ url: '/siyun/feedback', method: 'GET' })
}

export function submitFeedback(data) {
  return request({
    url: '/siyun/feedback',
    method: 'POST',
    data,
    loading: true,
  })
}

export function getServiceMessages() {
  return request({ url: '/siyun/service/messages', method: 'GET' })
}

export function sendServiceMessage(content) {
  return request({
    url: '/siyun/service/messages',
    method: 'POST',
    data: { content },
  })
}

export function uploadCreatorVideo(filePath, onProgress) {
  return uploadFile({
    url: '/siyun/upload/video',
    filePath,
    onProgress,
  })
}
