package com.foxluo.mine.data.repo

import com.foxluo.baselib.data.manager.AuthManager
import com.foxluo.baselib.data.respository.BaseRepository
import com.foxluo.baselib.data.result.BaseResponse.Companion.toRequestResult
import com.foxluo.baselib.data.result.RequestResult
import com.foxluo.mine.data.api.PersonalApi
import com.foxluo.mine.data.bean.PersonalProfile

class PersonalRepository : BaseRepository() {
    private val api by lazy {
        createApi<PersonalApi>()
    }

    suspend fun getProfile(): RequestResult<PersonalProfile?> {
        return runCatching{ api?.profile() }.toRequestResult().also { result ->
            if (result.isSuccess()) {
                (result as RequestResult.Success).data?.let {
                    AuthManager.updatePersonalInfo(
                        it.username,
                        it.nickname,
                        it.avatar,
                        it.signature
                    )
                }
            }
        }
    }

    suspend fun setProfile(
        nickname: String? = null,
        avatar: String? = null,
        signature: String? = null,
        email: String? = null
    ): RequestResult<Unit?> {
        val body = mutableMapOf<String, String>()
        if (nickname != null) {
            body["nickname"] = nickname
        }
        if (avatar != null) {
            body["avatar"] = avatar
        }
        if (signature != null) {
            body["signature"] = signature
        }
        if (email != null) {
            body["email"] = email
        }
        return runCatching{ api?.profile(body) }.toRequestResult()
    }
}