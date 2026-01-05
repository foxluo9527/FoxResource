package com.foxluo.chat.data.repo

import com.foxluo.baselib.data.manager.AuthManager
import com.foxluo.baselib.data.respository.BaseRepository
import com.foxluo.baselib.data.result.BaseResponse.Companion.toRequestResult
import com.foxluo.baselib.data.result.RequestResult
import com.foxluo.chat.data.api.FriendsApi
import com.foxluo.chat.data.database.FriendDao
import com.foxluo.chat.data.result.FriendResult

class FriendsRepository(private val friendDao: FriendDao) : BaseRepository() {
    private val friendsApi by lazy {
        createApi<FriendsApi>()
    }

    suspend fun loadFriendship(): RequestResult {
        var result =
            kotlin.runCatching { friendsApi?.getFriendsList() }.getOrNull().toRequestResult()
        if (result is RequestResult.Success<*>) {
            (result.data as? List<FriendResult>)?.let {
                val list = it.map {
                    it.toEntity()
                }
                AuthManager.authInfo?.user?.id?.toInt()?.let { userId ->
                    friendDao.updateAllFriends(userId, list)
                } ?: {
                    result = RequestResult.Error(401,"请先登录")
                }
            }
        }
        return result
    }

    suspend fun searchUser(keyword: String): RequestResult {
        var result =
            kotlin.runCatching { friendsApi?.search(keyword) }.getOrNull().toRequestResult()
        return result
    }

    suspend fun request(userId: Int, message: String, mark: String?): RequestResult {
        val map = mapOf<String, String?>(
            "friend_id" to userId.toString(),
            "message" to message,
            "mark" to mark
        )
        var result =
            kotlin.runCatching { friendsApi?.request(map) }.getOrNull().toRequestResult()
        return result
    }

    suspend fun getRequests(): RequestResult {
        var result =
            kotlin.runCatching { friendsApi?.getFriendsRequests() }.getOrNull().toRequestResult()
        return result
    }

    suspend fun delete(id: Int): RequestResult {
        var result =
            kotlin.runCatching { friendsApi?.delete(id.toString()) }.getOrNull().toRequestResult()
        return result
    }

    suspend fun remark(id: Int, mark: String): RequestResult {
        val map = mapOf<String, String>("friendId" to id.toString(), "remark" to mark)
        var result =
            kotlin.runCatching { friendsApi?.remark(map) }.getOrNull().toRequestResult()
        return result
    }

    suspend fun accept(id: Int): RequestResult {
        val map = mapOf<String, String>("requestId" to id.toString())
        var result =
            kotlin.runCatching { friendsApi?.accept(map) }.getOrNull().toRequestResult()
        return result
    }
}