package com.foxluo.mine.data.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.foxluo.baselib.data.result.RequestResult
import com.foxluo.baselib.domain.viewmodel.BaseViewModel
import com.foxluo.mine.data.repo.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ForgetViewModel : BaseViewModel() {
    val codeSendState by lazy {
        MutableLiveData<Int>()
    }

    val changePassResult by lazy {
        MutableLiveData<Boolean>()
    }

    private val repo by lazy {
        com.foxluo.mine.data.repo.AuthRepository()
    }

    private suspend fun setWaitSendCodeTime(time: Int) {
        codeSendState.postValue(time)
        delay(1000)
        if (time > 0) {
            setWaitSendCodeTime(time - 1)
        }
    }

    fun sendCode(email: String) {
        viewModelScope.launch {
            isLoading.postValue(true)
            val result = repo.sendForgetEmailCode(email)
            isLoading.postValue(false)
            if (result is RequestResult.Success<*>) {
                setWaitSendCodeTime(60)
            } else if (result is RequestResult.Error) {
                toast.postValue(false to result.message)
            }
        }
    }

    fun changePass(email: String, code: String, password: String) {
        viewModelScope.launch {
            isLoading.postValue(true)
            val result = repo.resetForgetPassword(email, code, password)
            isLoading.postValue(false)
            if (result is RequestResult.Success<*>) {
                changePassResult.postValue(true)
            } else if (result is RequestResult.Error) {
                toast.postValue(false to result.message)
            }
        }
    }
}