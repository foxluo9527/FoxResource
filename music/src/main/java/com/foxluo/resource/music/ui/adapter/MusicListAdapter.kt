package com.foxluo.resource.music.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.foxluo.baselib.R
import com.foxluo.baselib.util.ImageExt.loadUrlWithCorner
import com.foxluo.baselib.util.ImageExt.processUrl
import com.foxluo.baselib.util.ViewExt.visible
import com.foxluo.resource.music.data.database.MusicEntity
import com.foxluo.resource.music.databinding.ItemMusicListBinding

class MusicListAdapter(
    val moreVisible: Boolean,
    val onItemClick: (Boolean, Int) -> Unit
) :
    PagingDataAdapter<MusicEntity, MusicListAdapter.MusicListViewHolder>(MUSIC_COMPARATOR) {
    companion object {
        private val MUSIC_COMPARATOR = object : DiffUtil.ItemCallback<MusicEntity>() {
            override fun areItemsTheSame(oldItem: MusicEntity, newItem: MusicEntity): Boolean =
                oldItem.musicId == newItem.musicId

            override fun areContentsTheSame(oldItem: MusicEntity, newItem: MusicEntity): Boolean =
                oldItem == newItem
        }
    }

    fun getPlayList() = (if (itemCount > 0) Array<MusicEntity>(itemCount) { getItem(it)!! }.toList()
    else listOf<MusicEntity>()).toMutableList()

    var currentIndex: Int? = null
        set(value) {
            val lastCurrentIndex = field
            field = value
            value?.let { notifyItemChanged(it) }
            lastCurrentIndex?.let { notifyItemChanged(it) }
        }

    inner class MusicListViewHolder(val binding: ItemMusicListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun setData(position: Int, data: MusicEntity) {
            binding.root.setBackgroundResource(if (currentIndex == position) R.color.F7F7F7 else R.color.white)
            binding.cover.loadUrlWithCorner(processUrl(data.coverImg), 6)
            binding.name.text = data.title
            binding.singer.text = data.artist?.name
            binding.more.visible(moreVisible)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemMusicListBinding.inflate(inflater, parent, false)
        return MusicListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MusicListViewHolder, position: Int) {
        val data = getItem(position) ?: return
        holder.setData(position, data)
        holder.binding.more.setOnClickListener {
            onItemClick.invoke(true, position)
        }
        holder.binding.root.setOnClickListener {
            onItemClick.invoke(false, position)
        }
    }
}