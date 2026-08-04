import { request } from '@/utils/request'

export function getGoods(keywords = '', cateId = null) {
  return request({
    url: '/siyun/goods',
    method: 'GET',
    data: {
      keywords,
      ...(cateId ? { cateId } : {}),
    },
  })
}

export function getGoodsDetail(id) {
  return request({
    url: `/siyun/goods/${id}`,
    method: 'GET',
  })
}

export function getGoodsCategories() {
  return request({
    url: '/siyun/goods-categories',
    method: 'GET',
  })
}

export function createGoodsOrder(data) {
  return request({
    url: '/siyun/goods/order',
    method: 'POST',
    data,
    loading: true,
  })
}

export function getAvailableCoupons(targetType, targetId, quantity = 1) {
  return request({
    url: '/siyun/coupons/available',
    method: 'GET',
    data: {
      targetType,
      targetId,
      quantity,
    },
  })
}

export function getMyOrders() {
  return request({
    url: '/siyun/orders',
    method: 'GET',
  })
}
