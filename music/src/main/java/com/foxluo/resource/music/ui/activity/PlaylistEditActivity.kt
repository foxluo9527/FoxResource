package com.foxluo.resource.music.ui.activity

import android.net.Uri
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts.TakePicture
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.ConvertUtils.dp2px
import com.foxluo.baselib.ui.AlbumSelectorActivity
import com.foxluo.baselib.ui.BaseBindingActivity
import com.foxluo.baselib.ui.adapter.AlbumAdapter
import com.foxluo.baselib.ui.contract.CommonResultContract.resultDataContract
import com.foxluo.baselib.util.CropImageContract
import com.foxluo.baselib.util.CropImageResult
import com.foxluo.baselib.util.DialogUtil.showInputDialog
import com.foxluo.baselib.util.ImageExt.loadUrlWithCorner
import com.foxluo.baselib.util.ImageExt.processUrl
import com.foxluo.baselib.util.getFilePath
import com.foxluo.resource.music.R
import com.foxluo.resource.music.data.domain.viewmodel.PlaylistDetailViewModel
import com.foxluo.resource.music.data.result.PlaylistDetailResult
import com.foxluo.resource.music.data.result.Tag
import com.foxluo.resource.music.databinding.ActivityPlaylistEditBinding
import com.xuexiang.xui.utils.XToastUtils
import com.xuexiang.xui.utils.XToastUtils.success
import com.xuexiang.xui.utils.XToastUtils.toast
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 *    Author : 罗福林
 *    Date   : 2026/1/15
 *    Desc   : 歌单编辑页面
 */
class PlaylistEditActivity : BaseBindingActivity<ActivityPlaylistEditBinding>() {

    private val viewModel by viewModels<PlaylistDetailViewModel>()
    private var currentPlaylist: PlaylistDetailResult? = null

    // 封面图片相关
    private var currentTakePictureUri: Uri? = null
    private var coverImageUrl: String? = null

    // 标签相关
    private val selectedTags = mutableListOf<Tag>()
    private val tagsFlow = MutableStateFlow<List<Tag>>(emptyList())

    // 图片选择和裁剪
    private val pickImageLauncher =
        registerForActivityResult(resultDataContract<AlbumSelectorActivity, AlbumAdapter.Image>("image") { image, intent ->
            intent.data?.let {
                image.uri = it
            }
        }) {
            it ?: return@registerForActivityResult
            // 处理选择的图片
            lifecycleScope.launch {
                val processedImage = AlbumSelectorActivity.getProcessedImage(
                    this@PlaylistEditActivity,
                    it,
                    false
                ) { show, text, cancel ->
                    setLoading(show, text, cancel)
                }
                coverImageUrl = processedImage.uri.getFilePath() ?: return@launch
                binding.ivCover.loadUrlWithCorner(coverImageUrl, 8)
            }
        }

    private val takePicture = registerForActivityResult(TakePicture()) { result ->
        if (result) {
            currentTakePictureUri?.let { cropImageContract.launch(CropImageResult(it, 0f, 0f)) }
        }
    }

    private val cropImageContract = registerForActivityResult(CropImageContract()) { croppedUri ->
        croppedUri ?: return@registerForActivityResult
        val originUri = currentTakePictureUri ?: return@registerForActivityResult
        val image =
            AlbumAdapter.Image(originUri, System.currentTimeMillis(), croppedUri, isCropped = true)
        lifecycleScope.launch {
            val processedImage = AlbumSelectorActivity.getProcessedImage(
                this@PlaylistEditActivity,
                image,
                false
            ) { show, text, cancel ->
                setLoading(show, text, cancel)
            }
            viewModel.processUploadingFile.value = processedImage.uri.toString()
        }
    }

    override fun initStatusBarView(): View? {
        return binding.root
    }

    override fun initBinding() = ActivityPlaylistEditBinding.inflate(layoutInflater)

    override fun initView() {
        super.initView()
    }

    override fun initObserver() {
        super.initObserver()
        viewModel.processUploadingFile.observe(this) {
            binding.ivCover.loadUrlWithCorner(it, 8)
        }
        viewModel.toast.observe(this) { (isSuccess, message) ->
            if (isSuccess == true) {
                success(message)
            } else if (isSuccess == false) {
                XToastUtils.error(message)
            } else {
                toast(message)
            }
        }
    }

    override fun initData() {
        // 获取传递的歌单数据
        currentPlaylist = intent.getSerializableExtra("playlist") as? PlaylistDetailResult
        currentPlaylist?.let { playlist ->
            coverImageUrl = playlist.coverImage
            binding.ivCover.loadUrlWithCorner(processUrl(playlist.coverImage), 8)
            binding.tvName.text = playlist.title
            binding.tvDescription.text = playlist.description
            // 初始化标签
            playlist.tags?.let {
                selectedTags.addAll(it)
                updateSelectedTagsText()
            }
            binding.switchIsPublic.isChecked = playlist.isPublic == 1
        }

        // 加载标签数据
        loadTags()
    }

    override fun initListener() {
        binding.titleBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        // 编辑封面
        binding.cvCover.setOnClickListener {
            showImagePicker()
        }

        // 编辑名称
        binding.tvName.setOnClickListener {
            currentPlaylist?.let {
                showInputDialog("请输入歌单名称", preFill = it.title) {
                    binding.tvName.text = it
                }
            }
        }

        // 编辑简介
        binding.tvDescription.setOnClickListener {
            currentPlaylist?.let {
                showInputDialog("请输入歌单简介", preFill = it.description) {
                    binding.tvDescription.text = it
                }
            }
        }

        // 保存
        binding.btnSave.setOnClickListener {
            savePlaylist()
        }
        binding.llTags.setOnClickListener {
            val isShowTags = binding.expandableLayout.isExpanded
            binding.expandableLayout.setExpanded(!isShowTags)
            binding.ivSelectTag.animate().rotation(if (isShowTags) 0f else 90f).setDuration(300).start()
        }
    }

    /**
     * 显示图片选择器
     */
    private fun showImagePicker() {
        pickImageLauncher.launch(Unit)
    }

    /**
     * 加载标签数据
     */
    private fun loadTags() {
        viewModel.getMusicTags { tags ->
            tagsFlow.value = tags
            updateTagsView(tags)
        }
    }

    /**
     * 更新标签视图
     */
    private fun updateTagsView(tags: List<Tag>) {
        binding.flTags.removeAllViews()
        tags.forEach { tag ->
            val tagView = createTagView(tag)
            tagView.layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.MarginLayoutParams.WRAP_CONTENT,
                ViewGroup.MarginLayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(dp2px(5f), dp2px(5f), dp2px(5f), dp2px(5f))
            }
            binding.flTags.addView(tagView)
        }
    }

    /**
     * 创建标签视图
     */
    private fun createTagView(tag: Tag): View {
        val view = layoutInflater.inflate(R.layout.item_tag, null)
        val tvTag = view.findViewById<android.widget.TextView>(R.id.tvTag)
        tvTag.text = tag.name

        // 检查是否已选择
        if (selectedTags.contains(tag)) {
            view.isSelected = true
            view.setBackgroundResource(R.drawable.shape_tag_selected)
            tvTag.setTextColor(resources.getColor(com.foxluo.baselib.R.color.white, null))
        } else {
            view.isSelected = false
            view.setBackgroundResource(R.drawable.shape_tag_unselected)
            tvTag.setTextColor(resources.getColor(com.foxluo.baselib.R.color.color_666666, null))
        }

        // 点击事件
        view.setOnClickListener {
            if (tvTag.isSelected) {
                // 取消选择
                selectedTags.remove(tag)
                view.isSelected = false
                view.setBackgroundResource(R.drawable.shape_tag_unselected)
                tvTag.setTextColor(
                    resources.getColor(
                        com.foxluo.baselib.R.color.color_666666,
                        null
                    )
                )
            } else {
                // 选择标签，最多3个
                if (selectedTags.size < 3) {
                    selectedTags.add(tag)
                    view.isSelected = true
                    view.setBackgroundResource(R.drawable.shape_tag_selected)
                    tvTag.setTextColor(resources.getColor(com.foxluo.baselib.R.color.white, null))
                } else {
                    XToastUtils.warning("最多选择3个标签")
                }
            }
            updateSelectedTagsText()
        }

        return view
    }

    /**
     * 更新已选择标签文本
     */
    private fun updateSelectedTagsText() {
        binding.tvSelectedTags.text = selectedTags.joinToString(", ") { it.name }
    }

    /**
     * 保存歌单
     */
    private fun savePlaylist() {
        currentPlaylist?.let { playlist ->
            lifecycleScope.launch {
                val coverImage = coverImageUrl?.let {
                    viewModel.uploadFileAsync(it)
                } ?: playlist.coverImage

                val updatedPlaylist = playlist.copy(
                    title = binding.tvName.text.toString(),
                    description = binding.tvDescription.text.toString(),
                    coverImage = coverImage,
                    tags = selectedTags,
                    isPublic = if (binding.switchIsPublic.isChecked) 1 else 0
                )

                viewModel.updatePlaylist(updatedPlaylist) {
                    setResult(RESULT_OK, intent.putExtra("updatedPlaylist", updatedPlaylist))
                    finish()
                }
            }
        }
    }
}
