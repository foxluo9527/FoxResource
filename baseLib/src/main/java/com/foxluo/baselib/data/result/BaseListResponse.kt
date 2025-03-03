package com.foxluo.baselib.data.result

class BaseListResponse<D> : BaseResponse<ListData<D>>() {

}

class ListData<D> {
    var list: List<D>? = null
    var total: Int = 0
    var current: Int = 1
    var pageSize: Int = 1
    fun hadMore() = current * pageSize < total
}