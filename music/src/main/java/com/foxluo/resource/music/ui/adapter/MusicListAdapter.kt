package com.foxluo.resource.music.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.foxluo.baselib.R
import com.foxluo.baselib.util.ImageExt.loadUrlWithCorner
import com.foxluo.baselib.util.ViewExt.visible
import com.foxluo.resource.music.data.bean.MusicData
import com.foxluo.resource.music.databinding.ItemMusicListBinding

class MusicListAdapter(
    val moreVisible: Boolean,
    val onItemClick: (Boolean, Int) -> Unit
) :
    RecyclerView.Adapter<MusicListAdapter.MusicListViewHolder>() {
    private val musicList = mutableListOf<MusicData>()

    var currentIndex: Int? = null
        set(value) {
            val lastCurrentIndex = field
            field = value
            value?.let { notifyItemChanged(it) }
            lastCurrentIndex?.let { notifyItemChanged(it) }
        }

    fun getPlayList() = musicList

    @SuppressLint("NotifyDataSetChanged")
    fun setDataList(list: List<MusicData>) {
        musicList.clear()
        musicList.addAll(list)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun insertDataList(list: List<MusicData>) {
        musicList.addAll(list)
        notifyDataSetChanged()
    }

    inner class MusicListViewHolder(val binding: ItemMusicListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun setData(position: Int, data: MusicData) {
            binding.root.setBackgroundResource(if (currentIndex == position) R.color.F7F7F7 else R.color.white)
            binding.cover.loadUrlWithCorner(data.coverImg, 6)
            binding.name.text = data.title
            binding.singer.text = data.artist.name
            binding.more.visible(moreVisible)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemMusicListBinding.inflate(inflater, parent, false)
        return MusicListViewHolder(binding)
    }

    override fun getItemCount() = musicList.size

    override fun onBindViewHolder(holder: MusicListViewHolder, position: Int) {
        val data = musicList[position]
        holder.setData(position, data)
        holder.binding.more.setOnClickListener {
            onItemClick.invoke(true, position)
        }
        holder.binding.root.setOnClickListener {
            onItemClick.invoke(false, position)
        }
    }
}