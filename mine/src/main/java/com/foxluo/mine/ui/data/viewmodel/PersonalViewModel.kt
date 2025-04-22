package com.foxluo.mine.ui.data.viewmodel

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.viewModelScope
import com.foxluo.baselib.data.result.RequestResult
import com.foxluo.baselib.domain.viewmodel.BaseUploadViewModel
import com.foxluo.baselib.domain.viewmodel.BaseViewModel
import com.foxluo.mine.ui.data.api.PersonalApi
import com.foxluo.mine.ui.data.bean.PersonalProfile
import com.foxluo.mine.ui.data.repo.PersonalRepository
import kotlinx.coroutines.launch

class PersonalViewModel : BaseUploadViewModel() {
    val profile by lazy {
        MediatorLiveData<PersonalProfile>().apply {
            addSource(processUploadingFile) {
                updateProfile(avatar = it)
            }
        }
    }

    private val repo by lazy {
        PersonalRepository()
    }

    fun initProfile() {
        viewModelScope.launch {
            isLoading.postValue(true)
            val result = repo.getProfile()
            if (result is RequestResult.Success<*>) {
                (result.data as PersonalProfile).let {
                    profile.value = it
                }
            } else if (result is RequestResult.Error) {
                toast.postValue(false to result.message)
            }
            isLoading.postValue(false)
        }
    }


    fun updateProfile(
        nickName: String? = null,
        avatar: String? = null,
        signature: String? = null,
        email: String? = null
    ) {
        viewModelScope.launch {
            isLoading.postValue(true)
            val result = repo.setProfile(nickName, avatar, signature, email)
            if (result is RequestResult.Success<*>) {
                initProfile()
            } else if (result is RequestResult.Error) {
                toast.postValue(false to result.message)
                isLoading.postValue(false)
            }
        }
    }
}