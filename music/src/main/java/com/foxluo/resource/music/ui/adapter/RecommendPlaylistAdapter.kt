package com.foxluo.resource.music.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.foxluo.baselib.util.ImageExt.loadUrl
import com.foxluo.baselib.util.ImageExt.loadUrlWithCorner
import com.foxluo.baselib.util.ImageExt.processUrl
import com.foxluo.resource.music.data.result.PlaylistResult
import com.foxluo.resource.music.databinding.LayoutItemRecommendPlayListBinding

class RecommendPlaylistAdapter(
    val onItemClick: (PlaylistResult) -> Unit
) : ListAdapter<PlaylistResult, RecommendPlaylistAdapter.RecommendPlaylistViewHolder>(PLAYLIST_COMPARATOR) {
    companion object {
        private val PLAYLIST_COMPARATOR = object : DiffUtil.ItemCallback<PlaylistResult>() {
            override fun areItemsTheSame(oldItem: PlaylistResult, newItem: PlaylistResult): Boolean = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: PlaylistResult, newItem: PlaylistResult): Boolean = oldItem == newItem
        }
    }

    inner class RecommendPlaylistViewHolder(val binding: LayoutItemRecommendPlayListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun setData(data: PlaylistResult) {
            binding.cover.loadUrl(processUrl(data.coverImage))
            binding.tvPlayListTitle.text = data.title
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendPlaylistViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = LayoutItemRecommendPlayListBinding.inflate(inflater, parent, false)
        return RecommendPlaylistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecommendPlaylistViewHolder, position: Int) {
        val data = getItem(position)
        holder.setData(data)
        holder.binding.root.setOnClickListener {
            onItemClick.invoke(data)
        }
    }
}