package com.foxluo.resource.music.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.foxluo.baselib.util.ImageExt.loadUrlWithCorner
import com.foxluo.baselib.util.ImageExt.processUrl
import com.foxluo.resource.music.data.result.ArtistResult
import com.foxluo.resource.music.databinding.LayoutItemHotSingerListBinding

class HotSingerAdapter(
    val onItemClick: (ArtistResult) -> Unit
) : ListAdapter<ArtistResult, HotSingerAdapter.HotSingerViewHolder>(ARTIST_COMPARATOR) {
    companion object {
        private val ARTIST_COMPARATOR = object : DiffUtil.ItemCallback<ArtistResult>() {
            override fun areItemsTheSame(oldItem: ArtistResult, newItem: ArtistResult): Boolean = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: ArtistResult, newItem: ArtistResult): Boolean = oldItem == newItem
        }
    }

    inner class HotSingerViewHolder(val binding: LayoutItemHotSingerListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun setData(data: ArtistResult) {
            binding.singerHeader.loadUrlWithCorner(processUrl(data.coverImage), 32)
            binding.singerName.text = data.name
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HotSingerViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = LayoutItemHotSingerListBinding.inflate(inflater, parent, false)
        return HotSingerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HotSingerViewHolder, position: Int) {
        val data = getItem(position)
        holder.setData(data)
        holder.binding.root.setOnClickListener {
            onItemClick.invoke(data)
        }
    }
}