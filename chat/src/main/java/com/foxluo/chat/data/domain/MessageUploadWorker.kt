package com.foxluo.chat.data.domain

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.blankj.utilcode.util.GsonUtils
import com.foxluo.baselib.data.respository.UploadRepository
import com.foxluo.baselib.data.result.FileUploadResponse
import com.foxluo.baselib.data.result.RequestResult
import com.foxluo.chat.data.database.MessageEntity
import com.foxluo.chat.data.repo.ChatRepository
import kotlinx.coroutines.delay
import java.util.UUID

class MessageUploadWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
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
    private val uploadRepo by lazy {
        UploadRepository()
    }

    override suspend fun doWork(): Result {
        val localMessage = inputData.getString("message_data")?.let {
            GsonUtils.fromJson(it, MessageEntity::class.java)
        } ?: return Result.failure()
        val uuid = inputData.getString("id")?.let {
            UUID.fromString(it)
        } ?: return getFailureWithMessage("获取任务id失败")
        return try {
            setMessageState(localMessage.apply {
                taskUuid = uuid.toString()
            }, -1)
            val result = when (localMessage.type) {
                "text" -> repo.sendTextMessage(localMessage)
                "voice" -> repo.sendVoiceMessage(localMessage)
                "file" -> {
                    val filePath = localMessage.file_path ?: return Result.failure(
                        Data.Builder().putString("message", "找不到文件").build().also {
                            setMessageState(localMessage, 0)
                        }
                    )
                    val uploadResult = getUploadResult(filePath) ?: return Result.failure(
                        Data.Builder().putString("message", "文件上传失败").build().also {
                            setMessageState(localMessage, 0)
                        }
                    )
                    localMessage.apply {
                        file_url = uploadResult.url
                        file_name = uploadResult.filename.split("/").last()
                        file_size = uploadResult.size
                    }
                    repo.sendFileMessage(localMessage)
                }

                else -> repo.sendTextMessage(localMessage)
            }
            delay(100)
            if (result.isSuccess()) {
                Result.success()
            } else {
                getFailureWithMessage((result as RequestResult.Error).message)
            }
        } catch (e: Exception) {
            getFailureWithMessage(e.message).also {
                if (e !is kotlinx.coroutines.CancellationException){
                    setMessageState(localMessage, 0)
                }
            }
        }
    }

    private fun getFailureWithMessage(message: String?) =
        Result.failure(Data.Builder().putString("message", message ?: "消息发送失败").build())

    private suspend fun setMessageState(localMessage: MessageEntity, state: Int) {
        repo.insertMessages(listOf(localMessage.apply {
            sendStatus = state
        }))
    }

    private suspend fun getUploadResult(filePath: String): FileUploadResponse? {
        return uploadRepo.uploadFile(filePath).let {
            if (it is RequestResult.Success) {
                val data = it.data as FileUploadResponse
                data
            } else null
        }
    }
}
