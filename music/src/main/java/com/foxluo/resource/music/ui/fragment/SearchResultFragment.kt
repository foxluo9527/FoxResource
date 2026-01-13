package com.foxluo.resource.music.ui.fragment

import android.view.LayoutInflater
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import com.foxluo.baselib.R
import com.foxluo.baselib.domain.AuthorizFailError
import com.foxluo.baselib.domain.viewmodel.getAppViewModel
import com.foxluo.baselib.ui.MainPageFragment
import com.foxluo.baselib.ui.view.StatusPager
import com.foxluo.baselib.util.Constant
import com.foxluo.resource.music.data.database.AlbumEntity
import com.foxluo.resource.music.data.database.MusicEntity
import com.foxluo.resource.music.data.domain.viewmodel.MainMusicViewModel
import com.foxluo.resource.music.data.domain.viewmodel.SearchMusicViewModel
import com.foxluo.resource.music.databinding.FragmentMusicListBinding
import com.foxluo.resource.music.databinding.FragmentSearchResultBinding
import com.foxluo.resource.music.player.PlayerManager
import com.foxluo.resource.music.ui.adapter.MusicMoreMenuAdapter
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.xuexiang.xui.utils.XToastUtils.toast
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SearchResultFragment : MainPageMusicFragment<FragmentSearchResultBinding>() {
    private val keyword by lazy {
        arguments?.getString("keyword") ?: ""
    }

    private val vm by viewModels<SearchMusicViewModel>()

    override val musicPager: MutableStateFlow<PagingData<MusicEntity>>
        get() = vm.musicPager


    override fun initBinding(): FragmentSearchResultBinding {
        return FragmentSearchResultBinding.inflate(layoutInflater)
    }

    override fun getPlayListId(): String {
        return Constant.SEARCH_RESULT_ID.toString()
    }

    override fun getPlayListTitle(): String {
        return Constant.SEARCH_RESULT_ALBUM_TITLE_PREFIX + keyword
    }

    override fun onMenuAction(
        action: Int,
        music: MusicEntity?
    ) {
        when (action) {
            0 -> toast("查看歌手: ${music?.artist?.name}")
            1 -> toast("查看专辑: ")
            2 -> toast("下一首播放")
            3 -> toast("添加到歌单: ${music?.title}")
            4 -> toast("分享: ${music?.title}")
            5 -> toast("举报: ${music?.title}")
        }
    }

    override fun initData() {
        super.initData()
        vm.loadMusic(keyword = keyword, sort = "")
    }

    /**
     * 搜索结果页始终显示playview
     */
    override fun showPlayView(): Boolean {
        return true
    }

    override fun initPlayDragPadding(): IntArray? {
        return intArrayOf(20, 50, 20, 0)
    }
}
