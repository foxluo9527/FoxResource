package com.foxluo.resource.music.ui.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.get
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagingData
import com.blankj.utilcode.util.BarUtils
import com.foxluo.baselib.data.manager.AuthManager
import com.foxluo.baselib.domain.viewmodel.EventViewModel
import com.foxluo.baselib.ui.ImageViewInfo
import com.foxluo.baselib.util.DialogUtil.showConfirmDialog
import com.foxluo.baselib.util.ImageExt.loadUrlWithCorner
import com.foxluo.baselib.util.ImageExt.processUrl
import com.foxluo.baselib.util.ViewExt.visible
import com.foxluo.resource.music.R
import com.foxluo.resource.music.data.database.MusicEntity
import com.foxluo.resource.music.data.domain.viewmodel.PlaylistDetailViewModel
import com.foxluo.resource.music.data.result.PlaylistDetailResult
import com.foxluo.resource.music.databinding.FragmentAlbumMusicListBinding
import com.foxluo.resource.music.ui.activity.PlaylistEditActivity
import com.xuexiang.xui.utils.XToastUtils.error
import com.xuexiang.xui.utils.XToastUtils.info
import com.xuexiang.xui.utils.XToastUtils.success
import com.xuexiang.xui.utils.XToastUtils.toast
import com.xuexiang.xui.widget.dialog.bottomsheet.BottomSheet
import com.xuexiang.xui.widget.dialog.bottomsheet.BottomSheet.BottomListSheetBuilder
import com.xuexiang.xui.widget.imageview.preview.PreviewBuilder
import kotlinx.coroutines.flow.MutableStateFlow


/**
 *    Author : 罗福林
 *    Date   : 2026/1/14
 *    Desc   :
 */
class PlaylistFragment : MainPageMusicFragment<FragmentAlbumMusicListBinding>() {
    private val id by lazy {
        arguments?.getString("id") ?: ""
    }

    override val musicPager: MutableStateFlow<PagingData<MusicEntity>>
        get() = vm.playlistMusicPager
    private val vm: PlaylistDetailViewModel by viewModels()

    // 处理编辑返回结果
    private val editPlaylistLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                (result.data?.getSerializableExtra("updatedPlaylist") as? PlaylistDetailResult)?.let {
                    // 更新歌单详情
                    vm.setPlaylistDetail(it)
                    EventViewModel.updatePlaylist.value = System.currentTimeMillis()
                }
            }
        }

    override fun getPlayListId(): String {
        return vm.playlistDetail.value?.id.toString()
    }

    override fun getPlayListTitle(): String {
        return vm.playlistDetail.value?.title ?: ""
    }

    override fun onMenuAction(
        action: Int,
        music: MusicEntity?
    ) {

    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun initView() {
        super.initView()
        // 设置返回键颜色和大小
        binding.toolbar.navigationIcon =
            resources.getDrawable(com.xuexiang.xui.R.drawable.xui_ic_navigation_back_white, null)
        binding.toolbar.setTitleTextColor(Color.WHITE)
        binding.toolbar.apply {
            layoutParams = layoutParams?.apply {
                height = BarUtils.getStatusBarHeight()
            }
        }
        // 设置返回键点击事件
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        // 设置CollapsingToolbar标题
        binding.collapsingToolbar.setCollapsedTitleTextColor(Color.WHITE)
        binding.collapsingToolbar.setStatusBarScrimColor(
            resources.getColor(
                com.foxluo.baselib.R.color.color_ED6C40,
                null
            )
        )
    }

    override fun initObserver() {
        super.initObserver()
        vm.isLoading.observe(this) {
            setLoading(it)
        }
        vm.toast.observe(this) { (isSuccess, message) ->
            if (isSuccess == true) {
                success(message)
            } else if (isSuccess == false) {
                error(message)
            } else {
                toast(message)
            }
        }
        AuthManager.userInfoStateFlow.asLiveData(lifecycleScope.coroutineContext).observe(this) {
            adapter.refresh()
        }
        vm.playlistDetail.observe(this) {
            if (it.isImporting == true) {
                info("歌单导入中")
            }
            binding.collapsingToolbar.title = it.title
            binding.toolbar.title = it.title
            binding.ivCover.loadUrlWithCorner(processUrl(it.coverImage), 10)
            binding.tvSubtitle.visible(!(it.description.isNullOrEmpty()))
            binding.tvSubtitle.text = it.description
            binding.toolbar.menu[0].setVisible(it?.creatorID == AuthManager.authInfo?.user?.id)
        }
    }

    override fun initData() {
        super.initData()
        if (id.isNotEmpty()) {
            vm.loadPlayListMusic(id)
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun initListener() {
        super.initListener()
        binding.appBar.addOnOffsetChangedListener(this::updateMusicListHeight)
        binding.toolbar.menu[0].setOnMenuItemClickListener {
            BottomListSheetBuilder(getActivity())
                .addItem(
                    resources.getDrawable(com.foxluo.baselib.R.drawable.ic_edit, null),
                    "编辑歌单"
                )
                .addItem(
                    resources.getDrawable(com.foxluo.baselib.R.drawable.ic_delete, null),
                    "删除歌单"
                )
                .setOnSheetItemClickListener(object :
                    BottomListSheetBuilder.OnSheetItemClickListener {
                    override fun onClick(
                        dialog: BottomSheet,
                        itemView: View?,
                        position: Int,
                        tag: String?
                    ) {
                        dialog.dismiss()
                        if (position == 0) {
                            // 编辑歌单
                            vm.playlistDetail.value?.let {
                                val intent = Intent(context, PlaylistEditActivity::class.java)
                                intent.putExtra("playlist", it)
                                editPlaylistLauncher.launch(intent)
                            }
                        } else {
                            requireContext().showConfirmDialog("是否确认删除歌单") {
                                vm.deletePlaylist(id) {
                                    requireActivity().onBackPressedDispatcher.onBackPressed()
                                }
                                EventViewModel.deletePlaylist.value = id
                            }
                        }
                    }
                })
                .build()
                .show()
            true
        }
        binding.ivCover.setOnClickListener {
            vm.playlistDetail.value?.coverImage?.let {
                PreviewBuilder.from(this).setImg(ImageViewInfo(processUrl(it))).start()
            }
        }
    }

    override fun initBinding() = FragmentAlbumMusicListBinding.inflate(layoutInflater)

    override fun showPlayView() = true
}