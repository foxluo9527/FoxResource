package com.foxluo.chat.data.domain.viewmodel

import androidx.lifecycle.viewModelScope
import com.foxluo.baselib.data.result.RequestResult
import com.foxluo.baselib.domain.viewmodel.BaseViewModel
import com.foxluo.chat.data.domain.ChatModuleInitializer
import com.foxluo.chat.data.repo.FriendsRepository
import com.foxluo.chat.data.result.FriendResult
import kotlinx.coroutines.launch

class UserDetailViewModel : BaseViewModel() {
    private val dao by lazy {
        ChatModuleInitializer.chatDb.friendDao()
    }

    private val repo by lazy {
        FriendsRepository(dao)
    }

    fun delete(user: FriendResult, block: () -> Unit) {
        viewModelScope.launch {
            isLoading.postValue(true)
            val result = repo.delete(user.id)
            if (result is RequestResult.Error) {
                toast.postValue(Pair(false, result.message))
            } else if (result is RequestResult.Success) {
                block.invoke()
                dao.deleteFriend(user.toEntity())//直接通过room关系好友列表数据
            }
            isLoading.postValue(false)
        }
    }

    fun remark(user: FriendResult, mark: String, block: () -> Unit) {
        viewModelScope.launch {
            isLoading.postValue(true)
            val result = repo.remark(user.id, mark)
            if (result is RequestResult.Error) {
                toast.postValue(Pair(false, result.message))
            } else if (result is RequestResult.Success) {
                block.invoke()
                user.mark = mark
                dao.updateFriend(user.toEntity())//直接通过room关系好友列表数据
            }
            isLoading.postValue(false)
        }
    }
}