package com.foxluo.mine.ui.data.viewmodel

import androidx.lifecycle.viewModelScope
import com.foxluo.baselib.data.manager.AuthInfo
import com.foxluo.baselib.data.manager.AuthManager
import com.foxluo.baselib.data.result.RequestResult
import com.foxluo.baselib.domain.viewmodel.BaseViewModel
import com.foxluo.mine.ui.data.repo.AuthRepository
import kotlinx.coroutines.launch

class RegisterViewModel: BaseViewModel() {
    private val repo by lazy {
        AuthRepository()
    }

    fun register(userName:String,password:String,email:String){
        viewModelScope.launch {
            isLoading.postValue(true)
            val result=repo.register(userName,email,password)
            isLoading.postValue(false)
            if (result is RequestResult.Success<*>) {
                (result.data as AuthInfo).let {
                    toast.postValue(true to result.message)
                    AuthManager.login(it)
                }
            } else if (result is RequestResult.Error) {
                toast.postValue(false to result.message)
            }
        }
    }
}