import { request } from '@/utils/request'

export function getComments(entityId, entityType) {
  return request({
    url: '/siyun/comment',
    method: 'GET',
    data: { id: entityId, entityType },
  })
}

export function getSubComments(commentId) {
  return request({
    url: '/siyun/subComment',
    method: 'GET',
    data: { id: commentId },
  })
}

export function addComment(entityId, entityType, content, parentId = 0) {
  return request({
    url: '/siyun/comment',
    method: 'POST',
    data: {
      entityId,
      entityType,
      parentId,
      content,
    },
    loading: true,
  })
}

export function deleteComment(id) {
  return request({
    url: '/siyun/comment',
    method: 'DELETE',
    data: { id },
    loading: true,
  })
}
