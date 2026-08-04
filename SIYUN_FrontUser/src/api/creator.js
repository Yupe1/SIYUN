import { request, uploadFile } from '@/utils/request'

export function getCreatorCourseCategories() {
  return request({
    url: '/siyun/creator/course-categories',
    method: 'GET',
  })
}

export function getCreatorCourses() {
  return request({
    url: '/siyun/creator/courses',
    method: 'GET',
  })
}

export function getCreatorCourse(id) {
  return request({
    url: `/siyun/creator/courses/${id}`,
    method: 'GET',
  })
}

export function createCreatorCourse(data) {
  return request({
    url: '/siyun/creator/courses',
    method: 'POST',
    data,
    loading: true,
  })
}

export function updateCreatorCourse(id, data) {
  return request({
    url: `/siyun/creator/courses/${id}`,
    method: 'PUT',
    data,
    loading: true,
  })
}

export function deleteCreatorCourse(id) {
  return request({
    url: `/siyun/creator/courses/${id}`,
    method: 'DELETE',
    loading: true,
  })
}

export function uploadCreatorCourseCover(filePath, onProgress) {
  return uploadFile({
    url: '/siyun/upload/course-cover',
    filePath,
    onProgress,
  })
}

export function uploadCreatorCourseVideo(filePath, onProgress) {
  return uploadFile({
    url: '/siyun/upload/video',
    filePath,
    onProgress,
  })
}
