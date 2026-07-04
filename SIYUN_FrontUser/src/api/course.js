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

export function purchaseCourse(course) {
  return request({
    url: '/siyun/order',
    method: 'POST',
    data: course,
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

export function addComment(courseId, content, parentId = 0) {
  return request({
    url: '/siyun/comment',
    method: 'POST',
    data: {
      entityId: courseId,
      entityType: 0,
      parentId,
      content,
      statusShow: 1,
      countLike: 0,
      countReply: 0,
    },
    loading: true,
  })
}

export function getComments(courseId) {
  return request({
    url: '/siyun/comment',
    method: 'GET',
    data: {
      id: courseId,
    },
  })
}

export function getSubComments(commentId) {
  return request({
    url: '/siyun/subComment',
    method: 'GET',
    data: {
      id: commentId,
    },
  })
}

export function likeComment(comment) {
  return request({
    url: '/siyun/commentLike',
    method: 'POST',
    data: comment,
  })
}

export function deleteComment(comment) {
  return request({
    url: '/siyun/comment',
    method: 'DELETE',
    data: comment,
  })
}
