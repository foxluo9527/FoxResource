package com.foxluo.resource.music.data.domain.handler

import com.foxluo.baselib.domain.handler.PostActionHandler
import com.foxluo.resource.music.data.repo.MusicRepository
import com.foxluo.resource.music.data.domain.viewmodel.MusicCommentViewModel

interface MusicActionHandler : PostActionHandler {
    val repo: MusicRepository

    val viewModel: MusicCommentViewModel

    override suspend fun handleAction(action: PostActionHandler.PostAction) {
        when (action) {
            is PostActionHandler.PostAction.likeAction -> {
                viewModel.likeComment(action.id){

                }
            }

            is PostActionHandler.PostAction.commentAction -> {

            }

            is PostActionHandler.PostAction.detailAction -> {

            }
        }
    }
}