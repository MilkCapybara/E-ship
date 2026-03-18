import request from './request'

/**
 * 用户注册
 */
export function register(data) {
  return request({
    url: '/auth/register',
    method: 'post',
    data
  })
}

/**
 * 用户登录
 */
export function login(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

/**
 * 发送验证码
 */
export function sendCode(data) {
  return request({
    url: '/auth/send-code',
    method: 'post',
    data
  })
}

/**
 * 修改密码
 */
export function changePassword(data) {
  return request({
    url: '/auth/change-password',
    method: 'post',
    data
  })
}

/**
 * 重置密码
 */
export function resetPassword(data) {
  return request({
    url: '/auth/reset-password',
    method: 'post',
    data
  })
}

/**
 * 获取用户信息
 */
export function getUserInfo() {
  return request({
    url: '/auth/user-info',
    method: 'get'
  })
}

/**
 * 通过用户名获取邮箱
 */
export function getEmailByUsername(username) {
  return request({
    url: '/auth/get-email',
    method: 'get',
    params: { username }
  })
}

/**
 * 退出登录
 */
export function logout() {
  return request({
    url: '/auth/logout',
    method: 'post'
  })
}
