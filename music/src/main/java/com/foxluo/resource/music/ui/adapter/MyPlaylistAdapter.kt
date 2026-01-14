package com.foxluo.resource.music.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.foxluo.baselib.util.ImageExt.loadUrlWithCorner
import com.foxluo.baselib.util.ImageExt.processUrl
import com.foxluo.resource.music.data.result.PlaylistResult
import com.foxluo.resource.music.databinding.LayoutItemMyPlayListBinding

class MyPlaylistAdapter(
    val onItemClick: (PlaylistResult) -> Unit
) : ListAdapter<PlaylistResult, MyPlaylistAdapter.MyPlaylistViewHolder>(PLAYLIST_COMPARATOR) {
    companion object {
        private val PLAYLIST_COMPARATOR = object : DiffUtil.ItemCallback<PlaylistResult>() {
            override fun areItemsTheSame(oldItem: PlaylistResult, newItem: PlaylistResult): Boolean = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: PlaylistResult, newItem: PlaylistResult): Boolean = oldItem == newItem
        }
    }

    inner class MyPlaylistViewHolder(val binding: LayoutItemMyPlayListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun setData(data: PlaylistResult) {
            binding.ivPlayListCover.loadUrlWithCorner(processUrl(data.coverImage), 8)
            binding.tvPlayListName.text = data.title
            binding.tvPlayListCount.text = "${data.trackCount}首"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyPlaylistViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = LayoutItemMyPlayListBinding.inflate(inflater, parent, false)
        return MyPlaylistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyPlaylistViewHolder, position: Int) {
        val data = getItem(position)
        holder.setData(data)
        holder.binding.root.setOnClickListener {
            onItemClick.invoke(data)
        }
    }
}