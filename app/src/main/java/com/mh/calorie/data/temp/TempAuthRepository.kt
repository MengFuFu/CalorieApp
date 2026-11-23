package com.mh.calorie.data.temp

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/** 登录失败的几种情况，页面据此选文案 */
enum class AuthError {
    EMPTY_ACCOUNT,      // 账号没填
    EMPTY_PASSWORD,     // 密码没填
    TOO_SHORT,          // 少于 3 位
    ACCOUNT_EXISTS,     // 注册时账号已存在
    ACCOUNT_NOT_FOUND,  // 登录时账号不存在
    WRONG_PASSWORD,     // 密码不对
    CONFIRM_MISMATCH    // 两次密码不一致
}

/**
 * 临时账号仓库：纯内存，进程一关就没了。
 * M15 换后端时把 register / login 换成网络请求，其余不用动。
 */
@Singleton
class TempAuthRepository @Inject constructor() {

    // 账号 → 密码（键统一小写，避免大小写被当成两个账号）
    private val accounts = mutableMapOf("demo" to "123456")

    private val _currentUser = MutableStateFlow<String?>(null)
    val currentUser: StateFlow<String?> = _currentUser.asStateFlow()

    val isLoggedIn: Boolean get() = _currentUser.value != null

    /** 注册：成功返回 null，失败返回错误类型 */
    fun register(account: String, password: String, confirm: String): AuthError? {
        val key = account.trim().lowercase()
        return when {
            key.isEmpty() -> AuthError.EMPTY_ACCOUNT
            password.isEmpty() -> AuthError.EMPTY_PASSWORD
            key.length < 3 || password.length < 3 -> AuthError.TOO_SHORT
            password != confirm -> AuthError.CONFIRM_MISMATCH
            accounts.containsKey(key) -> AuthError.ACCOUNT_EXISTS
            else -> {
                accounts[key] = password
                null
            }
        }
    }

    /** 登录：成功后当前用户就是账号名 */
    fun login(account: String, password: String): AuthError? {
        val key = account.trim().lowercase()
        return when {
            key.isEmpty() -> AuthError.EMPTY_ACCOUNT
            password.isEmpty() -> AuthError.EMPTY_PASSWORD
            !accounts.containsKey(key) -> AuthError.ACCOUNT_NOT_FOUND
            accounts[key] != password -> AuthError.WRONG_PASSWORD
            else -> {
                _currentUser.value = key
                null
            }
        }
    }

    fun logout() {
        _currentUser.value = null
    }

}