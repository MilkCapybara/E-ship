import request from './request'

/**
 * 合约信箱（待审核）
 */
export function getOwnerInbox(params) {
  return request({
    url: '/contract/inbox',
    method: 'get',
    params
  })
}

/**
 * 同意合约
 */
export function approveContract(contractId, ownerId) {
  return request({
    url: `/contract/${contractId}/approve`,
    method: 'post',
    params: { ownerId }
  })
}

/**
 * 拒绝合约
 */
export function rejectContract(contractId, ownerId, data) {
  return request({
    url: `/contract/${contractId}/reject`,
    method: 'post',
    params: { ownerId },
    data
  })
}

/**
 * 我的合约
 */
export function getMyContracts(params) {
  return request({
    url: '/contract/my',
    method: 'get',
    params
  })
}

/**
 * 合约详情
 */
export function getContractDetail(contractId) {
  return request({
    url: `/contract/${contractId}`,
    method: 'get'
  })
}

/**
 * 创建合约
 */
export function createContract(renterId, data) {
  return request({
    url: '/contract',
    method: 'post',
    params: { renterId },
    data
  })
}

/**
 * 租家的合约
 */
export function getRenterContracts(params) {
  return request({
    url: '/contract/renter',
    method: 'get',
    params
  })
}

/**
 * 取消合约
 */
export function cancelContract(contractId, renterId) {
  return request({
    url: `/contract/${contractId}`,
    method: 'delete',
    params: { renterId }
  })
}
