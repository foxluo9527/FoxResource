package com.foxluo.resource.music.data.domain.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.foxluo.baselib.data.result.RequestResult
import com.foxluo.baselib.domain.viewmodel.BaseViewModel
import com.foxluo.baselib.ui.adapter.CommentAdapter
import com.foxluo.resource.music.data.repo.MusicRepository
import kotlinx.coroutines.launch

class MusicCommentViewModel : BaseViewModel() {
    init {
        size = 3
    }

    private val repo by lazy {
        MusicRepository()
    }

    val commentList by lazy {
        MutableLiveData<List<CommentAdapter.CommentBean>>()
    }

    fun getCommentList(musicId: String) {
        viewModelScope.launch {
            isLoading.postValue(true)
            val result = repo.getMusicComment(musicId, page, size)
            if (result is RequestResult.Success<*>) {
                commentList.postValue(result.data as? List<CommentAdapter.CommentBean>)
            } else if (result is RequestResult.Error) {
                toast.postValue(Pair(false, result.message))
            }
            isLoading.postValue(false)
        }
    }
}