package com.foxluo.baselib.domain.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

open class BaseViewModel : ViewModel() {
    val isLoading by lazy {
        MutableLiveData<Boolean>()
    }

    var page: Int = 1

    var size: Int = 20

    val toast by lazy {
        MutableLiveData<Pair<Boolean?, String>>()
    }
}