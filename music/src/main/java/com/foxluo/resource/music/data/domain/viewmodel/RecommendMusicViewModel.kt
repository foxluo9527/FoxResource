package com.foxluo.resource.music.data.domain.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.foxluo.baselib.data.result.ListData
import com.foxluo.baselib.data.result.RequestResult
import com.foxluo.baselib.domain.viewmodel.BaseViewModel
import com.foxluo.resource.music.data.bean.MusicData
import com.foxluo.resource.music.data.repo.MusicRepository
import kotlinx.coroutines.launch

class RecommendMusicViewModel : BaseViewModel() {
    init {
        size = 30
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
                (result.data as? ListData<MusicData>)?.let { data ->
                    dataList.postValue(data.list)
                    hadMore.postValue(data.hadMore())
                }
            } else if (result is RequestResult.Error) {
                toast.postValue(Pair(false, result.message))
            }
            isLoading.postValue(false)
        }
    }
}