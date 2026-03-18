import request from './request'

/**
 * 添加船舶
 */
export function addShip(ownerId, data) {
  return request({
    url: '/ship',
    method: 'post',
    params: { ownerId },
    data
  })
}

/**
 * 修改船舶
 */
export function updateShip(shipId, ownerId, data) {
  return request({
    url: `/ship/${shipId}`,
    method: 'put',
    params: { ownerId },
    data
  })
}

/**
 * 删除船舶
 */
export function deleteShip(shipId, ownerId) {
  return request({
    url: `/ship/${shipId}`,
    method: 'delete',
    params: { ownerId }
  })
}

/**
 * 我的船舶列表
 */
export function getMyShips(params) {
  return request({
    url: '/ship/my',
    method: 'get',
    params
  })
}

/**
 * 船舶详情
 */
export function getShipDetail(shipId) {
  return request({
    url: `/ship/${shipId}`,
    method: 'get'
  })
}

/**
 * 出租中的船舶
 */
export function getRentedShips(params) {
  return request({
    url: '/ship/rented',
    method: 'get',
    params
  })
}

/**
 * 搜索船舶
 */
export function searchShips(data) {
  return request({
    url: '/ship/search',
    method: 'post',
    data
  })
}

/**
 * 推荐船舶
 */
export function getRecommendedShips(limit = 10) {
  return request({
    url: '/ship/recommended',
    method: 'get',
    params: { limit }
  })
}

/**
 * 可租赁船舶
 */
export function getAvailableShips(params) {
  return request({
    url: '/ship/available',
    method: 'get',
    params
  })
}
