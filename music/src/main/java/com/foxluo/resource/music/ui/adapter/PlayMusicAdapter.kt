package com.foxluo.resource.music.ui.adapter

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.adapter.FragmentViewHolder
import com.foxluo.resource.music.data.database.MusicEntity
import com.foxluo.resource.music.ui.fragment.PlayFragment

class PlayMusicAdapter(fragmentActivity: FragmentActivity, val album: List<MusicEntity>) :
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