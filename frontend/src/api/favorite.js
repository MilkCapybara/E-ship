import request from './request'

/**
 * 添加收藏
 */
export function addFavorite(userId, shipId) {
  return request({
    url: '/favorite',
    method: 'post',
    params: { userId, shipId }
  })
}

/**
 * 取消收藏
 */
export function removeFavorite(shipId, userId) {
  return request({
    url: `/favorite/${shipId}`,
    method: 'delete',
    params: { userId }
  })
}

/**
 * 我的收藏
 */
export function getMyFavorites(params) {
  return request({
    url: '/favorite/my',
    method: 'get',
    params
  })
}

/**
 * 检查是否已收藏
 */
export function checkFavorite(shipId, userId) {
  return request({
    url: `/favorite/check/${shipId}`,
    method: 'get',
    params: { userId }
  })
}
