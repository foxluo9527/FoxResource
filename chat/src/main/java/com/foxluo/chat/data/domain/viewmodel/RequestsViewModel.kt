package com.foxluo.chat.data.domain.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.foxluo.baselib.data.result.RequestResult
import com.foxluo.baselib.domain.viewmodel.BaseViewModel
import com.foxluo.chat.data.domain.ChatModuleInitializer
import com.foxluo.chat.data.repo.FriendsRepository
import com.foxluo.chat.data.result.FriendRequestResult
import kotlinx.coroutines.launch

class RequestsViewModel : BaseViewModel() {
    private val repo by lazy {
        FriendsRepository(ChatModuleInitializer.chatDb.friendDao())
    }
    private val _requests by lazy {
        MutableLiveData<List<FriendRequestResult>>()
    }

    val requests: LiveData<List<FriendRequestResult>> by lazy {
        _requests
    }

    fun getFriendRequests() {
        viewModelScope.launch {
            isLoading.postValue(true)
            val result = repo.getRequests()
            if (result is RequestResult.Error) {
                toast.postValue(Pair(false, result.message))
                _requests.postValue(listOf<FriendRequestResult>())
            } else if (result is RequestResult.Success) {
                _requests.postValue(result.data?:listOf())
            }
            isLoading.postValue(false)
        }
    }

    fun accept(data: FriendRequestResult,block:()-> Unit) {
        viewModelScope.launch {
            isLoading.postValue(true)
            val result = repo.accept(data.id)
            if (result is RequestResult.Error) {
                toast.postValue(Pair(false, result.message))
            }else{
                block.invoke()
                _requests.postValue(_requests.value?.toMutableList()?.apply {
                    remove(data)
                })
            }
            isLoading.postValue(false)
        }
    }
}