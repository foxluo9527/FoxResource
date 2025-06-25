package com.foxluo.baselib.domain.handler

interface PostActionHandler {
    sealed interface PostAction {
        data class likeAction(val id: String) : PostAction
        data class commentAction(
            val id: String,
            val content: String,
            val objectId: String,
            val replyId: String?
        ) :
            PostAction
        data class detailAction(val id:String):PostAction
    }

    suspend fun handleAction(action: PostAction)
}