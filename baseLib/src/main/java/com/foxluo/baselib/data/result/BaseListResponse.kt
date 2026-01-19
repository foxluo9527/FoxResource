package com.foxluo.baselib.data.result

import kotlinx.serialization.Serializable

class BaseListResponse<D> : BaseResponse<ListData<D>>() {

}

@Serializable
data class ListData<D>(
    var list: List<D>? = null,
    var total: Int = 0,
    var current: Int = 1,
    var pageSize: Int = 1
) : java.io.Serializable {
    fun hadMore() = current * pageSize < total
}