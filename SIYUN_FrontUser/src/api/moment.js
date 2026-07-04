import { request } from '@/utils/request'

export function getMoment(id) {
  return request({
    url: `/siyun/moment/${id}`,
    method: 'GET',
  })
}

export function searchMoments(keywords = '') {
  return request({
    url: '/siyun/moments',
    method: 'GET',
    data: {
      keywords,
    },
  })
}

export function getMyMoments() {
  return request({
    url: '/siyun/myMoments',
    method: 'GET',
  })
}

export function addMoment(data) {
  return request({
    url: '/siyun/moment',
    method: 'POST',
    data,
    loading: true,
  })
}

export function deleteMoment(moment) {
  return request({
    url: '/siyun/moment',
    method: 'DELETE',
    data: moment,
    loading: true,
  })
}

export function applyCreator(data) {
  return request({
    url: '/siyun/beingCreator',
    method: 'POST',
    data,
    loading: true,
  })
}

export function likeMoment(moment) {
  return request({
    url: '/siyun/likeMoment',
    method: 'POST',
    data: moment,
  })
}

export function collectMoment(moment) {
  return request({
    url: '/siyun/collectMoment',
    method: 'POST',
    data: moment,
  })
}

export function shareMoment(moment) {
  return request({
    url: '/siyun/shareMoment',
    method: 'POST',
    data: moment,
  })
}
