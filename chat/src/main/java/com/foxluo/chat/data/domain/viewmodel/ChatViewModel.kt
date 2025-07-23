package com.foxluo.chat.data.domain.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.blankj.utilcode.util.TimeUtils
import com.blankj.utilcode.util.Utils
import com.foxluo.baselib.data.manager.AuthManager
import com.foxluo.baselib.data.result.RequestResult
import com.foxluo.baselib.domain.viewmodel.BaseUploadViewModel
import com.foxluo.baselib.ui.adapter.AlbumAdapter
import com.foxluo.baselib.util.getFilePath
import com.foxluo.chat.data.database.MessageEntity
import com.foxluo.chat.data.domain.ChatModuleInitializer
import com.foxluo.chat.data.domain.MessageUploadWorker
import com.foxluo.chat.data.repo.ChatRepository
import com.foxluo.chat.data.result.FriendResult
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel : BaseUploadViewModel() {
    private val messageDao by lazy {
        ChatModuleInitializer.chatDb.messageDao()
    }
    private val chatDao by lazy {
        ChatModuleInitializer.chatDb.chatDao()
    }
    private val friendDao by lazy {
        ChatModuleInitializer.chatDb.friendDao()
    }
    private val repo by lazy {
        ChatRepository(messageDao, chatDao, friendDao)
    }

    private val workManager = WorkManager.getInstance(Utils.getApp().applicationContext)

    val messagePager by lazy {
        MutableStateFlow<PagingData<MessageEntity>>(PagingData.empty())
    }

    fun loadMessage(friendId: Int) {
        viewModelScope.launch {
            repo.getUnreadMessages(friendId)
            val userId = AuthManager.authInfo?.user?.id?.toInt()
            if (userId == null) {
                toast.postValue(false to "获取数据失败,请先登录")
            } else {
                Pager(
                    config = PagingConfig(
                        pageSize = 20,
                        prefetchDistance = 2,
                        initialLoadSize = 20
                    ),
                    pagingSourceFactory = { messageDao.getMessages(userId, friendId) }
                ).flow
                    .cachedIn(viewModelScope)
                    .collectLatest { pagingData ->
                        messagePager.value = pagingData.map {
                            it.apply {
                                sent_at = TimeUtils.getFriendlyTimeSpanByNow(send_time)
                            }
                        }
                    }
            }
        }
    }

    fun sendTextMessage(
        friend: FriendResult,
        content: String,
        block: (LiveData<WorkInfo?>) -> Unit
    ) {
        viewModelScope.launch {
            isLoading.postValue(true)
            val localMessage = repo.createTempTextMessage(friend, content) ?: return@launch.also {
                toast.postValue(false to "发送失败")
            }
            isLoading.postValue(false)
            // 启动任务
            val uploadWork = enqueueSend(localMessage)
            block.invoke(workManager.getWorkInfoByIdLiveData(uploadWork.id))
        }
    }

    /**
     * 发送图片/视频类消息
     */
    fun sendImageMessage(
        friend: FriendResult,
        image: AlbumAdapter.Image,
        block: (LiveData<WorkInfo?>) -> Unit
    ) {
        viewModelScope.launch {
            val filePath = image.uri.getFilePath(image.isCropped) ?: return@launch
            isLoading.postValue(true)
            val localMessage = repo.createTempFileMessage(
                friend,
                filePath,
                if (image.isVideo) "video" else "image"
            ) ?: return@launch.also {
                toast.postValue(false to "发送失败")
            }
            isLoading.postValue(false)
            val uploadWork = enqueueSend(localMessage)
            block.invoke(workManager.getWorkInfoByIdLiveData(uploadWork.id))
        }
    }

    private suspend fun enqueueSend(localMessage: MessageEntity): OneTimeWorkRequest {
        return OneTimeWorkRequestBuilder<MessageUploadWorker>().apply {
            setInputData(
                workDataOf(
                    "message_data" to Gson().toJson(localMessage)
                )
            )
            setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
        }.build().apply {
            workManager.enqueue(this)
            localMessage.let {
                it.taskUuid = this.id.toString()
                repo.insertMessages(listOf(it))
            }
        }
    }

    fun deleteMessage(localMessage: MessageEntity) {
        viewModelScope.launch {
            isLoading.postValue(true)
            if (localMessage.sendStatus == -1 && localMessage.taskUuid.isNotEmpty()) {
                workManager.cancelWorkById(
                    UUID.fromString(
                        localMessage.taskUuid
                    )
                )
            } else {
                val result = repo.deleteMessage(localMessage.id)
                if (result is RequestResult.Error) {
                    toast.postValue(false to "删除消息失败:${result.message}")
                }
            }
            messageDao.deleteMessage(localMessage)
            isLoading.postValue(false)
        }
    }

    fun retrySendMessage(localMessage: MessageEntity, block: (LiveData<WorkInfo?>) -> Unit) {
        viewModelScope.launch {
            isLoading.postValue(true)
            // 启动任务
            val uploadWork = enqueueSend(localMessage)
            block.invoke(workManager.getWorkInfoByIdLiveData(uploadWork.id))
            isLoading.postValue(false)
        }
    }
}