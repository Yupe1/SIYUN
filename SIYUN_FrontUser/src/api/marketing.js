import { request } from '@/utils/request'

export function getHomeAds() {
  return request({
    url: '/siyun/ads',
    method: 'GET',
  })
}
