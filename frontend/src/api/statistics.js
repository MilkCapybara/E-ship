import request from './request'

/**
 * 获取船东统计数据
 */
export function getOwnerStatistics(ownerId) {
  return request({
    url: '/statistics/owner',
    method: 'get',
    params: { ownerId }
  })
}

/**
 * 获取租家统计数据
 */
export function getRenterStatistics(renterId) {
  return request({
    url: '/statistics/renter',
    method: 'get',
    params: { renterId }
  })
}

/**
 * 获取管理员统计数据
 */
export function getAdminStatistics() {
  return request({
    url: '/statistics/admin',
    method: 'get'
  })
}
