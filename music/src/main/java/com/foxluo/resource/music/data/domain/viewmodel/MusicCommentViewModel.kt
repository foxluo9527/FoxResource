package com.foxluo.resource.music.data.domain.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.foxluo.baselib.data.result.ListData
import com.foxluo.baselib.data.result.RequestResult
import com.foxluo.baselib.domain.viewmodel.BaseViewModel
import com.foxluo.baselib.ui.adapter.CommentAdapter
import com.foxluo.resource.music.data.domain.MusicModuleInitializer
import com.foxluo.resource.music.data.repo.MusicRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MusicCommentViewModel : BaseViewModel() {
    private val db by lazy{
        MusicModuleInitializer.musicDb
    }

    private val repo by lazy {
        MusicRepository(db.musicDao(),db.artistDao())
    }

    val commentList by lazy {
        MutableLiveData<List<CommentAdapter.CommentBean>>()
    }

    val insertReplyList by lazy {
        MutableLiveData<Pair<String, List<CommentAdapter.CommentBean>>>()
    }

    val appendCommentList by lazy {
        MutableLiveData<List<CommentAdapter.CommentBean>>()
    }

    val processLoading by lazy {
        MutableLiveData<Boolean>()
    }

    val commentCount by lazy {
        MutableLiveData<Int>()
    }

    val replyComment by lazy {
        MutableLiveData<CommentAdapter.CommentBean>()
    }

    fun getCommentList(musicId: String, isLoadMore: Boolean = false) {
        viewModelScope.launch {
            isLoading.postValue(true)
            if (isLoadMore) {
                page++
            } else {
                page = 1
            }
            val result = repo.getMusicComment(musicId, page, size)
            if (result is RequestResult.Success) {
                result.data?.let { data ->
                    if (!isLoadMore) {
                        commentList.postValue(data.list)
                    } else {
                        appendCommentList.postValue(data.list)
                    }
                    hadMore.postValue(data.hadMore())
                    commentCount.postValue(data.total)
                }
            } else if (result is RequestResult.Error) {
                toast.postValue(Pair(false, result.message))
            }
            isLoading.postValue(false)
            processLoading.postValue(false)
        }
    }

    fun likeComment(commentId: String, block: (Boolean) -> Unit) {
        viewModelScope.launch {
            if (processLoading.value == true) return@launch
            processLoading.postValue(true)
            val result = repo.likeMusicComment(commentId)
            processLoading.postValue(false)
            if (result is RequestResult.Success) {
                block.invoke(true)
            } else if (result is RequestResult.Error) {
                toast.postValue(Pair(false, result.message))
                block.invoke(false)
            }
        }
    }

    fun getReplyList(commentId: String, isAddRefresh: Boolean = false) {
        viewModelScope.launch {
            if (processLoading.value == true && !isAddRefresh) return@launch
            commentList.value?.find { it.id == commentId }?.let { comment ->
                if (isAddRefresh) comment.replyCount++
                processLoading.postValue(true)
                val result = repo.getMusicCommentReply(
                    commentId,
                    1,
                    if (!isAddRefresh) comment.replyCount else comment.replyCount - comment.displayReplyCount + 1
                )
                processLoading.postValue(false)
                if (result is RequestResult.Success) {
                    var replyId = commentId
                    result.data?.let { data ->
                        val list = data.list
                        comment.replyCount = data.total
                        list?.toMutableList()?.apply {
                            if (data.total > 1) {
                                list.getOrNull(0)?.let { replyId = it.id }
                            }
                        }?.toList()
                    }?.let {
                        insertReplyList.postValue(replyId to it)
                    }
                    Unit
                } else if (result is RequestResult.Error) {
                    toast.postValue(Pair(false, result.message))
                }
            }
        }
    }

    fun postComment(
        musicId: String,
        content: String,
        block: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            processLoading.postValue(true)
            val result = repo.postMusicComment(musicId, content, replyComment.value?.id)
            if (result is RequestResult.Success) {
                delay(200)
                block.invoke(true)
            } else if (result is RequestResult.Error) {
                toast.postValue(Pair(false, result.message))
                block.invoke(false)
                processLoading.postValue(false)
            }
        }
    }
}