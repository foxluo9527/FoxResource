package com.foxluo.chat.data.domain.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.foxluo.baselib.data.result.RequestResult
import com.foxluo.baselib.domain.viewmodel.BaseViewModel
import com.foxluo.chat.data.domain.ChatModuleInitializer
import com.foxluo.chat.data.repo.FriendsRepository
import com.foxluo.chat.data.result.UserSearchResult
import kotlinx.coroutines.launch

class SearchUserViewModel : BaseViewModel() {
    private val repo by lazy {
        FriendsRepository(ChatModuleInitializer.chatDb.friendDao())
    }

    private val _users by lazy {
        MutableLiveData<List<UserSearchResult>>()
    }

    val users: LiveData<List<UserSearchResult>> by lazy {
        _users
    }

    fun searchUser(keyword: String) {
        viewModelScope.launch {
            isLoading.postValue(true)
            val result = repo.searchUser(keyword)
            if (result is RequestResult.Error) {
                toast.postValue(Pair(false, result.message))
            } else if (result is RequestResult.Success<*>) {
                val dataList = (result as RequestResult.Success<List<UserSearchResult>>).data
                _users.postValue(dataList)
            }
            isLoading.postValue(false)
        }
    }

    fun request(userId: Int, message: String, mark: String?) {
        viewModelScope.launch {
            isLoading.postValue(true)
            val result = repo.request(userId, message, mark)
            if (result is RequestResult.Error) {
                toast.postValue(Pair(false, result.message))
            } else {
                _users.postValue(
                    _users.value?.toMutableList()?.apply {
                        val index = indexOf(find { it.id == userId })
                        if (index >= 0) {
                            this[index] = this[index].apply {
                                is_requested = true
                            }
                        }
                    }
                )
            }
            isLoading.postValue(false)
        }
    }
}