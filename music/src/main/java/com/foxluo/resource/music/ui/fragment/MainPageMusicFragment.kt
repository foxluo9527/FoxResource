package com.foxluo.resource.music.ui.fragment

import android.R
import android.annotation.SuppressLint
import android.view.Choreographer
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.blankj.utilcode.util.ConvertUtils.dp2px
import com.foxluo.baselib.domain.viewmodel.EventViewModel
import com.foxluo.baselib.ui.MainPage
import com.foxluo.baselib.ui.view.StatusPager
import com.foxluo.baselib.util.ViewExt.gone
import com.foxluo.baselib.util.ViewExt.visible
import com.foxluo.resource.music.data.database.AlbumEntity
import com.foxluo.resource.music.data.domain.viewmodel.PlaylistViewModel
import com.foxluo.resource.music.databinding.LayoutMusicPlayListBinding
import com.foxluo.resource.music.player.PlayerManager
import com.google.android.material.appbar.AppBarLayout
import com.xuexiang.xui.utils.XToastUtils
import com.xuexiang.xui.utils.XToastUtils.success
import com.xuexiang.xui.utils.XToastUtils.toast
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog.ListCallback


/**
 *    Author : 罗福林
 *    Date   : 2026/1/5
 *    Desc   :
 */
abstract class MainPageMusicFragment<Binding : ViewBinding> : BaseMusicFragment<Binding>(), MainPage {
    private val playlistViewModel by lazy {
        ViewModelProvider(this)[PlaylistViewModel::class.java]
    }
    private val onBackPressedCallback by lazy {
        object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                if (adapter.isSelectModel) {
                    playlistBinding.tvComplete.performClick()
                }
            }
        }
    }
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
        playlistViewModel.toast.observe(this){ (isSuccess, message) ->
            if (isSuccess == true) {
                success(message)
            } else if (isSuccess == false) {
                XToastUtils.error(message)
            } else {
                toast(message)
            }
        }
        playlistViewModel.isLoading.observe(this){
            setLoading(it)
        }
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

    @SuppressLint("SetTextI18n")
    override fun onSelectChanged() {
        playlistBinding.addToPlaying.isEnabled = adapter.hasSelected()
        playlistBinding.addToPlaylist.isEnabled = adapter.hasSelected()
        playlistBinding.download.isEnabled = adapter.hasSelected()
        playlistBinding.delete.isEnabled = adapter.hasSelected()
        playlistBinding.selectCount.text = "${adapter.selectCount}/${adapter.itemCount}首"
        playlistBinding.rbSelectAll.isChecked = adapter.isSelectAll
    }

    override fun initView() {
        super.initView()
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback)
    }

    override fun initListener() {
        super.initListener()
        // 设置播放全部点击事件
        playlistBinding.llPlayAll.setOnClickListener {
            playAllSongs()
        }
        playlistBinding.rbSelectAll.setOnClickListener {
            adapter.allSelect()
        }
        playlistBinding.ivAllSelect.setOnClickListener {
            onSelect()
            playlistBinding.llPlayAll.gone()
            playlistBinding.llSelectAll.visible()
            playlistBinding.llListControl.visible()
            adapter.isSelectModel = true
            EventViewModel.mainPageStateChanged.value = System.currentTimeMillis()
            onBackPressedCallback.isEnabled = true
        }
        playlistBinding.tvComplete.setOnClickListener {
            playlistBinding.llPlayAll.visible()
            playlistBinding.llSelectAll.gone()
            playlistBinding.llListControl.gone()
            adapter.isSelectModel = false
            EventViewModel.mainPageStateChanged.value = System.currentTimeMillis()
            onBackPressedCallback.isEnabled = false
        }
        playlistBinding.addToPlaying.setOnClickListener {
            toast("已添加到播放队列")
            PlayerManager.getInstance().appendPlayList(adapter.getSelectedList())
        }
        playlistBinding.addToPlaylist.setOnClickListener {
            val ids = adapter.getSelectedList().map { it.musicId }
            addToPlaylist(ids)
        }
    }

    protected fun addToPlaylist(ids: List<String>){
        playlistViewModel.getPlaylist(isRecommend = false, success = { list ->
            val playLists = list.filter { it.id.toString() != getPlayListId() }
            MaterialDialog.Builder(requireContext())
                .title("选择要添加的歌单")
                .items(playLists.map { it.title })
                .itemsCallback(ListCallback { dialog: MaterialDialog?, view: View?, which: Int, text: CharSequence? ->
                    playlistViewModel.addMusicToPlaylist(
                        playLists[which].id.toString(),
                        ids,
                    ){
                        toast("已添加到歌单")
                    }
                })
                .show()
        },error={
            toast("获取歌单列表失败")
        })
    }

    abstract fun onSelect()

    protected fun updateMusicListHeight(appBarLayout: AppBarLayout, verticalOffset: Int) {
        choreographer.postFrameCallback { _ ->
            // 获取root布局高度
            val rootHeight = binding.root.height
            val totalScrollRange = appBarLayout.height
            // 计算剩余空间高度
            val remainingHeight =
                rootHeight - totalScrollRange - verticalOffset - playlistBinding.llMusicList.top - if (adapter.isSelectModel) dp2px(60f) else 0
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
        if (adapter.isSelectModel){
            onSelectChanged()
        }
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