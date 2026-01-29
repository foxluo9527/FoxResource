package com.foxluo.resource.music.ui.fragment

import android.annotation.SuppressLint
import android.view.View
import androidx.core.os.bundleOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.launcher.ARouter
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ToastUtils
import com.foxluo.baselib.R
import com.foxluo.baselib.data.manager.AuthManager
import com.foxluo.baselib.domain.AuthorizFailError
import com.foxluo.baselib.domain.viewmodel.EventViewModel
import com.foxluo.baselib.ui.BaseBindingFragment
import com.foxluo.baselib.ui.fragment.TempFragment
import com.foxluo.baselib.ui.view.StatusPager
import com.foxluo.baselib.util.DialogUtil.showInputDialog
import com.foxluo.baselib.util.ViewExt.visible
import com.foxluo.resource.music.data.domain.viewmodel.ArtistViewModel
import com.foxluo.resource.music.data.domain.viewmodel.PlaylistViewModel
import com.foxluo.resource.music.databinding.FragmentMainMusicBinding
import com.foxluo.resource.music.ui.adapter.HotSingerAdapter
import com.foxluo.resource.music.ui.adapter.MyPlaylistAdapter
import com.foxluo.resource.music.ui.adapter.RecommendPlaylistAdapter
import com.xuexiang.xui.widget.dialog.bottomsheet.BottomSheet
import com.xuexiang.xui.widget.dialog.bottomsheet.BottomSheet.BottomListSheetBuilder


class MainMusicFragment : BaseBindingFragment<FragmentMainMusicBinding>() {
    private val recommendMusicFragment by lazy {
        RecommendMusicFragment()
    }

    private val recentMusicFragment by lazy {
        RecentMusicFragment()
    }

    private val manageMusicFragment by lazy {
        ManageMusicFragment.getFragment(true,false)
    }

    private val playListFragment by lazy {
        TempFragment().apply {
            arguments = bundleOf("type" to "歌单")
        }
    }

    private val singerFragment by lazy {
        TempFragment().apply {
            arguments = bundleOf("type" to "歌手")
        }
    }

    private val recommendMusicStatusPager by lazy {
        getStatusPager(binding.rvRecommendPlayList) {
            loadRecommendPlaylistData()
        }
    }

    private val hotSingerStatusPager by lazy {
        getStatusPager(binding.rvHotSinger) {
            loadArtistListData()
        }
    }

    private val playlistMusicStatusPager by lazy {
        getStatusPager(binding.rvMyPlayList) {
            loadMyPlaylistData()
        }
    }

    private fun getStatusPager(replaceView: View, block: () -> Unit) =
        StatusPager.builder(replaceView).emptyViewLayout(com.foxluo.baselib.R.layout.state_empty)
            .loadingViewLayout(com.foxluo.baselib.R.layout.state_loading)
            .errorViewLayout(com.foxluo.baselib.R.layout.state_error)
            .addRetryButtonId(com.foxluo.baselib.R.id.btn_retry).setRetryClickListener { pager, _ ->
                retry(pager, block)
            }.build()


    private fun retry(statePager: StatusPager, block: () -> Unit) {
        if (statePager.currentError is AuthorizFailError) {
            val topActivity = ActivityUtils.getTopActivity()
            if (topActivity.javaClass.simpleName != "LoginActivity") {
                ARouter.getInstance().build("/mine/login").navigation(topActivity)
            }
        } else {
            block()
        }
    }

    // ViewModel
    private val playlistViewModel by lazy {
        ViewModelProvider(this)[PlaylistViewModel::class.java]
    }

    private val artistViewModel by lazy {
        ViewModelProvider(this)[ArtistViewModel::class.java]
    }

    // Adapter
    private val recommendPlaylistAdapter by lazy {
        RecommendPlaylistAdapter {
            // 处理推荐歌单点击事件
            EventViewModel.showMainPageFragment.value = PlaylistFragment().apply {
                arguments = bundleOf("id" to it.id.toString())
            } to System.currentTimeMillis()
        }
    }

    private val hotSingerAdapter by lazy {
        HotSingerAdapter {
            // 处理热门歌手点击事件
        }
    }

    private val myPlaylistAdapter by lazy {
        MyPlaylistAdapter {
            // 处理我的歌单点击事件
            EventViewModel.showMainPageFragment.value = PlaylistFragment().apply {
                arguments = bundleOf("id" to it.id.toString())
            } to System.currentTimeMillis()
        }
    }

    private val loadedDataList by lazy {
        MutableLiveData<Int>(0)
    }

    private val isAdmin
        get() = AuthManager.userInfoStateFlow.value.let {
            it?.isAdmin() == true
        }

    override fun initView() {
        super.initView()
        // 设置推荐歌单 RecyclerView
        binding.rvRecommendPlayList.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = recommendPlaylistAdapter
        }

        // 设置热门歌手 RecyclerView
        binding.rvHotSinger.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = hotSingerAdapter
        }

        // 设置我的歌单 RecyclerView
        binding.rvMyPlayList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = myPlaylistAdapter
        }
    }

    override fun initData() {
        super.initData()
        // 加载数据
        binding.root.isRefreshing = true
    }

    @SuppressLint("SetTextI18n")
    override fun initObserver() {
        super.initObserver()
        AuthManager.userInfoStateFlow.asLiveData(lifecycleScope.coroutineContext).observe(this){
            loadData()
            binding.musicManage.visible(isAdmin)
        }
        EventViewModel.updatePlaylist.observe(this) {
            if (it > 0L) {
                loadData()
            }
        }
        EventViewModel.deletePlaylist.observe(this) {id->
            if (id != null) {
                val currentList = myPlaylistAdapter.currentList.filter { it.id.toString() != id }
                myPlaylistAdapter.submitList(currentList)
                binding.count.text = "(${currentList.size})"
            }
        }
        loadedDataList.observe(this) {
            if (it >= 3) {
                binding.root.isRefreshing = false
            }
        }
    }

    override fun initListener() {
        super.initListener()
        binding.recommend.setOnClickListener {
            EventViewModel.showMainPageFragment.value = recommendMusicFragment to System.currentTimeMillis()
        }
        binding.recent.setOnClickListener {
            EventViewModel.showMainPageFragment.value = recentMusicFragment to System.currentTimeMillis()
        }

        // 推荐歌单更多按钮
        binding.moreRecommendPlayList.setOnClickListener {
            // 处理更多推荐歌单点击事件
        }

        // 热门歌手更多按钮
        binding.moreHotSinger.setOnClickListener {
            // 处理更多热门歌手点击事件
        }

        // 创建歌单按钮
        binding.createPlayList.setOnClickListener {
            BottomListSheetBuilder(getActivity())
                .addItem("导入网易云/QQ音乐歌单")
                .addItem("新建歌单")
                .setOnSheetItemClickListener(object :
                    BottomListSheetBuilder.OnSheetItemClickListener {
                    override fun onClick(
                        dialog: BottomSheet,
                        itemView: View?,
                        position: Int,
                        tag: String?
                    ) {
                        dialog.dismiss()
                        if (position==0){
                            // 导入歌单
                            requireContext().showInputDialog(content = "请输入歌单URL") {
                                playlistViewModel.importPlaylist(it) {
                                    loadMyPlaylistData()
                                }
                            }
                        }else{
                            // 处理创建歌单点击事件
                            requireContext().showInputDialog(content = "请输入歌单名称") {
                                playlistViewModel.createPlaylist(it) {
                                    loadMyPlaylistData()
                                }
                            }
                        }
                    }
                })
                .build()
                .show()
        }
        binding.root.setOnRefreshListener {
            loadedDataList.value = 0
            loadData()
        }
        binding.musicManage.setOnClickListener {
            EventViewModel.showMainPageFragment.value =
                manageMusicFragment to System.currentTimeMillis()
        }
    }

    private fun loadData() {
        loadRecommendPlaylistData()
        loadArtistListData()
        loadMyPlaylistData()
    }

    private fun loadRecommendPlaylistData() {
        recommendMusicStatusPager.showLoading()
        // 加载推荐歌单
        playlistViewModel.getPlaylist(isRecommend = true, success = {
            if (it.isNullOrEmpty()) {
                recommendMusicStatusPager.showEmpty()
            } else {
                recommendMusicStatusPager.showContent()
            }
            recommendPlaylistAdapter.submitList(it)
            loadedDataList.value = (loadedDataList.value ?: 0) + 1
        }, error = { error ->
            onLoadError(recommendMusicStatusPager, error)
            loadedDataList.value = (loadedDataList.value ?: 0) + 1
        })
    }

    private fun loadArtistListData() {
        hotSingerStatusPager.showLoading()
        // 加载热门歌手
        artistViewModel.getArtistList(success = {
            if (it.isNullOrEmpty()) {
                hotSingerStatusPager.showEmpty()
            } else {
                hotSingerStatusPager.showContent()
            }
            hotSingerAdapter.submitList(it)
            loadedDataList.value = (loadedDataList.value ?: 0) + 1
        }, error = { error ->
            onLoadError(hotSingerStatusPager, error)
            loadedDataList.value = (loadedDataList.value ?: 0) + 1
        })
    }

    private fun loadMyPlaylistData() {
        playlistMusicStatusPager.showLoading()
        // 加载我的歌单
        playlistViewModel.getPlaylist(isRecommend = false, success = {
            if (it.isNullOrEmpty()) {
                playlistMusicStatusPager.showEmpty()
            } else {
                playlistMusicStatusPager.showContent()
            }
            myPlaylistAdapter.submitList(it)
            // 更新歌单数量
            binding.count.text = "(${it.size})"
            loadedDataList.value = (loadedDataList.value ?: 0) + 1
        }, error = { error ->
            onLoadError(playlistMusicStatusPager, error)
            loadedDataList.value = (loadedDataList.value ?: 0) + 1
        })
        if (isAdmin) {
            playlistViewModel.getMusicStats { result ->
                binding.moreMusicManage.text = result?.music?.total?.let { "${it}首" } ?: ""
            }
        }
    }

    private fun onLoadError(statePager: StatusPager, error: Throwable) {
        statePager.showError(error).apply {
            setText(R.id.tv_title, error.message)
            if (error is AuthorizFailError) {
                setText(R.id.btn_retry, "点击登录")
            } else {
                setText(R.id.btn_retry, "点击重试")
            }
        }
    }

    override fun initBinding() = FragmentMainMusicBinding.inflate(layoutInflater)
}