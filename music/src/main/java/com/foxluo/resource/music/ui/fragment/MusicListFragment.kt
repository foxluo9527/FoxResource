package com.foxluo.resource.music.ui.fragment

import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ToastUtils
import com.foxluo.baselib.R
import com.foxluo.baselib.ui.BaseBindingFragment
import com.foxluo.resource.music.data.bean.AlbumData
import com.foxluo.resource.music.data.bean.MusicData
import com.foxluo.resource.music.data.domain.viewmodel.MusicViewModel
import com.foxluo.resource.music.databinding.FragmentMusicListBinding
import com.foxluo.resource.music.player.PlayerManager
import com.foxluo.resource.music.ui.adapter.MusicListAdapter

class MusicListFragment : BaseBindingFragment<FragmentMusicListBinding>() {
    private val vm: MusicViewModel by viewModels()

    private val adapter by lazy {
        MusicListAdapter(false, onClickItem)
    }

    private val onClickItem: (Boolean, MusicData, Int) -> Unit =
        { showMore: Boolean, data: MusicData, position: Int ->
            PlayerManager.getInstance().loadAlbum(
                AlbumData(
                    null,
                    "播放列表",
                    null,
                    null,
                    null,
                    adapter.getPlayList()
                ), position
            )
        }

    override fun initObserver() {
        vm.isLoading.observe(this) {
            setLoading(it)
            binding.refresh.finishRefresh()
            binding.refresh.finishLoadMore()
        }
        vm.toast.observe(this) {
            ToastUtils.showShort(it.second)
        }
        vm.dataList.observe(this) {
            if (vm.page == 1) {
                adapter.setDataList(it)
            } else {
                adapter.insertDataList(it)
            }
        }
    }

    override fun initView() {
        binding.recycleView.adapter = adapter
        binding.refresh.setPrimaryColorsId(R.color.color_F05019, R.color.white)
    }

    override fun initListener() {
        binding.refresh.setOnRefreshListener {
            vm.getMusicData(true)
        }
        binding.refresh.setOnLoadMoreListener {
            vm.getMusicData(false)
        }
    }

    override fun initData() {
        binding.refresh.autoRefresh()
    }

    override fun initBinding() = FragmentMusicListBinding.inflate(layoutInflater)
}