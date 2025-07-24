package com.foxluo.baselib.ui.adapter

import android.net.Uri
import com.blankj.utilcode.util.TimeUtils
import com.foxluo.baselib.R
import com.foxluo.baselib.databinding.AlbumImageItemBinding
import com.foxluo.baselib.databinding.ItemAlbumTitleBinding
import com.foxluo.baselib.ui.adapter.AlbumAdapter.Item
import com.foxluo.baselib.util.ImageExt.loadUri
import com.foxluo.baselib.util.TimeUtil.nowTime
import com.foxluo.baselib.util.ViewExt.visible
import com.xuexiang.xui.adapter.recyclerview.BaseRecyclerAdapter
import com.xuexiang.xui.adapter.recyclerview.RecyclerViewHolder
import java.io.Serializable

class AlbumAdapter() : BaseRecyclerAdapter<Item>() {
    sealed class Item():Serializable

    data class Image(
        @Transient
        var uri: Uri,
        val date: Long,
        @Transient
        var croppedUri: Uri? = null,
        val isVideo: Boolean = false,
        var isCropped: Boolean = false,
        var isCompressed: Boolean = false
    ) : Item(), Serializable

    data class Title(val time: String) : Item()

    var imageList: List<Image> = listOf()
        set(value) {
            field = value
            val map = value.groupBy {
                val timeGap = nowTime - it.date
                val oneDay = 86400000
                if (timeGap <= oneDay * 3)
                    "最近"
                else if (timeGap <= oneDay * 7)
                    TimeUtils.getChineseWeek(it.date)
                else if (TimeUtils.isLeapYear(it.date))
                    TimeUtils.millis2String(
                        it.date,
                        "MM-dd"
                    ) + " " + TimeUtils.getChineseWeek(it.date)
                else
                    TimeUtils.millis2String(it.date, "yyyy-MM-dd") + " " + TimeUtils.getChineseWeek(
                        it.date
                    )
            }
            refresh(map.flatMap {
                mutableListOf<Item>().apply {
                    add(Title(it.key))
                    addAll(it.value)
                }
            })
        }


    override fun bindData(
        holder: RecyclerViewHolder,
        position: Int,
        item: Item?
    ) {
        if (item is Title) {
            val titleBinding = ItemAlbumTitleBinding.bind(holder.itemView)
            titleBinding.root.text = item.time
        } else if (item is Image) {
            val imageBinding = AlbumImageItemBinding.bind(holder.itemView)
            imageBinding.imageView.loadUri(item.uri)
            imageBinding.flagVideo.visible(item.isVideo)
        }
    }


    override fun getItemViewType(position: Int): Int {
        return if (getItem(position) is Title) 1 else 2
    }

    override fun getItemLayoutId(type: Int): Int {
        return if (type == 1) R.layout.item_album_title else R.layout.album_image_item
    }
}