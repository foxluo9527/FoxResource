package com.foxluo.resource.music.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.foxluo.baselib.util.ImageExt.loadUrlWithCorner
import com.foxluo.baselib.util.ViewExt.visible
import com.foxluo.resource.music.data.bean.MusicData
import com.foxluo.resource.music.databinding.ItemMusicListBinding

class MusicListAdapter(val onItemClick: (Boolean, MusicData) -> Unit) : RecyclerView.Adapter<MusicListAdapter.MusicListViewHolder>() {
    private val musicList = mutableListOf<MusicData>()
    private var moreVisible = false
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

    fun setVisibleMore(visible: Boolean){

    }

    inner class MusicListViewHolder(val binding: ItemMusicListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun setData(data: MusicData) {
            binding.cover.loadUrlWithCorner(data.coverImg, 6)
            binding.name.text = data.title
            binding.singer.text = data.artist.name
        }

        fun visibleMore(visible: Boolean) {
            binding.more.visible(visible)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemMusicListBinding.inflate(inflater)
        return MusicListViewHolder(binding)
    }

    override fun getItemCount() = musicList.size

    override fun onBindViewHolder(holder: MusicListViewHolder, position: Int) {
        val data = musicList.get(position)
        holder.setData(data)
        holder.binding.more.setOnClickListener {
            onItemClick.invoke(true, data)
        }
        holder.binding.root.setOnClickListener {
            onItemClick.invoke(false, data)
        }
    }
}