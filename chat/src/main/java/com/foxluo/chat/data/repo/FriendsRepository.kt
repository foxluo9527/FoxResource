package com.foxluo.chat.data.repo

import com.foxluo.baselib.data.manager.AuthManager
import com.foxluo.baselib.data.respository.BaseRepository
import com.foxluo.baselib.data.result.BaseResponse.Companion.toRequestResult
import com.foxluo.baselib.data.result.RequestResult
import com.foxluo.chat.data.api.FriendsApi
import com.foxluo.chat.data.database.FriendDao
import com.foxluo.chat.data.result.FriendRequestResult
import com.foxluo.chat.data.result.FriendResult
import com.foxluo.chat.data.result.UserSearchResult

class FriendsRepository(private val friendDao: FriendDao) : BaseRepository() {
    private val friendsApi by lazy {
        createApi<FriendsApi>()
    }

    suspend fun loadFriendship(): RequestResult<List<FriendResult>?> {
        var result =
            kotlin.runCatching { friendsApi?.getFriendsList() }.toRequestResult()
        if (result is RequestResult.Success) {
            result.data?.let { resultList ->
                val list = resultList.map {
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

    suspend fun searchUser(keyword: String): RequestResult<List<UserSearchResult>?> {
        var result =
            kotlin.runCatching { friendsApi?.search(keyword) }.toRequestResult()
        return result
    }

    suspend fun request(userId: Int, message: String, mark: String?): RequestResult<Unit?> {
        val map = mapOf<String, String?>(
            "friend_id" to userId.toString(),
            "message" to message,
            "mark" to mark
        )
        var result =
            kotlin.runCatching { friendsApi?.request(map) }.toRequestResult()
        return result
    }

    suspend fun getRequests(): RequestResult<List<FriendRequestResult>?> {
        var result =
            kotlin.runCatching { friendsApi?.getFriendsRequests() }.toRequestResult()
        return result
    }

    suspend fun delete(id: Int): RequestResult<Unit?> {
        var result =
            kotlin.runCatching { friendsApi?.delete(id.toString()) }.toRequestResult()
        return result
    }

    suspend fun remark(id: Int, mark: String): RequestResult<Unit?> {
        val map = mapOf<String, String>("friendId" to id.toString(), "remark" to mark)
        var result =
            kotlin.runCatching { friendsApi?.remark(map) }.toRequestResult()
        return result
    }

    suspend fun accept(id: Int): RequestResult<Unit?> {
        val map = mapOf<String, String>("requestId" to id.toString())
        var result =
            kotlin.runCatching { friendsApi?.accept(map) }.toRequestResult()
        return result
    }
}