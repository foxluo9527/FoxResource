package com.foxluo.resource.music.data.domain.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.foxluo.baselib.data.result.RequestResult
import com.foxluo.baselib.domain.viewmodel.BaseViewModel
import com.foxluo.resource.music.data.bean.MusicData
import com.foxluo.resource.music.data.repo.MusicRepository
import kotlinx.coroutines.launch

class MusicViewModel : BaseViewModel() {
    val currentMusic by lazy {
        MutableLiveData<MusicData?>()
    }

    private val repo by lazy {
        MusicRepository()
    }

    val dataList by lazy {
        MutableLiveData<List<MusicData>>()
    }

    fun getMusicData(isRefresh: Boolean) {
        if (isRefresh) {
            page = 1
        } else {
            page++
        }
        viewModelScope.launch {
            val result = repo.getMusicList(page, size)
            if (result is RequestResult.Success<*>) {
                dataList.postValue(result.data as List<MusicData>?)
            } else if (result is RequestResult.Error) {
                toast.postValue(Pair(false, result.message))
            }
            isLoading.postValue(false)
        }
    }
}