package com.foxluo.baselib.domain

/**
 *    Author : 罗福林
 *    Date   : 2026/1/4
 *    Desc   : 认证失败错误
 */
class AuthorizFailError : Throwable() {
    override val message: String?
        get() = "请先登录"
}