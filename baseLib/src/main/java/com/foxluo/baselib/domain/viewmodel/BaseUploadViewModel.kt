package com.foxluo.baselib.domain.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.foxluo.baselib.data.respository.UploadRepository
import com.foxluo.baselib.data.result.FileUploadResponse
import com.foxluo.baselib.data.result.RequestResult
import kotlinx.coroutines.launch

open class BaseUploadViewModel : BaseViewModel() {
    private val uploadRepo by lazy {
        UploadRepository()
    }
    val processUploadingFile by lazy {
        MutableLiveData<String>()
    }

    fun uploadFile(filePath: String) {
        viewModelScope.launch {
            isLoading.value = true
            uploadRepo.uploadFile(filePath).let {
                if (it is RequestResult.Success<*>) {
                    val data = it.data as FileUploadResponse
                    processUploadingFile.value = data.url
                }
            }
        }
    }

    suspend fun uploadFileAsync(filePath: String): String? {
        isLoading.value = true
        return uploadRepo.uploadFile(filePath).let {
            if (it is RequestResult.Success<*>) {
                val data = it.data as FileUploadResponse
                processUploadingFile.value = data.url
                data.url
            } else {
                null
            }
        }
    }

    protected suspend fun getUploadResult(filePath: String): FileUploadResponse? {
        return uploadRepo.uploadFile(filePath).let {
            if (it is RequestResult.Success<*>) {
                val data = it.data as FileUploadResponse
                data
            } else null
        }
    }
}