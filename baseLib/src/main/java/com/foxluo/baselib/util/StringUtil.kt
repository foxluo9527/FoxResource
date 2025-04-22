package com.foxluo.baselib.util

import android.annotation.SuppressLint
import java.net.URL
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.Date
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

    @SuppressLint("SimpleDateFormat")
    fun String.formatServerTime(): String {
        //进行转化时区
        val dateFormat: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US)
        val formatDate: Date? = dateFormat.parse(this.replace("Z", "+0000"))
        return formatDate?.time?.getFriendlyTimeSpanByNow() ?: "时间格式化错误"
    }

    /**
     * 获取友好型与当前时间的差
     *
     * @param millis 毫秒时间戳
     * @return 友好型与当前时间的差
     *
     *  * 如果小于1秒钟内，显示刚刚
     *  * 如果在1分钟内，显示XXX秒前
     *  * 如果在1小时内，显示XXX分钟前
     *  * 如果在1小时外的今天内，显示今天15:32
     *  * 如果是昨天的，显示昨天15:32
     *  * 其余显示，2016-10-15
     *  * 时间不合法的情况全部日期和时间信息，如星期六 十月 27 14:21:20 CST 2007
     *
     */
    @SuppressLint("DefaultLocale")
    fun Long.getFriendlyTimeSpanByNow(): String {
        val millis = this
        val now = System.currentTimeMillis()
        val span = now - millis
        if (span < 0) return String.format(
            "%tc",
            millis
        ) // U can read http://www.apihome.cn/api/java/Formatter.html to understand it.

        if (span < 1000) {
            return "刚刚"
        } else if (span < ConstUtils.MIN) {
            return java.lang.String.format("%d秒前", span / ConstUtils.SEC)
        } else if (span < ConstUtils.HOUR) {
            return java.lang.String.format("%d分钟前", span / ConstUtils.MIN)
        }
        // 获取当天00:00
        val wee: Long = (now / ConstUtils.DAY) * ConstUtils.DAY
        if (millis >= wee) {
            return String.format("今天%tR", millis)
        } else if (millis >= wee - ConstUtils.DAY) {
            return String.format("昨天%tR", millis)
        } else {
            return String.format("%tF", millis)
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