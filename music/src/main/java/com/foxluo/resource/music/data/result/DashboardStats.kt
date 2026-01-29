package com.foxluo.resource.music.data.result

/**
 *    Author : 罗福林
 *    Date   : 2026/1/26
 *    Desc   :
 */

data class DashboardStats(val users:Stats,val music: Stats,)

data class Stats(val total:Int?)