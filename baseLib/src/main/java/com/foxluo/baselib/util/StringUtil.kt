package com.foxluo.baselib.util

import java.net.URL
import java.util.Locale

object StringUtil {
    fun getUrlName(url: String?): String {
        var urlName: String? = null
        try {
            var urlPath = URL(url).path.lowercase(Locale.getDefault())
            urlName = urlPath.substring(urlPath.lastIndexOf('/') + 1)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
        return urlName
    }
}