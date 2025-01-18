package com.foxluo.baselib.util

import com.google.gson.Gson

object GsonUtil {
    fun Any.toJsonString() = Gson().toJson(this)
}