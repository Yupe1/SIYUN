import { request } from '@/utils/request'

export function searchCourses(keywords = '') {
  return request({
    url: '/siyun/course',
    method: 'GET',
    data: {
      keywords,
    },
  })
}

export function getCourseDetail(id) {
  return request({
    url: `/siyun/course/${id}`,
    method: 'GET',
  })
}

export function purchaseCourse(courseId, couponUserId = null) {
  return request({
    url: '/siyun/order',
    method: 'POST',
    data: {
      courseId,
      ...(couponUserId ? { couponUserId } : {}),
    },
    loading: true,
  })
}

export function getOrderStatus(courseId) {
  return request({
    url: '/siyun/orderStatus',
    method: 'GET',
    data: {
      courseId,
    },
  })
}

export function getCourseContents(courseId) {
  return request({
    url: '/siyun/course/content',
    method: 'GET',
    data: {
      courseId,
    },
  })
}

export function getLikeStatus(courseId) {
  return request({
    url: '/siyun/likeStatus',
    method: 'GET',
    data: {
      courseId,
    },
  })
}

export function getCollectStatus(courseId) {
  return request({
    url: '/siyun/collectStatus',
    method: 'GET',
    data: {
      courseId,
    },
  })
}

export function getCoupons(course) {
  return request({
    url: '/siyun/coupons',
    method: 'GET',
    data: course,
  })
}

export function startPlay(course) {
  return request({
    url: '/siyun/startplay',
    method: 'POST',
    data: course,
  })
}

export function stopPlay(log) {
  return request({
    url: '/siyun/stopplay',
    method: 'PATCH',
    data: log,
  })
}

export function toggleLike(course) {
  return request({
    url: '/siyun/like',
    method: 'POST',
    data: course,
  })
}

export function toggleCollect(course) {
  return request({
    url: '/siyun/collect',
    method: 'POST',
    data: course,
  })
}

export function getMyCollect() {
  return request({
    url: '/siyun/collect',
    method: 'GET',
  })
}

export function shareCourse(course) {
  return request({
    url: '/siyun/share',
    method: 'POST',
    data: course,
  })
}
