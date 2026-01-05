package com.foxluo.resource.music.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.foxluo.resource.music.R
import com.foxluo.resource.music.databinding.ItemBottomMenuBinding

class MusicMoreMenuAdapter(
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<MusicMoreMenuAdapter.MenuViewHolder>() {

    private val items = listOf(
        MenuItem(R.drawable.ic_artist, "查看歌手"),
        MenuItem(R.drawable.ic_album, "查看专辑"),
        MenuItem(R.drawable.ic_add_playlist, "添加到播放列表"),
        MenuItem(R.drawable.ic_add_album, "添加到歌单"),
        MenuItem(R.drawable.ic_share, "分享"),
        MenuItem(R.drawable.ic_report, "举报")
    )

    data class MenuItem(
        val iconRes: Int,
        val title: String
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val binding = ItemBottomMenuBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MenuViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class MenuViewHolder(
        private val binding: ItemBottomMenuBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MenuItem) {
            binding.ivIcon.setImageResource(item.iconRes)
            binding.tvTitle.text = item.title

            binding.root.setOnClickListener {
                onItemClick.invoke(adapterPosition)
            }
        }
    }
}
