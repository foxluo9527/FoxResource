package com.foxluo.chat.data.repo

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.foxluo.baselib.data.manager.AuthManager
import com.foxluo.chat.data.database.MessageDao
import com.foxluo.chat.data.database.MessageEntity

@OptIn(ExperimentalPagingApi::class)
class MessagePagingSource(val messageDao: MessageDao, val friendId: Int) : RemoteMediator<Int, MessageEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, MessageEntity>
    ): MediatorResult {
        TODO("Not yet implemented")
    }
}