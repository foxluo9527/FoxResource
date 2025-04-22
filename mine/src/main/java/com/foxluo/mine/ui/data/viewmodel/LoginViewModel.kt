package com.foxluo.mine.ui.data.viewmodel

import androidx.lifecycle.viewModelScope
import com.foxluo.baselib.data.manager.AuthInfo
import com.foxluo.baselib.data.manager.AuthManager
import com.foxluo.baselib.data.result.RequestResult
import com.foxluo.baselib.domain.viewmodel.BaseViewModel
import com.foxluo.mine.ui.data.repo.AuthRepository
import com.foxluo.mine.ui.data.repo.PersonalRepository
import kotlinx.coroutines.launch

class LoginViewModel : BaseViewModel() {
    private val repo by lazy {
        AuthRepository()
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            isLoading.postValue(true)
            val result = repo.login(username, password)
            isLoading.postValue(false)
            if (result is RequestResult.Success<*>) {
                (result.data as AuthInfo).let {
                    toast.postValue(true to result.message)
                    AuthManager.login(it)
                    PersonalRepository().getProfile()
                }
            } else if (result is RequestResult.Error) {
                toast.postValue(false to result.message)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            isLoading.postValue(true)
            val result = repo.logout()
            isLoading.postValue(false)
            if (result is RequestResult.Success<*>) {
                toast.postValue(true to result.message)
                AuthManager.logout()
            } else if (result is RequestResult.Error) {
                toast.postValue(false to result.message)
            }
        }
    }
}