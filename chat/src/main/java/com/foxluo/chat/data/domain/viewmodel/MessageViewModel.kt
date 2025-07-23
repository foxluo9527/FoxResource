package com.foxluo.chat.data.domain.viewmodel

import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.foxluo.baselib.data.manager.AuthManager
import com.foxluo.baselib.data.result.RequestResult
import com.foxluo.baselib.domain.viewmodel.BaseViewModel
import com.foxluo.chat.data.database.ChatEntity
import com.foxluo.chat.data.domain.ChatModuleInitializer
import com.foxluo.chat.data.repo.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MessageViewModel : BaseViewModel() {
    val friendDao by lazy {
        ChatModuleInitializer.chatDb.friendDao()
    }
    private val messageDao by lazy {
        ChatModuleInitializer.chatDb.messageDao()
    }
    private val chatDao by lazy {
        ChatModuleInitializer.chatDb.chatDao()
    }
    private val repo by lazy {
        ChatRepository(messageDao, chatDao, friendDao)
    }

    val chatPager by lazy {
        MutableStateFlow<PagingData<ChatEntity>>(PagingData.empty())
    }

    fun loadMessage(block: () -> Unit) {
        viewModelScope.launch {
            val userId = AuthManager.authInfo?.user?.id?.toInt()
            if (userId != null) {
                val result = repo.getUnreadMessages()
                if (result is RequestResult.Error) {
                    toast.postValue(false to result.message)
                }
                block.invoke()
                Pager(
                    config = PagingConfig(
                        pageSize = 20,
                        prefetchDistance = 2,
                        initialLoadSize = 20
                    ),
                    pagingSourceFactory = { chatDao.getChats(userId) }
                ).flow
                    .cachedIn(viewModelScope)
                    .collectLatest { pagingData ->
                        chatPager.value = pagingData
                    }
            } else {
                block.invoke()
            }
        }
    }
}