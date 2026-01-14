package com.foxluo.resource.music.ui.fragment

import android.annotation.SuppressLint
import android.view.Choreographer
import androidx.viewbinding.ViewBinding
import com.alibaba.android.arouter.launcher.ARouter
import com.blankj.utilcode.util.ActivityUtils
import com.foxluo.baselib.ui.MainPage
import com.foxluo.baselib.ui.view.StatusPager
import com.foxluo.resource.music.data.database.AlbumEntity
import com.foxluo.resource.music.databinding.LayoutMusicPlayListBinding
import com.foxluo.resource.music.player.PlayerManager
import com.google.android.material.appbar.AppBarLayout
import com.xuexiang.xui.utils.XToastUtils.toast

/**
 *    Author : 罗福林
 *    Date   : 2026/1/5
 *    Desc   :
 */
abstract class MainPageMusicFragment<Binding : ViewBinding> : BaseMusicFragment<Binding>(), MainPage {
    val playlistBinding by lazy {
        LayoutMusicPlayListBinding.bind(binding.root)
    }

    // Choreographer用于根据屏幕刷新率更新UI
    private val choreographer = Choreographer.getInstance()

    override fun getLeftPlayPadding() = initPlayDragPadding()?.get(0) ?: 0
    override fun getTopPlayPadding() = initPlayDragPadding()?.get(1) ?: 0
    override fun getRightPlayPadding() = initPlayDragPadding()?.get(2) ?: 0
    override fun getBottomPlayPadding() = initPlayDragPadding()?.get(3) ?: 0
    override fun showNavBottom() = false
    open fun initPlayDragPadding():IntArray?{ return null}

    override fun onResume() {
        super.onResume()
        if (statePager.curState == StatusPager.VIEW_STATE_LOADING) {
            adapter.refresh()
        }
    }

    override fun initObserver() {
        super.initObserver()
        // 监听adapter数据变化，更新歌曲数量
        adapter.registerAdapterDataObserver(object :
            androidx.recyclerview.widget.RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                updateSongCount()
            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                super.onItemRangeRemoved(positionStart, itemCount)
                updateSongCount()
            }

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                super.onItemRangeChanged(positionStart, itemCount)
                updateSongCount()
            }
        })
    }

    override fun initView() {
        super.initView()

        // 设置播放全部点击事件
        playlistBinding.llPlayAll.setOnClickListener {
            playAllSongs()
        }
    }

    protected fun updateMusicListHeight(appBarLayout: AppBarLayout, verticalOffset: Int) {
        choreographer.postFrameCallback { _ ->
            // 获取root布局高度
            val rootHeight = binding.root.height
            val totalScrollRange = appBarLayout.height
            // 计算剩余空间高度
            val remainingHeight =
                rootHeight - totalScrollRange - verticalOffset - playlistBinding.llMusicList.top
            playlistBinding.llMusicList.let {
                // 只在高度发生变化时才更新，避免不必要的布局
                if (it.height != remainingHeight) {
                    it.layoutParams = it.layoutParams?.apply {
                        height = remainingHeight
                    }
                }
            }
        }
    }

    /**
     * 更新歌曲数量
     */
    @SuppressLint("SetTextI18n")
    private fun updateSongCount() {
        val songCount = adapter.itemCount
        playlistBinding.tvSongCount.text = "${songCount}首"
    }

    /**
     * 播放全部歌曲
     */
    private fun playAllSongs() {
        if (statePager.curState != StatusPager.VIEW_STATE_CONTENT) {
            if (statePager.curState == StatusPager.VIEW_STATE_ERROR) {
                retry()
            }
            return
        }
        val playList = adapter.getPlayList()
        if (playList.isEmpty()) {
            toast("暂无歌曲可播放")
            return
        }

        AlbumEntity(
            albumId = getPlayListId(),
            title = getPlayListTitle(),
            curMusicId = 0
        ).apply {
            musics = playList.distinctBy { it.musicId }
            autoPlay = true
        }.let {
            PlayerManager.getInstance().loadAlbum(it, true)
        }
    }
}