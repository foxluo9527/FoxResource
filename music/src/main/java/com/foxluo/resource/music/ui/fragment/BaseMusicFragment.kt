package com.foxluo.resource.music.ui.fragment

import android.view.LayoutInflater
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.alibaba.android.arouter.launcher.ARouter
import com.blankj.utilcode.util.ActivityUtils
import com.foxluo.baselib.R
import com.foxluo.baselib.domain.AuthorizFailError
import com.foxluo.baselib.domain.viewmodel.getAppViewModel
import com.foxluo.baselib.ui.BaseBindingFragment
import com.foxluo.baselib.ui.view.StatusPager
import com.foxluo.baselib.util.Constant
import com.foxluo.resource.music.data.database.AlbumEntity
import com.foxluo.resource.music.data.database.MusicEntity
import com.foxluo.resource.music.data.domain.viewmodel.MainMusicViewModel
import com.foxluo.resource.music.databinding.FragmentMusicListBinding
import com.foxluo.resource.music.player.PlayerManager
import com.foxluo.resource.music.ui.adapter.MusicListAdapter
import com.foxluo.resource.music.ui.adapter.MusicMoreMenuAdapter
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.scwang.smart.refresh.layout.constant.RefreshState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 *    Author : 罗福林
 *    Date   : 2026/1/5
 *    Desc   :通用音乐播放Fragment基类
 */
abstract class BaseMusicFragment<Binding : ViewBinding> : BaseBindingFragment<Binding>() {
    private val musicViewModel by lazy {
        getAppViewModel<MainMusicViewModel>()
    }

    private val playerManager by lazy {
        PlayerManager.getInstance()
    }

    protected val adapter by lazy {
        MusicListAdapter(showItemMore(), clickItem, moreClick)
    }

    private val clickItem: (Int) -> Unit = { position: Int ->
        musicViewModel.isCurrentMusicByUser = true
        AlbumEntity(
            albumId = getPlayListId(),
            title = getPlayListTitle(),
            curMusicId = position
        ).apply {
            musics = adapter.getPlayList().distinctBy { it.musicId }
            autoPlay = true
        }.let {
            PlayerManager.getInstance().loadAlbum(it, true)
        }
        onClickItem(position)
    }

    private val moreClick: (Int) -> Unit = { position ->
        onMoreClick(position)
    }

    val musicListBinding: FragmentMusicListBinding by lazy {
        FragmentMusicListBinding.bind(binding.root)
    }

    protected val statePager by lazy {
        StatusPager.builder(musicListBinding.loading)
            .emptyViewLayout(getEmptyViewLayout())
            .loadingViewLayout(getLoadingViewLayout())
            .errorViewLayout(getErrorViewLayout())
            .addRetryButtonId(getRetryBtn())
            .setRetryClickListener { pager, _ ->
                retry()
            }
            .build()
    }

    fun retry(){
        if (statePager.currentError is AuthorizFailError) {
            val topActivity = ActivityUtils.getTopActivity()
            if (topActivity.javaClass.simpleName != "LoginActivity") {
                ARouter.getInstance().build("/mine/login").navigation(topActivity)
            }
        } else {
            adapter.refresh()
        }
    }

    abstract val musicPager: MutableStateFlow<PagingData<MusicEntity>>
    private var bottomSheetDialog: BottomSheetDialog? = null

    private fun showBottomSheetMenu(position: Int) {
        bottomSheetDialog?.dismiss()

        val dialog = BottomSheetDialog(requireContext())
        val dialogBinding = com.foxluo.resource.music.databinding.BottomSheetMenuBinding.inflate(
            LayoutInflater.from(requireContext())
        )

        val menuAdapter = MusicMoreMenuAdapter { menuIndex ->
            val music = adapter.getItemData(position)
            onMenuAction(menuIndex,music)
            dialog.dismiss()
        }

        dialogBinding.rvMenu.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = menuAdapter
        }

        dialog.setContentView(dialogBinding.root)
        dialog.show()
        bottomSheetDialog = dialog
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bottomSheetDialog?.dismiss()
        bottomSheetDialog = null
    }

    override fun initObserver() {
        super.initObserver()
        lifecycleScope.launch {
            musicPager.collectLatest {
                adapter.submitData(it)
                adapter.updateCurrentIndex(musicViewModel.currentMusic.value?.musicId)
            }
        }
        var hasRefreshing = false
        var hasLoadingMore = false
        adapter.addLoadStateListener { loadState ->
            val refresh = loadState.refresh
            val append = loadState.append
            when (refresh) {
                is LoadState.Error -> {
                    musicListBinding.loading.finishRefresh(false)
                    statePager.showError(refresh.error).apply {
                        setText(R.id.tv_title, refresh.error.message)
                        if (refresh.error is AuthorizFailError) {
                            setText(R.id.btn_retry, "点击登录")
                        } else {
                            setText(R.id.btn_retry, "点击重试")
                        }
                    }
                }

                is LoadState.Loading -> {
                    hasRefreshing = true
                    if (musicListBinding.loading.state != RefreshState.Refreshing) {
                        statePager.showLoading()
                    }
                }

                is LoadState.NotLoading -> {
                    if (hasRefreshing) {
                        musicListBinding.loading.finishRefresh(true)
                        if (adapter.getPlayList().isEmpty()){
                            statePager.showEmpty()
                        }else{
                            statePager.showContent()
                        }
                        //如果第一页数据就没有更多了，第一页不会触发append
                        if ((append as? LoadState.NotLoading)?.endOfPaginationReached == true) {
                            //没有更多了(只能用source的append)
                            musicListBinding.loading.finishLoadMoreWithNoMoreData()
                        }
                    }
                }
            }

            when (append) {
                is LoadState.Loading -> {
                    hasLoadingMore = true
                    //重置上拉加载状态，显示加载loading
                    musicListBinding.loading.resetNoMoreData()
                }

                is LoadState.NotLoading -> {
                    if (hasLoadingMore) {
                        hasLoadingMore = false
                        if (append.endOfPaginationReached) {
                            //没有更多了(只能用source的append)
                            musicListBinding.loading.finishLoadMoreWithNoMoreData()
                        } else {
                            musicListBinding.loading.finishLoadMore(true)
                        }
                    }
                    if (PlayerManager.getInstance().album.albumId != Constant.TABLE_ALBUM_PLAYING_ID.toString()) return@addLoadStateListener
                    val displayList = adapter.getPlayList()
                    val playList = PlayerManager.getInstance().album.musics
                    val appendList = displayList.apply {
                        removeAll { item -> playList?.any { it.musicId == item.musicId } == true }
                    }
                    if (appendList.isNullOrEmpty()) return@addLoadStateListener
                    PlayerManager.getInstance().appendPlayList(appendList.toList())
                }

                is LoadState.Error -> {
                    musicListBinding.loading.finishLoadMore(false)
                }
            }
        }
        PlayerManager.getInstance().uiStates.observe(this) {
            if (it != null && it.musicId != musicViewModel.currentMusic.value?.musicId) {
                musicViewModel.currentMusic.value = PlayerManager.getInstance().currentPlayingMusic
            }
        }
        musicViewModel.currentMusic.observe(this) { currentMusic ->
            adapter.updateCurrentIndex(currentMusic?.musicId)
        }
    }

    override fun initListener() {
        super.initListener()
        //设置下拉刷新
        musicListBinding.loading.setOnRefreshListener {
            adapter.refresh()
        }
        //上拉加载更多
        musicListBinding.loading.setOnLoadMoreListener {
            adapter.retry()
        }
    }

    override fun initView() {
        super.initView()
        statePager.showLoading()
        musicListBinding.recycleView.adapter = adapter
    }

    open fun onClickItem(position: Int) {}

    open fun onMoreClick(position: Int) {
        showBottomSheetMenu(position)
    }

    open fun getEmptyViewLayout() = R.layout.state_empty

    open fun getLoadingViewLayout() = R.layout.state_loading

    open fun getErrorViewLayout() = R.layout.state_error

    open fun getRetryBtn() = R.id.btn_retry

    open fun showItemMore() = true

    abstract fun getPlayListId(): String

    abstract fun getPlayListTitle(): String

    abstract fun onMenuAction(action: Int, music: MusicEntity?)
}