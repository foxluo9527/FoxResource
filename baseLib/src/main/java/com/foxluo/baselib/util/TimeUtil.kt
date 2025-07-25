package com.foxluo.baselib.util

import com.blankj.utilcode.util.TimeUtils

object TimeUtil {
    val nowTime: Long
        get() = System.currentTimeMillis()

    const val oneDay = 24 * 60 * 60 * 1000L

    fun getChatTime(time: Long): String {
        if (TimeUtils.isToday(time)) {
            return TimeUtils.millis2String(time, "HH:mm")
        } else if (TimeUtils.isToday(time - oneDay)) {
            return "昨天"
        } else if (TimeUtils.isToday(time - oneDay * 7)) {
            return TimeUtils.getChineseWeek(time)
        } else if (TimeUtils.isLeapYear(time)) {
            return TimeUtils.millis2String(time, "MM-dd")
        } else {
            return TimeUtils.millis2String(time, "yyyy-MM-dd")
        }
    }
}