package com.foxluo.resource.music.ui.adapter

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.adapter.FragmentViewHolder
import com.foxluo.baselib.R
import com.foxluo.resource.music.data.bean.MusicData
import com.foxluo.resource.music.databinding.FragmentPlayBinding
import com.foxluo.resource.music.player.PlayerManager
import com.foxluo.resource.music.ui.activity.PlayActivity
import com.foxluo.resource.music.ui.fragment.DetailLyricsFragment
import com.foxluo.resource.music.ui.fragment.DetailSongFragment
import com.foxluo.resource.music.ui.fragment.PlayFragment
import com.google.android.material.tabs.TabLayoutMediator
import com.xuexiang.xui.adapter.recyclerview.BaseRecyclerAdapter
import com.xuexiang.xui.adapter.recyclerview.RecyclerViewHolder

class PlayMusicAdapter(fragmentActivity: FragmentActivity, val album: List<MusicData>) :
    FragmentStateAdapter(fragmentActivity) {

    private val fragmentList = Array<PlayFragment>(album.size) { position ->
        PlayFragment().apply {
            arguments = bundleOf("musicData" to album[position])
        }
    }

    override fun createFragment(position: Int): Fragment {
        return fragmentList[position]
    }

    override fun getItemCount() = album.size

    fun getFragment(position: Int) = fragmentList.getOrNull(position)

    fun getFragmentIndex(fragment: PlayFragment) = fragmentList.indexOf(fragment)

    override fun onViewDetachedFromWindow(holder: FragmentViewHolder) {
        super.onViewDetachedFromWindow(holder)
        getFragment(holder.position)?.onDetached()
    }

    fun getFragmentPositionByMusicId(musicId: String) =
        album.indexOf(album.find { it.musicId == musicId })
}