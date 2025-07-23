package com.foxluo.chat.data.domain.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.foxluo.baselib.data.result.RequestResult
import com.foxluo.baselib.domain.viewmodel.BaseViewModel
import com.foxluo.chat.data.domain.ChatModuleInitializer
import com.foxluo.chat.data.repo.FriendsRepository
import com.foxluo.chat.data.result.FriendResult
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FriendsViewModel : BaseViewModel() {
    private val repo by lazy {
        FriendsRepository(ChatModuleInitializer.chatDb.friendDao())
    }
    private val _friends by lazy {
        MutableLiveData<List<FriendResult>>()
    }

    val friends: LiveData<List<FriendResult>> by lazy {
        _friends
    }

    fun initObserver(){
        viewModelScope.launch {
            ChatModuleInitializer.friendshipFlow.collectLatest {
                _friends.postValue(it.map {
                    FriendResult(
                        it.id,
                        it.username,
                        it.nickname,
                        it.avatar,
                        it.signature,
                        it.mark
                    )
                })
            }
        }
    }

    fun getFriends() {
        viewModelScope.launch {
            isLoading.postValue(true)
            val result = repo.loadFriendship()
            if (result is RequestResult.Error) {
                toast.postValue(Pair(false, result.message))
            }
            isLoading.postValue(false)
        }
    }


}