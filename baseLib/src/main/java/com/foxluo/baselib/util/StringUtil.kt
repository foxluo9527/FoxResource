package com.foxluo.baselib.util

import android.annotation.SuppressLint
import java.net.URL
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.Locale

object StringUtil {
    fun String.prefix(prefix: String): String {
        return "${prefix}$this"
    }

    // UTC时间格式化器（线程安全）
    private val isoFormatter by lazy {
        DateTimeFormatter.ISO_INSTANT
    }

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

    @SuppressLint("SimpleDateFormat")
    fun String.formatServerTime(): String {
        //进行转化时区
        return com.blankj.utilcode.util.TimeUtils.getFriendlyTimeSpanByNow(formatServerTimeS())
    }

    @SuppressLint("SimpleDateFormat")
    fun String.formatServerTimeS(): Long {
        //进行转化时区
        return Instant.from(isoFormatter.parse(this))
            .toEpochMilli()
    }

    @SuppressLint("SimpleDateFormat", "DefaultLocale")
    fun Long.toServerTime(): String {
        // 1. 转换为UTC时区时间
        val instant = Instant.ofEpochMilli(this)

        // 2. 格式化输出
        return isoFormatter.format(instant).let {
            // 确保毫秒部分有3位数字
            if (it.substringAfterLast(".").length < 4) {
                it.replace(Regex("(\\.[0-9]{1,3})Z"), { mr ->
                    String.format(".%03dZ", mr.groupValues[1].substring(1).toLong())
                })
            } else {
                it
            }
        }
    }

    fun getMediaType(fileType: String): String {
        return when (fileType.lowercase(Locale.ROOT)) {
            "txt" -> "text/plain"
            "html", "htm" -> "text/html"
            "css" -> "text/css"
            "csv" -> "text/csv"
            "xml" -> "application/xml"
            "json" -> "application/json"
            "js" -> "application/javascript"
            "pdf" -> "application/pdf"
            "zip" -> "application/zip"
            "tar" -> "application/x-tar"
            "gif" -> "image/gif"
            "jpeg", "jpg" -> "image/jpeg"
            "png" -> "image/png"
            "svg" -> "image/svg+xml"
            "mp3" -> "audio/mpeg"
            "wav" -> "audio/wav"
            "mp4" -> "video/mp4"
            "avi" -> "video/x-msvideo"
            else -> "application/octet-stream" // Default binary type
        }
    }
}