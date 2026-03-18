import request from './request'

/**
 * 查询所有用户
 */
export function getAllUsers(params) {
  return request({
    url: '/admin/users',
    method: 'get',
    params
  })
}

/**
 * 更新用户状态
 */
export function updateUserStatus(data) {
  return request({
    url: '/admin/user/status',
    method: 'put',
    data
  })
}

/**
 * 更新信用评分
 */
export function updateCreditScore(data) {
  return request({
    url: '/admin/user/credit',
    method: 'put',
    data
  })
}

/**
 * 用户详情
 */
export function getUserDetail(userId) {
  return request({
    url: `/admin/user/${userId}`,
    method: 'get'
  })
}

/**
 * 查询所有船舶
 */
export function getAllShips(params) {
  return request({
    url: '/admin/ships',
    method: 'get',
    params
  })
}

/**
 * 更新船舶状态
 */
export function updateShipStatus(shipId, status) {
  return request({
    url: '/admin/ship/status',
    method: 'put',
    params: { shipId, status }
  })
}

/**
 * 查询所有合约
 */
export function getAllContracts(params) {
  return request({
    url: '/admin/contracts',
    method: 'get',
    params
  })
}

/**
 * 强制终止合约
 */
export function forceTerminateContract(contractId, reason) {
  return request({
    url: `/admin/contract/${contractId}/terminate`,
    method: 'post',
    params: { reason }
  })
}

/**
 * 获取统计数据
 */
export function getStatistics() {
  return request({
    url: '/admin/statistics',
    method: 'get'
  })
}
