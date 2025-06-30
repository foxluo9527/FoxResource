package com.foxluo.resource.music.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.foxluo.baselib.R
import com.foxluo.resource.music.data.bean.MusicData
import com.foxluo.resource.music.databinding.ItemPlayingMusicListBinding

class PlayingListAdapter(private val onItemClick: (Boolean, Int) -> Unit) :
    RecyclerView.Adapter<PlayingListAdapter.PlayingListViewHolder>() {

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

    inner class PlayingListViewHolder(val binding: ItemPlayingMusicListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun initData(position: Int, data: MusicData) {
            binding.title.text = data.title
            binding.singer.text = data.artist?.name
            binding.root.setBackgroundResource(if (currentIndex == position) R.color.F7F7F7 else R.color.white)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayingListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemPlayingMusicListBinding.inflate(inflater, parent, false)
        return PlayingListViewHolder(binding)
    }

    override fun getItemCount() = musicList.size

    override fun onBindViewHolder(holder: PlayingListViewHolder, position: Int) {
        holder.initData(position, musicList.get(position))
        holder.binding.close.setOnClickListener {
            onItemClick.invoke(true, position)
        }
        holder.binding.root.setOnClickListener {
            onItemClick.invoke(false, position)
        }
    }
}